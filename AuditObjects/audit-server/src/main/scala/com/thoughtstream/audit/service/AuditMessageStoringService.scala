package com.thoughtstream.audit.service

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
    collection += JSON.parse(auditMessage).asInstanceOf[DBObject]
  }
}
