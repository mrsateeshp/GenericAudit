package com.thoughtstream.audit.service

import java.util.Date

import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import com.thoughtstream.audit.bean.MongoDBInstance
import com.thoughtstream.audit.process.{XpathToJsonConverter, JsonAuditMessageProcessor}

import scala.xml.Elem

/**
 *
 * @author Sateesh
 * @since 12/11/2014
 */

trait AuditMessageStoringService[A] {
  def save(auditMessage: A): Unit
}

import com.mongodb.casbah.Imports._

case class MongoAuditMessageStoringService(mongoDbInstance: MongoDBInstance, collectionName: String = "defCollection", xpathCollectionName: String = "xpaths") extends AuditMessageStoringService[XMLAuditRequest] {
  private val serviceEndpoint: (String, Int) = mongoDbInstance.serviceEndpoint
  private val databaseName: String = mongoDbInstance.databaseName

  private val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)(collectionName)
  private val xpathCollection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)(xpathCollectionName)

  //todo: enhance it to derive entity type and use to relate to collection
  override def save(request: XMLAuditRequest): Unit = {
    val processorResponse = JsonAuditMessageProcessor.process(request.newObject, request.oldObject)
    val auditMessage = processorResponse.jsonResponse

    val dbObject = JSON.parse(auditMessage).asInstanceOf[DBObject]
    val auditInfo: BasicDBObject = new BasicDBObject()

    //todo: for now just adding some static data.
    auditInfo.put("who", "unknown")
    auditInfo.put("when", new Date())

    dbObject.put("auditInfo", auditInfo)
    collection += dbObject

    //storing xpaths
    val jsonXpaths = XpathToJsonConverter(processorResponse.xpaths)
    println(jsonXpaths)

    for (jsonXpath <- jsonXpaths) {
      val xpathDbObject = JSON.parse(jsonXpath).asInstanceOf[DBObject]

      if (xpathCollection.find(xpathDbObject).size == 0) {
        xpathCollection += xpathDbObject
      }
    }
  }
}

final case class XMLAuditRequest(newObject: Elem, oldObject: Elem = <root/>)
