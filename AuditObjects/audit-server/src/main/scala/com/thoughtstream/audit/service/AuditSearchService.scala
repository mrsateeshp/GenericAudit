package com.thoughtstream.audit.service

import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import com.thoughtstream.audit.Utils
import com.thoughtstream.audit.bean.MongoDBInstance
import com.thoughtstream.audit.process._
import play.api.libs.json.{Json, JsValue}

/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
trait AuditSearchService extends SearchQueryBuilder {
  def search(xpathQuery: String, startIndex: Int = 0, noOfResults: Int = 10): Iterable[JsValue] = search(build(xpathQuery), startIndex, noOfResults)

  def search(query: SearchQuery, startIndex: Int, noOfResults: Int): Iterable[JsValue]

}

class MongoBasedAuditSearchService
(mongoDbInstance: MongoDBInstance, collectionName: String = "defCollection")
  extends AuditSearchService with JsonQueryBuilder {

  private val serviceEndpoint = mongoDbInstance.serviceEndpoint
  private val databaseName = mongoDbInstance.databaseName
  private val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)(collectionName)

  override def search(query: SearchQuery, startIndex: Int, noOfResults: Int): Iterable[JsValue] = {
    val jsonQuery = build(query)

    println("Json Query: " + jsonQuery)
    val dbQuery = JSON.parse(jsonQuery).asInstanceOf[DBObject]

    /*todo: example of how to enable data range queries
    val and = new BasicDBList()
    and.add(dbQuery)
    and.add(new BasicDBObject("auditInfo.when", new BasicDBObject("$gte", new Date(1310669017000l)).append("$lt", new Date(1310669019000l))))
    val finalQuery = new BasicDBObject("$and", and)*/

    val cursor = collection.find(dbQuery).skip(startIndex).limit(noOfResults)
    cursor.map(x => Json.parse(x.toString)).toIterable
  }

  def convertToString(obj: Any): String = {
    obj match {
      case x: BasicDBObject => "{" + x.map(f=> f._1 + " => " + convertToString(f._2)).mkString(",") + "}"
      case x: BasicDBList => "{"+x.map(convertToString).mkString(",")+"}"
      case _ => if (Utils.isNumeric(obj.toString)) obj.toString else "'"+obj.toString+"'"
    }
  }
}
