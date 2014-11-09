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
    val xpathIncludingThisElem = withInitialXpath + (if (nameAttribute.isEmpty) "" else nameAttribute.get.text)
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
}
