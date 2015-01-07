package com.thoughtstream.audit.service

import java.util.Date

import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import com.thoughtstream.audit.Utils
import com.thoughtstream.audit.bean.MongoDBInstance
import com.thoughtstream.audit.process._
import org.joda.time.DateTime
import play.api.libs.json.{JsString, JsObject, JsValue, Json}

/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
trait AuditSearchService[A] extends SearchQueryBuilder {
  private val startIndex = 0
  private val noOfResults = 10
  def search(xpathQuery: String): Iterable[A] = search(build(xpathQuery), startIndex, noOfResults)

  def search(xpathQuery: String, startIndex: Int, noOfResults: Int): Iterable[A] = search(build(xpathQuery), startIndex, noOfResults)

  def search(xpathQuery: String, fromDate: Date, toDate: Date): Iterable[A] = search(build(xpathQuery), fromDate, toDate, startIndex, noOfResults)

  def search(xpathQuery: String, fromDate: Date, toDate: Date, startIndex: Int, noOfResults: Int): Iterable[A] = search(build(xpathQuery), fromDate, toDate, startIndex, noOfResults)

  def search(query: SearchQuery, startIndex: Int, noOfResults: Int): Iterable[A] = search(query, null, null, startIndex, noOfResults)

  def search(query: SearchQuery, fromDate: Date, toDate: Date, startIndex: Int, noOfResults: Int): Iterable[A]

  def searchQuerySuggestions(query: String): Iterable[String]
}

class MongoBasedAuditSearchService
(mongoDbInstance: MongoDBInstance, collectionName: String = "defCollection", xpathCollectionName: String = "xpaths")
  extends AuditSearchService[MongoDBSearchResult] with JsonQueryBuilder {

  val uri = MongoClientURI(mongoDbInstance.connectionString)
  val mongoClient = MongoClient(uri)
  val mongoDb = mongoClient(mongoDbInstance.databaseName)
  private val collection = mongoDb(collectionName)
  private val xpathCollection = mongoDb(xpathCollectionName)

  override def search(query: SearchQuery, fromDate: Date, _toDate: Date, startIndex: Int, noOfResults: Int): Iterable[MongoDBSearchResult] = {
    val jsonQuery = build(query)

    println("Json Query: " + jsonQuery)
    val dbQuery = JSON.parse(jsonQuery).asInstanceOf[DBObject]
    val sortClause = JSON.parse("{mData.when: -1 }").asInstanceOf[DBObject]
    var completeQuery = dbQuery

    val toDate = if (_toDate != null) new DateTime(_toDate).plusDays(1).toDate else null

    if (fromDate != null || toDate != null) {
      val and = new BasicDBList()
      and.add(dbQuery)
      and.add(buildDateRangePredicate(fromDate, toDate))
      completeQuery = new BasicDBObject("$and", and)
    }

    val cursor = collection.find(completeQuery).sort(sortClause).skip(startIndex).limit(noOfResults)
    cursor.map(x => retrieveResult(Json.parse(x.toString))).toIterable
  }

  private def buildDateRangePredicate(fromDate: Date, toDate: Date): BasicDBObject = {
    val dateRangeQuery = (fromDate, toDate) match {
      case (null, x: Date) =>
        new BasicDBObject("$lte", toDate)
      case (x: Date, null) =>
        new BasicDBObject("$gte", fromDate)
      case (x: Date, y: Date) =>
        new BasicDBObject("$gte", fromDate).append("$lt", toDate)
    }

    new BasicDBObject("mData.when", dateRangeQuery)
  }

  //todo: any exception in this block means document is not complaint with the standard schema. need to handle those exceptions
  private def retrieveResult(value: JsValue): MongoDBSearchResult = {
    value match {
      case x: JsObject =>
        val documentList = x.fieldSet.filter(y => !y._1.equalsIgnoreCase("_id") && !y._1.equalsIgnoreCase("mData"))
        if (documentList.size != 1) throw new RuntimeException("document is not complaint with the standard schema" + value.toString())

        val id = value \ "_id" \ "$oid" match {
          case x: JsString => x.value
          case _ => throw new RuntimeException("Unrecognised Id...")
        }

        val date = value \ "mData" \ "when" \ "$date" match {
          case x: JsString => new DateTime(x.value).toDate
          case _ => throw new RuntimeException("Unrecognised date...")
        }

        val who = value \ "mData" \ "who" match {
          case x: JsString => x.value
          case _ => throw new RuntimeException("Unrecognised who...")
        }

        val oType = value \ "mData" \ "oType" match {
          case x: JsString => x.value
          case _ => throw new RuntimeException("Unrecognised Operation Type...")
        }

        val metaData = AuditMetaData(who, date, oType)

        MongoDBSearchResult(id, metaData, JsObject(Seq(documentList.head._1 -> documentList.head._2)))
      case _ => MongoDBSearchResult(null, null, value)
    }
  }


  override def searchQuerySuggestions(inputQuery: String): Iterable[String] = {
    val queryPathOption = extractLastQueryPath(inputQuery)

    val query = queryPathOption.getOrElse("")
    if (query.isEmpty || query.charAt(0) != '/') {
      Seq()
    } else {

      val jsonQuery = buildSuggestionsQuery(query)
      val dbQuery = JSON.parse(jsonQuery).asInstanceOf[DBObject]

      xpathCollection.distinct("xpath", dbQuery).map(_.toString.substring(query.lastIndexOf("/") + 1)).toIterable
    }
  }

  def convertToString(obj: Any): String = {
    obj match {
      case x: BasicDBObject => "{" + x.map(f => f._1 + " => " + convertToString(f._2)).mkString(",") + "}"
      case x: BasicDBList => "{" + x.map(convertToString).mkString(",") + "}"
      case _ => if (Utils.isNumeric(obj.toString)) obj.toString else "'" + obj.toString + "'"
    }
  }
}

final case class MongoDBSearchResult(id: String, metaData: AuditMetaData, document: JsValue)
