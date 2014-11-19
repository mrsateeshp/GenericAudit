package com.thoughtstream.audit.process

import com.thoughtstream.audit.domain._

import scala.xml.Elem

/**
 *
 * @author Sateesh
 * @since 12/11/2014
 */
object JsonAuditMessageProcessor extends AuditMessageProcessor {

  override def process(newObject: Elem, oldObject: Elem = <root/>): String = {
    val result = doConvertToJsonString(performDiffAndMerge(oldObject,newObject))
    println(result)
    "{" + result + "}"
  }

  def compareXpaths(firstStr: String, secondStr: String): Boolean = {
    val first = firstStr.toLowerCase
    val second = secondStr.toLowerCase
    if( first.count(_.equals('/')) != second.count(_.equals('/')) ) {
      first < second
    } else {
      if(first.substring(0,first.lastIndexOf('/')+1).equals(second.substring(0,second.lastIndexOf('/')+1))){
        val firstElement = first.substring(first.lastIndexOf('/')+1)
        val secondElement = second.substring(second.lastIndexOf('/')+1)
        if(firstElement.forall(Character.isDigit) && secondElement.forall(Character.isDigit)) {
          firstElement.toInt < secondElement.toInt
        } else {
          first < second
        }
      } else {
        first < second
      }

    }
  }
  
  def performDiffAndMerge(oldObject: Elem, newObject: Elem): Seq[(String, AttributeType)] = {
    val oldObjectDataMap = extractVariablesWithXpaths(oldObject)
    val newObjectDataMap = extractVariablesWithXpaths(newObject)

    val collectionsFromOldObject = oldObjectDataMap.filter(_._2.isInstanceOf[Collection]).map(_._1)

    val primitivesFromOld = oldObjectDataMap.filter(_._2.isInstanceOf[Primitive]).filter(x => !collectionsFromOldObject.exists(y => x._1.startsWith(y)))

    var mergedObjectDataMap = newObjectDataMap

    primitivesFromOld.foreach(x=> {
      val key = x._1
      val value = x._2.asInstanceOf[Primitive]
      val newValueOption = newObjectDataMap.get(key)
      if(newValueOption.isEmpty) {
        mergedObjectDataMap += (key + postfixForOldPrimitiveValue -> value)
        mergedObjectDataMap += (key -> Primitive(""))
      }else {
        val newValue = newObjectDataMap(key).asInstanceOf[Primitive]
        if (!value.value.equals(newValue.value)) {
          mergedObjectDataMap += (key + postfixForOldPrimitiveValue -> value)
        }
      }
    })

    mergedObjectDataMap.toSeq.sortWith((x,y)=>compareXpaths(x._1,y._1))
  }



  def generateKeyPart(xpathKey: String) = {
    val jsonKey = xpathKey.substring(xpathKey.lastIndexOf('/') + 1)

    if (!jsonKey.startsWith("DO_NOT_USE")) {
      "\"" + jsonKey + "\":"
    } else {
      ""
    }
  }

  def generatePrimitiveValue(primitive: Primitive): String = {
    if (primitive.isNumeric) primitive.value else "\"" + primitive.value + "\""
  }

  def applyDoNotUseTag(parentKey: String, input: (String, AttributeType)): (String, AttributeType) = {
    val key = input._1
    if (key.startsWith(parentKey)) {
      (parentKey + "/DO_NOT_USE" + key.substring(parentKey.length + 1), input._2)
    } else {
      input
    }

  }

  def processNestedCollections(key: String, source: Seq[(String, AttributeType)]): String = {
    val childElements = source.tail.filter(_._1.startsWith(key + "/")).map(x => applyDoNotUseTag(key, x))
    val resultTemp = generateKeyPart(key) + " [" + doConvertToJsonString(childElements) + "]"
    val remaining = source.tail.filter(!_._1.startsWith(key + "/"))
    if (remaining.isEmpty)
      resultTemp
    else
      resultTemp + " , " + doConvertToJsonString(remaining)
  }

  def processNestedObjects(key: String, source: Seq[(String, AttributeType)]): String = {
    val resultTemp = generateKeyPart(key) + " {" + doConvertToJsonString(source.tail.filter(_._1.startsWith(key + "/"))) + "}"
    val remaining = source.tail.filter(!_._1.startsWith(key + "/"))
    if (remaining.isEmpty)
      resultTemp
    else
      resultTemp + " , " + doConvertToJsonString(remaining)
  }

  def doConvertToJsonString(source: Seq[(String, AttributeType)]): String = {

    val (key, value) = source.head
    println(key)
    val result: String = value match {
      case x: Primitive if source.tail.isEmpty => generateKeyPart(key) + generatePrimitiveValue(x)
      case x: Primitive => generateKeyPart(key) + generatePrimitiveValue(x) + " , " + doConvertToJsonString(source.tail)
      case _: Entity | _: ValueObject => processNestedObjects(key, source)
      case x: Collection => processNestedCollections(key, source)
    }
    result
  }

  def extractVariablesWithXpaths(elements: Seq[Elem], withInitialXpath: String, result: Map[String, AttributeType]): Map[String, AttributeType] = {
    val element = elements.head
    val nextElements = elements.tail

    val nameAttribute = element.attribute("name")
    //ignore the element that does not have name attribute. name is mandatory.
    if (nameAttribute.isEmpty) {
      if (nextElements.isEmpty) return result else extractVariablesWithXpaths(nextElements, withInitialXpath, result)
    }

    val xpathIncludingThisElem = withInitialXpath + nameAttribute.get.text

    //thisElementTotalResult  means --> (incoming_result
    // + this_element + all_child_elements)
    val thisElementTotalResult = if (element.child.isEmpty) {
      //leaf element --> mostly primitive
      element.label match {
        case Primitive(true) if element.attribute("value").isDefined =>
          result + (xpathIncludingThisElem -> constructVariableType(element))

        //else ignore the item
        case _ => result
      }
    } else {
      val resultPlusThisElementXpath = result + (xpathIncludingThisElem -> constructComplexType(element))
      //exercising all child elements
      val childElements = element.child.collect {
        case x: Elem => x
      }
      extractVariablesWithXpaths(childElements, xpathIncludingThisElem + "/", resultPlusThisElementXpath)
    }

    //if it is the last element in the sequence, just return the result, else move it to next element in the sequence
    if (nextElements.isEmpty) {
      thisElementTotalResult
    } else {
      extractVariablesWithXpaths(nextElements, withInitialXpath, thisElementTotalResult)
    }
  }

  def extractVariablesWithXpaths(element: Elem, withInitialXpath: String = "/", result: Map[String, AttributeType] = Map()): Map[String, AttributeType] = {
    extractVariablesWithXpaths(Seq(element), withInitialXpath, result)
  }
}
