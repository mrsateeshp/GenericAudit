package com.thoughtstream.audit.domain

/**
 *
 * @author Sateesh
 * @since 10/11/2014
 */
abstract class VariableType

trait XMLElementAware {
  def getXmlElementTag: String

  def unapply(label: String): Option[Boolean] = Some(getXmlElementTag.equalsIgnoreCase(label))
}

case class Primitive(value: String, isNumeric: Boolean = false) extends VariableType

object Primitive extends XMLElementAware {
  val getXmlElementTag = "primitive"

  def unapply(that: VariableType): Option[(String, Boolean)] = {
    that match {
      case null => None
      case x: Primitive => Some((x.value, x.isNumeric))
      case _ => None
    }
  }
}

trait ComplexType {
  this: VariableType =>
}

class Entity extends VariableType with ComplexType

object Entity extends XMLElementAware {
  val getXmlElementTag = "entity"

  def apply() = new Entity
}

class ValueObject extends VariableType with ComplexType

object ValueObject extends XMLElementAware {
  val getXmlElementTag = "valueObject"

  def apply() = new ValueObject
}

class Collection extends VariableType with ComplexType

object Collection extends XMLElementAware {
  val getXmlElementTag = "collection"

  def apply() = new Collection
}

