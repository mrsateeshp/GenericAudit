package com.thoughtstream.audit.service

import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import com.thoughtstream.audit.process._
import org.json4s.JValue
import org.json4s.native.JsonMethods._

/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
trait AuditSearchService extends SearchQueryBuilder {
  def search(xpathQuery: String, startIndex: Int = 0, noOfResults: Int = 10): Iterable[JValue] = search(build(xpathQuery), startIndex, noOfResults)

  def search(query: SearchQuery, startIndex: Int, noOfResults: Int): Iterable[JValue]

}

class MongoBasedAuditSearchService
(serviceEndpoint: (String, Int), databaseName: String, collectionName: String = "defCollection")
  extends AuditSearchService with JsonQueryBuilder {

  val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)(collectionName)

  override def search(query: SearchQuery, startIndex: Int, noOfResults: Int): Iterable[JValue] = {
    val jsonQuery = build(query)

    println("Json Query: " + jsonQuery)
    val dbQuery = JSON.parse(jsonQuery).asInstanceOf[DBObject]

    /*todo: example of how to enable data range queries
    val and = new BasicDBList()
    and.add(dbQuery)
    and.add(new BasicDBObject("auditInfo.when", new BasicDBObject("$gte", new Date(1310669017000l)).append("$lt", new Date(1310669019000l))))
    val finalQuery = new BasicDBObject("$and", and)*/

    val cursor = collection.find(dbQuery).skip(startIndex).limit(noOfResults)

    cursor.map(x => parse(x.toString)).toIterable
  }
}
