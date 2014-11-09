package com.thoughtstream

import scala.xml.Elem

/**
 *
 * @author Sateesh
 * @since 07/11/2014
 */
package object audit {

  def findAllAuditableXpaths(element: Elem, nextElements: Seq[Elem] = Seq.empty, withInitialXpath: String = "/", result: Map[String, String] = Map()): Map[String, String] = {
    val nameAttribute = element.attribute("name")
    val xpathIncludingThisElem = withInitialXpath + (if (nameAttribute.isEmpty) "" else nameAttribute.get.text )
    val resultPlusThisElementXpath = if (nameAttribute.isEmpty) result else result + (xpathIncludingThisElem -> element.label)

    //thisElementTotalResult  means --> (result_input + this_element + all_child_elements)
    val thisElementTotalResult = if (element.child.isEmpty) {
      //leaf element --> mostly primitive
      resultPlusThisElementXpath
    } else {
      //exercising all child elements
      val childElements = element.child.collect {
        case x: Elem => x
      }
      findAllAuditableXpaths(childElements.head, childElements.tail, xpathIncludingThisElem + "/", resultPlusThisElementXpath)
    }

    //if it is the last element in the sequence, just return the result, else move it to next element in the sequence
    if (nextElements.isEmpty) {
      thisElementTotalResult
    } else {
      findAllAuditableXpaths(nextElements.head, nextElements.tail, withInitialXpath, thisElementTotalResult)
    }
  }

  //todo: convert this to tail recursion
  def findAllAuditableXpathsWithOutRecursion(element: Elem, withInitialXpath: String = "/"): Map[String, String] = {
    val nameAttribute = element.attribute("name")
    val xpathIncludingThisElem = withInitialXpath + (if (nameAttribute.isEmpty) "" else nameAttribute.get.text + "/")

    if (element.child.isEmpty) {
      if (nameAttribute.isEmpty) Map[String, String]() else Map(xpathIncludingThisElem -> element.label)
    } else {

      var indexes: Map[String, String] = Map()

      for (node <- element.child) {
        //consider only elements and ignore such as comments..
        node match {
          case x: Elem => indexes ++= findAllAuditableXpathsWithOutRecursion(x, xpathIncludingThisElem)
          case _ =>
        }
      }

      if (nameAttribute.nonEmpty) {
        indexes += (xpathIncludingThisElem -> element.label)
      }

      indexes
    }
  }

  /*def getAuditableXpathForNameAttribute(element: Elem) ={
    val nameAttribute = element.attribute("name")

    if(nameAttribute.isEmpty) Map[String,String]() else Map(nameAttribute.get.text -> element.label)
  }*/

  /*def getAuditableXpathsOfEntity(entity: Elem) = {

  val nameAttribute = entity.attribute("name")

  entity match {
  case <entity/> =>
  val valueOfNameAttribute = if(nameAttribute.isEmpty) "" else nameAttribute.get.text + "/"
    getAuditableXpathForNameAttribute(entity) ++ Map(valueOfNameAttribute + "id" -> "attribute", valueOfNameAttribute + "type" -> "attribute")
  }
}*/


  def getIndexesOld(element: Elem): Map[String,String] = {

    val currentIndex = element.attribute("name")

    var indexes: Map[String,String] = Map()

    for(node <- element.child) {
      val attributeName = node.attribute("name").get.text

      node match {
        case <attribute/> => indexes += (attributeName -> "attribute")

        case <entity/> =>

          indexes += (attributeName -> node.label)
          indexes +=  (attributeName + "/id"->"attribute")
          indexes += (attributeName + "/type"->"attribute")

        case <valueObject/> => indexes = indexes ++ findAllAuditableXpaths(node.asInstanceOf)

        case <collection>{children @ _*}</collection> =>

          indexes += (attributeName -> node.label)
          if(children.nonEmpty) {
            children.head match {

              case <entity/> =>
                indexes +=  (attributeName + "/id"->"attribute")
                indexes += (attributeName + "/type"->"attribute")

              case <valueObject/> => indexes = indexes ++ findAllAuditableXpaths(node.asInstanceOf)

              case _=>
            }
          }


        case _ => println("ignoring: " + node.text)
      }
    }

    if(currentIndex.nonEmpty) {
      indexes = indexes.map(x => currentIndex.get.text + "/" + x._1 -> x._2)
      indexes + (currentIndex.get.text -> element.label)
    }

    indexes
  }
}
