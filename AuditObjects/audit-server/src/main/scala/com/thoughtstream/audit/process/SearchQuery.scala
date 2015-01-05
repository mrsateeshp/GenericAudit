package com.thoughtstream.audit.process

/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
sealed trait SearchQuery {
}

trait SearchQueryBuilder {

  val supportedOperatorsInOrderOfPrecedence: Seq[QueryOperator] = Seq(Or, And)

  private def createQueryForXpath(xpath: String): SearchQuery = {
    require(xpath != null && xpath.trim.length > 1 && xpath.trim.startsWith("/"), "Xpath can not be null or empty or just '/' for the PrimitiveQuery!")

    val firstElement = xpath.substring(1).split("/")(0)
    val nameValuePair = firstElement.split("=")
    if (nameValuePair.length > 1) {
      val (name, value) = (nameValuePair(0), nameValuePair(1))
      new CompositeQuery(PrimitiveQuery("/" + name + "/eId=" + value), And, PrimitiveQuery(xpath.replace("/" + firstElement, "/" + name)))
    } else {
      new PrimitiveQuery(xpath)
    }
  }

  private def createCompositeQuery(queries: Seq[SearchQuery], operator: QueryOperator): SearchQuery ={
    queries match {
      case Seq(x) => x
      case Seq(x, rest@_*) => new CompositeQuery(x, operator, createCompositeQuery(rest,operator))
    }
  }

  private def build(xpathQuery: String, operators: Seq[QueryOperator]): SearchQuery = {
    val tokens = xpathQuery.split(operators.head.regex).filter(!_.trim.isEmpty)
    if(operators.tail.isEmpty) {
      createCompositeQuery(tokens.map(createQueryForXpath),operators.head)
    } else {
      createCompositeQuery(tokens.map(build(_, operators.tail)), operators.head)
    }
  }

  def build(xpathQuery: String): SearchQuery = {
    if (xpathQuery == null || xpathQuery.trim.length < 1 || !xpathQuery.trim.startsWith("/"))
      PrimitiveQuery("/undefined")
    else
      build(xpathQuery.trim.replaceAll(" ",""), supportedOperatorsInOrderOfPrecedence)
  }

  def extractLastQueryPath(query: String): Option[String] = {

    if(query==null || query.isEmpty){
      None
    } else {
      val lastQuery = subStringLastQuery(query)
      validateForSingleQuery(lastQuery)
      Some(lastQuery.replaceAll("=[^=/]*/","/").split("=").head)
    }
  }

  private def subStringLastQuery(currentQuery: String): String = {
    if(currentQuery.trim.isEmpty){
      currentQuery.trim
    } else {
      var resultQuery = currentQuery.trim
      for(operator <- supportedOperatorsInOrderOfPrecedence){
        if(resultQuery.endsWith(operator.sign)){
          resultQuery = ""
        } else {
          resultQuery = resultQuery.split(operator.regex).last
        }
      }
      resultQuery
    }
  }

  private def validateForSingleQuery(currentQuery: String): Unit = {
    require(currentQuery!=null)

    for(operator <- supportedOperatorsInOrderOfPrecedence){
      if(currentQuery.contains(operator.sign)){
        //todo: specific exception
        throw new RuntimeException("more than one query found!")
      }
    }
  }
}

sealed abstract class QueryOperator(val sign: String, val regex: String)

object And extends QueryOperator("&&", "&&")
object Or extends QueryOperator("++", "\\+\\+")

final case class PrimitiveQuery(xpath: String) extends SearchQuery {
  require(xpath != null && xpath.trim.length > 1 && xpath.trim.startsWith("/"), "Xpath can not be null or empty or just '/' for the PrimitiveQuery!")
}

final case class CompositeQuery(leftQuery: SearchQuery, operator: QueryOperator, rightQuery: SearchQuery) extends SearchQuery