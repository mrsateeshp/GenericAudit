package com.thoughtstream.audit.service

import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import com.thoughtstream.audit.process.MongoQueryBuilder
import org.json4s.JValue
import org.json4s.native.JsonMethods._

/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
trait AuditSearchService {
  def search(xpathQuery: String, startIndex: Int, noOfResults: Int): Iterable[JValue]
}

abstract class MongoBasedAuditSearchService
(serviceEndpoint: (String, Int), databaseName: String, collectionName: String = "defCollection")
  extends AuditSearchService with MongoQueryBuilder {

  val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)(collectionName)

  override def search(xpathQuery: String, startIndex: Int = 0, noOfResults: Int = 10): Iterable[JValue] = {

    val cursor = collection.find(JSON.parse(getJsonQuery(xpathQuery)).asInstanceOf[DBObject]).skip(startIndex).limit(noOfResults)

    cursor.map(x => parse(x.toString)).toIterable
  }
}
