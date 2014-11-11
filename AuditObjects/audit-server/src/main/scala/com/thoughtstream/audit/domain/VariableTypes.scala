package com.thoughtstream.audit.domain

/**
 *
 * @author Sateesh
 * @since 10/11/2014
 */
abstract class VariableType

case class Primitive(value: String, isNumeric: Boolean = false) extends VariableType

trait ComplexType {
  this: VariableType =>
}

trait XMLElementAware {
  def getXmlElementTag: String
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

