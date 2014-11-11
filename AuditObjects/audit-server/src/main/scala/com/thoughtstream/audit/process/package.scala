package com.thoughtstream.audit

import com.thoughtstream.audit.domain._

import scala.xml.Elem

/**
 *
 * @author Sateesh
 * @since 10/11/2014
 */
package object process {

  def extractVariablesWithXpaths(elements: Seq[Elem], withInitialXpath: String, result: Map[String, VariableType]): Map[String, VariableType] = {
    val element = elements.head
    val nextElements = elements.tail

    val nameAttribute = element.attribute("name")
    //ignore the element that does not have name attribute. name is mandatory.
    if (nameAttribute.isEmpty) {
      if(nextElements.isEmpty) return result else extractVariablesWithXpaths(nextElements,withInitialXpath,result)
    }

    val xpathIncludingThisElem = withInitialXpath + nameAttribute.get.text

    //thisElementTotalResult  means --> (incoming_result
    // + this_element + all_child_elements)
    val thisElementTotalResult = if (element.child.isEmpty) {
      //leaf element --> mostly primitive
      val value = element.attribute("value")
      if(element.label.equals("primitive") && value.isDefined) {
        val numericAtt = element.attribute("numeric")
        val isNumeric = if(numericAtt.isDefined) {
          numericAtt.get.text.equalsIgnoreCase("true")
        } else false

        result + (xpathIncludingThisElem -> Primitive(value.get.text, isNumeric))
      } else {
        //else ignore the item
        result
      }
    } else {
      val resultPlusThisElementXpath = result + (xpathIncludingThisElem -> getComplexTypeFromElementLabel(element.label).asInstanceOf[VariableType])
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

  def extractVariablesWithXpaths(element: Elem, withInitialXpath: String = "/", result: Map[String, VariableType] = Map()): Map[String, VariableType] = {
    extractVariablesWithXpaths(Seq(element), withInitialXpath, result)
  }
}
