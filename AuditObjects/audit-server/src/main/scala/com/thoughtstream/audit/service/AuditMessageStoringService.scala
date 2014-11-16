package com.thoughtstream.audit.service

import java.util.Date

import com.mongodb.BasicDBObject
import com.mongodb.util.JSON

/**
 *
 * @author Sateesh
 * @since 12/11/2014
 */

trait AuditMessageStoringService {
  def save(auditMessage: String): Unit
}

import com.mongodb.casbah.Imports._
trait MongoAuditMessageStoringService extends AuditMessageStoringService {
  val serviceEndpoint: (String, Int)
  val databaseName: String

  private val mongoConn = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)
  private val mongoDB = mongoConn(databaseName)
  private val collectionName: String = "defCollection"

  //todo: enhance it to derive entity type and use to relate to collection
  override def save(auditMessage: String): Unit = {
    val collection = mongoDB(collectionName)
    val dbObject = JSON.parse(auditMessage).asInstanceOf[DBObject]
    val auditInfo: BasicDBObject = new BasicDBObject()

    //todo: for now just adding some static data.
    auditInfo.put("who", "unknown")
    auditInfo.put("when", new Date())

    dbObject.put("auditInfo", auditInfo)
    collection += dbObject
  }
}
