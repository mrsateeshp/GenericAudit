package com.thoughtstream.audit.service

import java.util.Date

import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import com.thoughtstream.audit.bean.MongoDBInstance
import com.thoughtstream.audit.process.{XpathToJsonConverter, JsonAuditMessageProcessor}

import scala.xml.{XML, Elem}

/**
 *
 * @author Sateesh
 * @since 12/11/2014
 */

trait AuditMessageStoringService[A] {
  def save(auditMessage: A): Unit
}

import com.mongodb.casbah.Imports._

case class MongoAuditMessageStoringService(mongoDbInstance: MongoDBInstance, collectionName: String = "defCollection", xpathCollectionName: String = "xpaths") extends AuditMessageStoringService[AuditSaveRequest[XMLDataSnapshot]] {
  private val serviceEndpoint: (String, Int) = mongoDbInstance.serviceEndpoint
  private val databaseName: String = mongoDbInstance.databaseName

  private val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)(collectionName)
  private val xpathCollection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)(xpathCollectionName)

  //todo: enhance it to derive entity type and use to relate to collection
  override def save(request: AuditSaveRequest[XMLDataSnapshot]): Unit = {

    //todo: tactical fix for delete, need to revisit it. for now, reversing the order when new object is not present
    val processorResponse = if(request.dataSnapshot.newObject == <root/>) {
      JsonAuditMessageProcessor.process(request.dataSnapshot.oldObject, request.dataSnapshot.newObject)
    }
    else {
      JsonAuditMessageProcessor.process(request.dataSnapshot.newObject, request.dataSnapshot.oldObject)
    }

    val auditMessage = processorResponse.jsonResponse

    val dbObject = JSON.parse(auditMessage).asInstanceOf[DBObject]
    val metaDataDbObject: BasicDBObject = new BasicDBObject()

    metaDataDbObject.put("who", request.who)
    metaDataDbObject.put("when", request.when)
    metaDataDbObject.put("oType", request.operationType)

    dbObject.put("mData", metaDataDbObject)
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

trait OperationTypeAware {
  def operationType: String
}

final case class XMLDataSnapshot(private val _newObject: Elem = null, private val _oldObject: Elem = null) extends OperationTypeAware {
  require(_newObject != null || _oldObject != null, "at least one input param should be non-null")

  def this(newStr: String, oldStr: String) =
    this(if (newStr != null) XML.loadString(newStr) else null, if (oldStr != null) XML.loadString(oldStr) else null)


  val newObject = if (_newObject == null) <root/> else _newObject
  val oldObject = if (_oldObject == null) <root/> else _oldObject

  override def operationType: String = {
    (_newObject, _oldObject) match {
      case (null, x: Elem) => "Delete"
      case (x: Elem, null) => "Insert"
      case _ => "Update"
    }
  }
}

final case class AuditMetaData(who: String = null, when: Date = null, operationType: String = null)

final case class AuditSaveRequest[A <: OperationTypeAware](dataSnapshot: A, private val metaData: AuditMetaData = null) {
  require(dataSnapshot != null)

  val who = if (metaData == null || metaData.who == null) "unknown" else metaData.who

  def when = if (metaData == null || metaData.when == null) new Date() else metaData.when

  val operationType = if (metaData == null || metaData.operationType == null) dataSnapshot.operationType else metaData.operationType
}
