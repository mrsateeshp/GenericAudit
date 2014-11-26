package com.thoughtstream.audit.service

import com.github.simplyscala.{MongodProps, MongoEmbedDatabase}
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import com.thoughtstream.audit.User
import com.thoughtstream.audit.bean.MongoDB
import com.thoughtstream.audit.process.{FancyTreeProcessor, JsonAuditMessageProcessor}
import com.thoughtstream.audit.utils.ReflectionUtils
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.xml.XML

/**
 * Integration test
 * @author Sateesh
 * @since 13/11/2014
 */
class MongoAuditMessageStoringServiceTest extends FunSuite with MongoEmbedDatabase with BeforeAndAfter{
  var mongoProps: MongodProps = null

  before {
    mongoProps = mongoStart(27017)   // by default port = 12345 & version = Version.2.3.0
  }                                  // add your own port & version parameters in mongoStart method if you need it

  after { mongoStop(mongoProps) }

  val serviceEndpoint = ("localhost",27017)
  val databaseName = "AuditObjects"

  test("testing with real objects") {
    val processor = JsonAuditMessageProcessor
    val consumer = new MongoDB(serviceEndpoint,databaseName) with MongoAuditMessageStoringService

    val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)("defCollection")

    val user = new User(1,"user1")
    val oldObj = XML.loadString(ReflectionUtils.getEntityXml(user))
    user.setName("user2")
    user.setAddress(new User.Address("22 Dec St", "HA3 6MA"))
    val newObj = XML.loadString(ReflectionUtils.getEntityXml(user))

    consumer.save(processor.process(newObj, oldObj))
    val searchService = new MongoBasedAuditSearchService(serviceEndpoint, databaseName)

    val result = searchService.search("/User")

    assert(result.size === 1)
    println(Json.prettyPrint(result.head))
  }

  test("first record save to & retrieve from MongoDb") {
    val processor = JsonAuditMessageProcessor
    val consumer = new MongoDB(serviceEndpoint,databaseName) with MongoAuditMessageStoringService

    val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)("defCollection")

    val oldObj = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="123" numeric="true"/>
      <primitive name="uidWife" value="123" numeric="true"/>
    </entity>
    val newObj = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="456" numeric="true"/>
      <primitive name="uidWife" value="123" numeric="true"/>
    </entity>

    consumer.save(processor.process(newObj, oldObj))
    val searchService = new MongoBasedAuditSearchService(serviceEndpoint, databaseName)

    var result = searchService.search("/user/uidWife=123&&/user/eId=JOHNF")
    assert(result.size === 1)
    println(Json.prettyPrint(result.head))

    result = searchService.search("/user/uidWife=123&&/user/eId=johnf12")
    assert(result.size === 0)

    result = searchService.search("/user/uidWife=123++/user/eId=johnf")
    assert(result.size === 1)
    println(Json.prettyPrint(result.head))

    result = searchService.search("/user/uidWife=123++/user/eId=johnf12")
    assert(result.size === 1)
    println(Json.prettyPrint(result.head))

    result = searchService.search("/user=johnf")
    assert(result.size === 1)
    println(Json.prettyPrint(result.head))

    result = searchService.search("/user=johnf/uidWife=123++/user/eId=johnf12")
    assert(result.size === 1)
    println(Json.prettyPrint(result.head))

    result = searchService.search("/user=johnf1")
    assert(result.size === 0)

    result = searchService.search("/user=johnf1/uidWife=123")
    assert(result.size === 0)

    // like
    result = searchService.search("/user/eId=joh%")
    assert(result.size === 1)
    println(Json.prettyPrint(result.head))

    result = searchService.search("/user/eId=%hnf++/user/test=abc")
    assert(result.size === 1)
    println(Json.prettyPrint(result.head))

    result = searchService.search("/user/eId=%h%++/user/test=abc")
    assert(result.size === 1)
    println(Json.prettyPrint(result.head))

    result = searchService.search("/user/eId=%qw%++/user/test=abc")
    assert(result.size === 0)
    //tear down
    collection.remove(MongoDBObject())
  }

  test("first record save to MongoDb") {
    val processor = JsonAuditMessageProcessor
    val consumer = new MongoDB(serviceEndpoint,databaseName) with MongoAuditMessageStoringService

    val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)("defCollection")

    val oldObj = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="123" numeric="true"/>
      <primitive name="uidWife" value="123" numeric="true"/>
    </entity>
    val newObj = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="456" numeric="true"/>
      <primitive name="uidWife" value="123" numeric="true"/>
    </entity>

    consumer.save(processor.process(newObj, oldObj))

    val userOptional = collection.findOne(JSON.parse("{'user.uid': {$exists: true}}").asInstanceOf[DBObject])

    val result = Json.parse(userOptional.get.toString)

    assert((result \ "user" \ "eId").as[String] === "johnf")

    //tear down
    collection.remove(MongoDBObject())
  }

  test("first record save to MongoDb and convert to presentable JsNodes") {
    val processor = JsonAuditMessageProcessor
    val consumer = new MongoDB(serviceEndpoint,databaseName) with MongoAuditMessageStoringService

    val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)("defCollection")

    val oldObj = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="123" numeric="true"/>
      <entity name="wife">
        <primitive name="eId" value="marryk"/>
        <primitive name="eType" value="user"/>
      </entity>
      <valueObject name="address">
        <primitive name="appNo" value="23" numeric="TRUE"/>
        <primitive name="fLine" value="April Street"/>
        <primitive name="postcode" value="MA2 4HL"/>
        <entity name="town">
          <primitive name="eId" value="New York"/>
          <primitive name="eType" value="place"/>
        </entity>
      </valueObject>
      <collection name="yearsOfEmployment">
        <primitive name="1" value="2005" numeric="TRUE"/>
        <primitive name="2" value="2001" numeric="TRUE"/>
      </collection>
      <collection name="friends">
        <entity name="1">
          <primitive name="eId" value="rajeshsv"/>
          <primitive name="eType" value="user"/>
        </entity>
        <entity name="2">
          <primitive name="eId" value="maheshm"/>
          <primitive name="eType" value="user"/>
        </entity>
      </collection>
      <collection name="previousAddresses">
        <valueObject name="1">
          <primitive name="firstLine" value="23 May St"/>
          <primitive name="postcode" value="MH4 5FD"/>
        </valueObject>
        <valueObject name="2">
          <primitive name="firstLine" value="23 June St"/>
          <primitive name="postcode" value="MH4 8FD"/>
        </valueObject>
      </collection>
    </entity>
    val newObj = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="123" numeric="true"/>
      <entity name="wife">
        <primitive name="eId" value="juliy"/>
        <primitive name="eType" value="user"/>
      </entity>
      <valueObject name="address">
        <primitive name="appNo" value="23" numeric="TRUE"/>
        <primitive name="fLine" value="April Street"/>
        <primitive name="postcode" value="MA2 4HL"/>
        <entity name="town">
          <primitive name="eId" value="New York"/>
          <primitive name="eType" value="place"/>
        </entity>
      </valueObject>
      <collection name="yearsOfEmployment">
        <primitive name="1" value="2005" numeric="TRUE"/>
        <primitive name="2" value="2001" numeric="TRUE"/>
      </collection>
      <collection name="friends">
        <entity name="1">
          <primitive name="eId" value="rajeshsv"/>
          <primitive name="eType" value="user"/>
        </entity>
        <entity name="2">
          <primitive name="eId" value="maheshm"/>
          <primitive name="eType" value="user"/>
        </entity>
      </collection>
      <collection name="previousAddresses">
        <valueObject name="1">
          <primitive name="firstLine" value="23 May St"/>
          <primitive name="postcode" value="MH4 5FD"/>
        </valueObject>
        <valueObject name="2">
          <primitive name="firstLine" value="23 June St"/>
          <primitive name="postcode" value="MH4 8FD"/>
        </valueObject>
      </collection>
    </entity>

    consumer.save(processor.process(newObj, oldObj))

    val userOptional = collection.findOne(JSON.parse("{'user.uid': {$exists: true}}").asInstanceOf[DBObject])

    val result = Json.parse(userOptional.get.toString)

    println(FancyTreeProcessor.transformToPresentableJsNodes(result).mkString("[",",","]"))

    assert((result \ "user" \ "eId").as[String] === "johnf")

    //tear down
    collection.remove(MongoDBObject())
  }
}
