package com.thoughtstream

import scala.xml.Elem

/**
 *
 * @author Sateesh
 * @since 07/11/2014
 */

package object audit {
  @deprecated
  def findAllPrimitiveValuesWithXpath(element: Elem, withInitialXpath: String = "/", result: Map[String, String] = Map()): Map[String, String] = {
    findAllPrimitiveValuesWithXpath(Seq(element), withInitialXpath, result)
  }

  @deprecated
  def findAllPrimitiveValuesWithXpath(elements: Seq[Elem], withInitialXpath: String, result: Map[String, String]): Map[String, String] = {
    val element = elements.head
    val nextElements = elements.tail

    val nameAttribute = element.attribute("name")

    //todo: ignoring collections for now
    if (nameAttribute.isEmpty) {
      if (nextElements.isEmpty) {
        return result
      } else {
        return findAllPrimitiveValuesWithXpath(nextElements, withInitialXpath, result)
      }
    }

    val xpathIncludingThisElem = withInitialXpath + (if (nameAttribute.isEmpty) "" else nameAttribute.get.text)

    //thisElementTotalResult  means --> (incoming_result
    // + this_element + all_child_elements)
    val thisElementTotalResult = element match {
      case <primitive/> => result + (xpathIncludingThisElem -> element.attribute("value").get.text)
      case _ => //exercising all child elements
        val childElements = element.child.collect {
          case x: Elem => x
        }
        findAllPrimitiveValuesWithXpath(childElements, xpathIncludingThisElem + "/", result)
    }

    //if it is the last element in the sequence, just return the result, else move it to next element in the sequence
    if (nextElements.isEmpty) {
      thisElementTotalResult
    } else {
      findAllPrimitiveValuesWithXpath(nextElements, withInitialXpath, thisElementTotalResult)
    }
  }

  @deprecated
  def findAllAuditableXpaths(element: Elem, withInitialXpath: String = "/", result: Map[String, String] = Map()): Map[String, String] = {
    findAllAuditableXpaths(Seq(element), withInitialXpath, result)
  }

  @deprecated
  def findAllAuditableXpaths(elements: Seq[Elem], withInitialXpath: String, result: Map[String, String]): Map[String, String] = {
    val element = elements.head
    val nextElements = elements.tail

    val nameAttribute = element.attribute("name")
    val xpathIncludingThisElem = withInitialXpath + (if (nameAttribute.isEmpty) "" else nameAttribute.get.text)
    val resultPlusThisElementXpath = if (nameAttribute.isEmpty) result else result + (xpathIncludingThisElem -> element.label)

    //thisElementTotalResult  means --> (incoming_result
    // + this_element + all_child_elements)
    val thisElementTotalResult = if (element.child.isEmpty) {
      //leaf element --> mostly primitive
      resultPlusThisElementXpath
    } else {
      //exercising all child elements
      val childElements = element.child.collect {
        case x: Elem => x
      }
      findAllAuditableXpaths(childElements, xpathIncludingThisElem + "/", resultPlusThisElementXpath)
    }

    //if it is the last element in the sequence, just return the result, else move it to next element in the sequence
    if (nextElements.isEmpty) {
      thisElementTotalResult
    } else {
      findAllAuditableXpaths(nextElements, withInitialXpath, thisElementTotalResult)
    }
  }
}
