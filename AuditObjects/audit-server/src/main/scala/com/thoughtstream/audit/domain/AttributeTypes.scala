package com.thoughtstream.audit.domain

/**
 *
 * @author Sateesh
 * @since 10/11/2014
 */
abstract class AttributeType

trait XMLElementAware {
  def getXmlElementTag: String

  def unapply(label: String): Option[Boolean] = Some(getXmlElementTag.equalsIgnoreCase(label))
}

case class Primitive(value: String, isNumeric: Boolean = false) extends AttributeType

object Primitive extends XMLElementAware {
  val getXmlElementTag = "primitive"

  def unapply(that: AttributeType): Option[(String, Boolean)] = {
    that match {
      case null => None
      case x: Primitive => Some((x.value, x.isNumeric))
      case _ => None
    }
  }
}

trait ComplexType {
  this: AttributeType =>
}

class Entity extends AttributeType with ComplexType

object Entity extends XMLElementAware {
  val getXmlElementTag = "entity"

  def apply() = new Entity
}

class ValueObject extends AttributeType with ComplexType

object ValueObject extends XMLElementAware {
  val getXmlElementTag = "valueObject"

  def apply() = new ValueObject
}

class Collection extends AttributeType with ComplexType

object Collection extends XMLElementAware {
  val getXmlElementTag = "collection"

  def apply() = new Collection
}

