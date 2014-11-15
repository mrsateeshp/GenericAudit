package com.thoughtstream.audit.process

import com.thoughtstream.audit.Utils

/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
trait JsonQueryBuilder {

  def isPrimitiveQueryNumeric(query: PrimitiveQuery) = {
    val xpath = query.xpath
    xpath.count(_.equals('=')) match {
      case 0 => false
      case 1 => Utils.isNumeric(xpath.split("=")(1))
      case _ => throw new RuntimeException("malformed xpath: " + xpath)
    }
  }

  def jsonQueryConverter(query: PrimitiveQuery): String = {
    val xpath = query.xpath
    if (!xpath.contains('=')) {
      "{'" + query.xpath.trim.replaceFirst("/", "").replaceAll("/", ".") + "':{$exists: true}}"
    } else {
      val isNumeric = isPrimitiveQueryNumeric(query)
      val (open, close) = if (isNumeric) ("':", "}") else ("':'", "'}")

      "{$or: [{'" + query.xpath.trim.replaceFirst("/", "").replaceAll("/", ".").replace("=", open) + close + ", " + "{'" + query.xpath.trim.replaceFirst("/", "").replaceAll("/", ".").replace("=", postfixForOldPrimitiveValue + open) + close + "]}"
    }
  }

  import com.thoughtstream.audit.process.QueryOperator._
  def jsonQueryConverter(query: CompositeQuery): String = {
    val queryFormat = "{ %s: [%s,%s] }"
    val operatorString = query.operator match {
      case And => "$and"
      case or => "$or"
    }

    queryFormat.format(operatorString, query.leftQuery.process(jsonQueryConverter), query.rightQuery.process(jsonQueryConverter))
  }

  private def jsonQueryConverter(query: SearchQuery): String = {
    query match {
      case x: PrimitiveQuery => jsonQueryConverter(x)
      case x: CompositeQuery => jsonQueryConverter(x)
    }
  }

  def build(query: SearchQuery): String = query.process(jsonQueryConverter)
}