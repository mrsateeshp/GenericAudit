package com.thoughtstream.audit

/**
 *
 * @author Sateesh
 * @since 11/11/2014
 */
package object domain {
  def getComplexTypeFromElementLabel(label: String): ComplexType = {
    label match {
      case Entity.getXmlElementTag => Entity()
      case ValueObject.getXmlElementTag => ValueObject()
      case Collection.getXmlElementTag => Collection()
    }
  }
}
