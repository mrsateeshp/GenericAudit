package com.thoughtstream.audit

import scala.xml.Elem

/**
 *
 * @author Sateesh
 * @since 11/11/2014
 */
package object domain {

  def constructVariableType(element: Elem): AttributeType = {
    element.label match {
      case Primitive(true) =>
        val value = element.attribute("value").getOrElse(Seq()).text
        val numericAtt = element.attribute("numeric")
        val isNumeric = if (numericAtt.isDefined) {
          numericAtt.get.text.equalsIgnoreCase("true")
        } else {
          Utils.isNumeric(value)
        }

        Primitive(value, isNumeric)

      case _ => constructComplexType(element)
    }
  }

  def constructComplexType(element: Elem): AttributeType = {
    element.label match {
      case Entity(true) => Entity()

      case ValueObject(true) => ValueObject()

      case Collection(true) => Collection()
    }
  }
}
