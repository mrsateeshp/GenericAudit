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
  def search(xpathQuery: String, startIndex: Int = 0, noOfResults: Int = 10): Iterable[A] = search(build(xpathQuery), startIndex, noOfResults)

  def search(query: SearchQuery, startIndex: Int, noOfResults: Int): Iterable[A]

}

class MongoBasedAuditSearchService
(mongoDbInstance: MongoDBInstance, collectionName: String = "defCollection")
  extends AuditSearchService[MongoDBSearchResult] with JsonQueryBuilder {

  private val serviceEndpoint = mongoDbInstance.serviceEndpoint
  private val databaseName = mongoDbInstance.databaseName
  private val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)(collectionName)

  override def search(query: SearchQuery, startIndex: Int, noOfResults: Int): Iterable[MongoDBSearchResult] = {
    val jsonQuery = build(query)

    println("Json Query: " + jsonQuery)
    val dbQuery = JSON.parse(jsonQuery).asInstanceOf[DBObject]
    val sortClause = JSON.parse("{auditInfo.when: -1 }").asInstanceOf[DBObject]

    /*todo: example of how to enable data range queries
    val and = new BasicDBList()
    and.add(dbQuery)
    and.add(new BasicDBObject("auditInfo.when", new BasicDBObject("$gte", new Date(1310669017000l)).append("$lt", new Date(1310669019000l))))
    val finalQuery = new BasicDBObject("$and", and)*/

    val cursor = collection.find(dbQuery).sort(sortClause).skip(startIndex).limit(noOfResults)
    cursor.map(x => retrieveResult(Json.parse(x.toString))).toIterable
  }

  //todo: any exception in this block means document is not complaint with the standard schema. need to handle those exceptions
  private def retrieveResult(value: JsValue): MongoDBSearchResult = {
    value match {
      case x: JsObject =>
        val documentList = x.fieldSet.filter(y=> !y._1.equalsIgnoreCase("_id") && !y._1.equalsIgnoreCase("auditInfo"))
        if(documentList.size != 1) throw new RuntimeException("document is not complaint with the standard schema" + value.toString())

        val id = value \ "_id" \ "$oid" match {
          case x: JsString => x.value
          case _ => throw new RuntimeException("Unrecognised Id...")
        }

        val date = value \ "auditInfo" \ "when" \ "$date" match {
          case x: JsString => new DateTime(x.value).toDate
          case _ => throw new RuntimeException("Unrecognised date...")
        }

        val who = value \ "auditInfo" \ "who" match {
          case x: JsString => x.value
          case _ => throw new RuntimeException("Unrecognised who...")
        }

        val auditInfo = AuditInfo(who, date)

        MongoDBSearchResult(id, auditInfo, JsObject(Seq(documentList.head._1 -> documentList.head._2)))
      case _ => MongoDBSearchResult(null, null, value)
    }
  }

  def convertToString(obj: Any): String = {
    obj match {
      case x: BasicDBObject => "{" + x.map(f=> f._1 + " => " + convertToString(f._2)).mkString(",") + "}"
      case x: BasicDBList => "{"+x.map(convertToString).mkString(",")+"}"
      case _ => if (Utils.isNumeric(obj.toString)) obj.toString else "'"+obj.toString+"'"
    }
  }
}

final case class AuditInfo(who: String, when: Date)

final case class MongoDBSearchResult(id: String, auditInfo: AuditInfo, document: JsValue)
