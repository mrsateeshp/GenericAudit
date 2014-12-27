package com.thoughtstream.audit.service

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.mongodb.casbah.MongoConnection
import com.thoughtstream.audit.bean.MongoDBInstance
import com.thoughtstream.audit.process.JsonAuditMessageProcessor._
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{BeforeAndAfter, FeatureSpec, GivenWhenThen}
import play.api.libs.json.Json

/**
 *
 * @author Sateesh
 * @since 27/12/2014
 */

class JsonAuditMessageSaveAndSearchFeatureSpec
  extends FeatureSpec with GivenWhenThen with BeforeAndAfter with MongoEmbedDatabase with StrictLogging {
  info("As a Generic Audit application owner")
  info("I want to save json to a document store")
  info("And I want to be able to search with Xpath based queries.")
  info("searching through collection can be ignored for now")

  var mongoProps: MongodProps = null

  val serviceEndpoint = ("localhost", 27017)
  val databaseName = "AuditObjects"

  val mongoDbInstance = new MongoDBInstance(serviceEndpoint, databaseName)
  val jsonStore = MongoAuditMessageStoringService(mongoDbInstance)
  val searchService = new MongoBasedAuditSearchService(mongoDbInstance)

  before {
    logger.debug("starting the server")
    mongoProps = mongoStart(27017)
    val collection = MongoConnection(serviceEndpoint._1, serviceEndpoint._2)(databaseName)("defCollection")
    collection.drop()
  }

  after {
    logger.debug("stopping the server")
    mongoStop(mongoProps)
  }

  feature("Audit Save & Search Services") {

    scenario("Search for existence of a x path") {
      Given("Empty collection in the document store")
      Given("Saved a json that has few properties")
      val newObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="456" numeric="true"/>
        <primitive name="ssn" value="DB123 12S"/>
      </entity>

      val savedJson: String = process(newObj)
      jsonStore.save(savedJson)
      logger.info("saved json: "+ Json.prettyPrint(Json.parse(savedJson)))

      When("Query with xpaths")
      val result = searchService.search("/user")

      Then("Search results contain the saved json")
      assert(result.size === 1)
      logger.info("search result: "+Json.prettyPrint(result.head))

    }

  }

}
