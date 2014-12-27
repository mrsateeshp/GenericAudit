package com.thoughtstream.audit.service

import java.util.Date

import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import com.thoughtstream.audit.bean.MongoDBInstance

/**
 *
 * @author Sateesh
 * @since 12/11/2014
 */

trait AuditMessageStoringService {
  def save(auditMessage: String): Unit
}

import com.mongodb.casbah.Imports._
case class MongoAuditMessageStoringService(mongoDbInstance: MongoDBInstance, collectionName: String = "defCollection") extends AuditMessageStoringService {
  private val serviceEndpoint: (String, Int) = mongoDbInstance.serviceEndpoint
  private val databaseName: String = mongoDbInstance.databaseName

  private val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)(collectionName)

  //todo: enhance it to derive entity type and use to relate to collection
  override def save(auditMessage: String): Unit = {
    val dbObject = JSON.parse(auditMessage).asInstanceOf[DBObject]
    val auditInfo: BasicDBObject = new BasicDBObject()

    //todo: for now just adding some static data.
    auditInfo.put("who", "unknown")
    auditInfo.put("when", new Date())

    dbObject.put("auditInfo", auditInfo)
    collection += dbObject
  }
}
