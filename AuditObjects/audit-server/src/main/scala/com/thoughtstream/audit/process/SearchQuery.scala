package com.thoughtstream.audit.process

/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
trait SearchQuery {
  def process[B](processor: SearchQuery => B): B = processor(this)
}

import com.thoughtstream.audit.process.QueryOperator.QueryOperator

trait SearchQueryBuilder {

  def createCompositeQuery(queries: Seq[SearchQuery], operator: QueryOperator): SearchQuery ={
    queries match {
      case Seq(x) => x
      case Seq(x, rest@_*) => new CompositeQuery(x, operator, createCompositeQuery(rest,operator))
    }
  }

  /*def createCompositeQueryFromStrings(queries: Seq[String], operator: QueryOperator): SearchQuery ={
    queries match {
      case x :: Nil => PrimitiveQuery(x)
      case x :: rest => new CompositeQuery(PrimitiveQuery(x), operator, createCompositeQuery(rest,operator))
    }
  }*/

  private def build(xpathQuery: String, operators: Seq[QueryOperator]): SearchQuery = {
    val tokens = xpathQuery.split(operators.head.toString).filter(!_.trim.isEmpty)
    if(operators.tail.isEmpty) {
      createCompositeQuery(tokens.map(new PrimitiveQuery(_)),operators.head)
    } else {
      createCompositeQuery(tokens.map(build(_, operators.tail)), operators.head)
    }
  }

  def build(xpathQuery: String): SearchQuery = {
    if (xpathQuery == null || xpathQuery.trim.length < 1 || !xpathQuery.trim.startsWith("/"))
      UndefinedPrimitiveQuery
    else
      build(xpathQuery.trim.replaceAll(" ",""), Seq(QueryOperator.And, QueryOperator.Or))
  }
}

case class PrimitiveQuery(xpath: String) extends SearchQuery {
  require(xpath != null && xpath.trim.length > 1 && xpath.trim.startsWith("/"), "Xpath can not be null or empty or just '/' for the PrimitiveQuery!")
}

object UndefinedPrimitiveQuery extends PrimitiveQuery("/undefined")

object QueryOperator extends Enumeration {
  type QueryOperator = Value
  val And = Value("&&")
  val Or = Value("\\+\\+")
}


case class CompositeQuery(leftQuery: SearchQuery, operator: QueryOperator, rightQuery: SearchQuery) extends SearchQuery