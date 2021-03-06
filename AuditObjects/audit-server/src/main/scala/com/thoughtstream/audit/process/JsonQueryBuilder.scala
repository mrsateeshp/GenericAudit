package com.thoughtstream.audit.process

import com.thoughtstream.audit.Utils
/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
trait JsonQueryBuilder {

  private def getJsonPrimitiveValue(query: PrimitiveQuery): String = {
    val value = query.xpath.trim.split("=")(1)
    if(Utils.isNumeric(value)) {
      value
    } else {

      "{ $regex: '"+value.replace("%",".*")+"', $options: 'i'}"
    }
  }

  private def jsonQueryConverter(query: PrimitiveQuery): String = {
    val xpath = query.xpath.trim
    if (!xpath.contains('=') || xpath.charAt(xpath.length-1).equals('=')) {
      "{'" + query.xpath.trim.replaceFirst("/", "").replaceAll("/", ".") + "':{$exists: true}}"
    } else {
      val jsonXpathWithOutValue = xpath.split("=")(0).trim.replaceFirst("/", "").replaceAll("/", ".")
      val value = getJsonPrimitiveValue(query)

      "{$or: [{'" + jsonXpathWithOutValue +"':" + value + "}" + ", " + "{'" + jsonXpathWithOutValue + postfixForOldPrimitiveValue +"':" + value + "}" + "]}"
    }
  }
  private def jsonQueryConverter(query: CompositeQuery): String = {
    val queryFormat = "{ %s: [%s,%s] }"
    val operatorString = query.operator match {
      case And => "$and"
      case Or => "$or"
    }

    queryFormat.format(operatorString, jsonQueryConverter(query.leftQuery), jsonQueryConverter(query.rightQuery))
  }

  private def jsonQueryConverter(query: SearchQuery): String = {
    query match {
      case x: PrimitiveQuery => jsonQueryConverter(x)
      case x: CompositeQuery => jsonQueryConverter(x)
    }
  }

  def build(query: SearchQuery): String = jsonQueryConverter(query)

  def buildSuggestionsQuery(xpath: String): String = {
    val slashCount = xpath.count(_ == '/')
    val letterCount = xpath.length

    "{xpath:{$regex: '^"+xpath+"', $options:'i'}, slashCount : "+ slashCount +", letterCount: {$gte : "+ letterCount +" } }"
  }
}
