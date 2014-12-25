package com.thoughtstream.audit.process

import org.scalatest.{FeatureSpec, GivenWhenThen}
import play.api.libs.json._

/**
 *
 * @author Sateesh
 * @since 25/12/2014
 */
class JsonAuditMessageProcessorFeatureSpec extends FeatureSpec with GivenWhenThen{

  info("As a Generic Audit application owner")
  info("I want to be able to compare xml snapshots of two objects and produce the diff in json format")
  info("So that i can save the json to a json store for easy retrieval.")
  info("Collections can be ignored for now.")

  import JsonAuditMessageProcessor._
  feature("JsonAuditMessageProcessor"){

    scenario("Input contains xml for only new object"){
      val newObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="456" numeric="true"/>
        <primitive name="ssn" value="DB123 12S"/>
      </entity>

      val resultString = process(newObj)
      println(resultString)

      shouldNotHaveOldEntries(resultString)

      val resultJson = Json.parse(resultString)

      assert(resultJson \ "user" \ "eId" === JsString("johnf"))
      assert(resultJson \ "user" \ "ssn" === JsString("DB123 12S"))
      assert(resultJson \ "user" \ "uid" === JsNumber(456))
    }

    scenario("Input xmls contains just one entity with few attributes"){
      val oldObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="123" numeric="true"/>
        <primitive name="ssn" value="DB123 12S"/>
      </entity>
      val newObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="456" numeric="true"/>
        <primitive name="ssn" value="DB123 12S"/>
      </entity>

      val resultString = process(newObj, oldObj)
      println(resultString)

      val resultJson = Json.parse(resultString)

      assert(resultJson \ "user" \ "eId" === JsString("johnf"))
      assert(resultJson \ "user" \ "ssn" === JsString("DB123 12S"))
      assert(resultJson \ "user" \ "uid" === JsNumber(456))
      assert(resultJson \ "user" \ "uid__old" === JsNumber(123))
    }

    scenario("Input xml contains nested entities"){
      val newObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="123" numeric="true"/>
        <entity name="wife">
          <primitive name="eId" value="kattyb"/>
          <primitive name="eType" value="user"/>
        </entity>
      </entity>

      val resultString = process(newObj)
      println(resultString)

      shouldNotHaveOldEntries(resultString)

      val resultJson = Json.parse(resultString)

      assert(resultJson \ "user" \ "eId" === JsString("johnf"))
      assert(resultJson \ "user" \ "uid" === JsNumber(123))
      assert(resultJson \ "user" \ "wife" \ "eId" === JsString("kattyb"))
      assert(resultJson \ "user" \ "wife" \ "eType" === JsString("user"))
    }

    scenario("Input xmls contain nested entities and they are not same."){
      val oldObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="123" numeric="true"/>
        <entity name="primaryContact">
          <primitive name="eId" value="mathewj"/>
          <primitive name="eType" value="user"/>
        </entity>
      </entity>
      val newObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="123" numeric="true"/>
        <entity name="primaryContact">
          <primitive name="eId" value="monkt"/>
          <primitive name="eType" value="user"/>
        </entity>
      </entity>

      val resultString = process(newObj, oldObj)
      println(resultString)

      val resultJson = Json.parse(resultString)

      assert(resultJson \ "user" \ "eId" === JsString("johnf"))
      assert(resultJson \ "user" \ "uid" === JsNumber(123))
      assert(resultJson \ "user" \ "primaryContact" \ "eId" === JsString("monkt"))
      assert(resultJson \ "user" \ "primaryContact" \ "eId__old" === JsString("mathewj"))
    }

    scenario("Input contains value object"){
      val newObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="123" numeric="true"/>
        <valueObject name="address">
          <primitive name="appNo" value="23" numeric="TRUE"/>
          <primitive name="fLine" value="April Street"/>
          <primitive name="postcode" value="MA2 4HL"/>
          <entity name="town">
            <primitive name="eId" value="New York"/>
            <primitive name="eType" value="place"/>
          </entity>
        </valueObject>
      </entity>

      val resultString = process(newObj)
      println(resultString)
      shouldNotHaveOldEntries(resultString)

      val resultJson = Json.parse(resultString)

      assert(resultJson \ "user" \ "eId" === JsString("johnf"))
      assert(resultJson \ "user" \ "address" \ "appNo" === JsNumber(23))
      assert(resultJson \ "user" \ "address" \ "fLine" === JsString("April Street"))
      assert(resultJson \ "user" \ "address" \ "postcode" === JsString("MA2 4HL"))
      assert(resultJson \ "user" \ "address" \ "town" \ "eId" === JsString("New York"))
      assert(resultJson \ "user" \ "address" \ "town" \ "eType" === JsString("place"))
    }

    scenario("Input contains value object that is not same as the old snapshot"){
      val oldObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="123" numeric="true"/>
        <valueObject name="address">
          <primitive name="appNo" value="23" numeric="TRUE"/>
          <primitive name="fLine" value="April Street"/>
          <primitive name="postcode" value="MA2 4HL"/>
          <entity name="town">
            <primitive name="eId" value="New York"/>
            <primitive name="eType" value="place"/>
          </entity>
        </valueObject>
      </entity>
      val newObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="123" numeric="true"/>
        <valueObject name="address">
          <primitive name="appNo" value="25" numeric="TRUE"/>
          <primitive name="fLine" value="May Street"/>
          <primitive name="postcode" value="BA1 4LK"/>
          <entity name="town">
            <primitive name="eId" value="Baltimore"/>
            <primitive name="eType" value="place"/>
          </entity>
        </valueObject>
      </entity>

      val resultString = process(newObj, oldObj)
      println(resultString)

      val resultJson = Json.parse(resultString)

      assert(resultJson \ "user" \ "eId" === JsString("johnf"))
      assert(resultJson \ "user" \ "address" \ "appNo" === JsNumber(25))
      assert(resultJson \ "user" \ "address" \ "fLine" === JsString("May Street"))
      assert(resultJson \ "user" \ "address" \ "postcode" === JsString("BA1 4LK"))
      assert(resultJson \ "user" \ "address" \ "town" \ "eId" === JsString("Baltimore"))
      assert(resultJson \ "user" \ "address" \ "town" \ "eType" === JsString("place"))

      assert(resultJson \ "user" \ "address" \ "appNo__old" === JsNumber(23))
      assert(resultJson \ "user" \ "address" \ "fLine__old" === JsString("April Street"))
      assert(resultJson \ "user" \ "address" \ "postcode__old" === JsString("MA2 4HL"))
      assert(resultJson \ "user" \ "address" \ "town" \ "eId__old" === JsString("New York"))
    }

    scenario("Input contains deep nested value objects and order of elements in xml is different between old & new."){
      val oldObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="123" numeric="true"/>
        <valueObject name="job">
          <primitive name="designation" value="Senior Engineer"/>
          <valueObject name="companyDetails">
            <primitive name="name" value="Ideal Solutions"/>
            <primitive name="established" value="1998" numeric="true"/>
          </valueObject>
        </valueObject>
      </entity>
      val newObj = <entity name="user">
        <valueObject name="job">
          <valueObject name="companyDetails">
            <primitive name="established" value="2010" numeric="true"/>
            <primitive name="name" value="Reactive Solutions"/>
          </valueObject>
          <primitive name="designation" value="Principal Engineer"/>
        </valueObject>
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <primitive name="uid" value="123" numeric="true"/>
      </entity>

      val resultString = process(newObj, oldObj)
      println(resultString)

      val resultJson = Json.parse(resultString)

      assert(resultJson \ "user" \ "eId" === JsString("johnf"))
      assert(resultJson \ "user" \ "job" \ "designation" === JsString("Principal Engineer"))
      assert(resultJson \ "user" \ "job" \ "companyDetails" \ "name" === JsString("Reactive Solutions"))
      assert(resultJson \ "user" \ "job" \ "companyDetails" \ "established" === JsNumber(2010))

      assert(resultJson \ "user" \ "job" \ "designation__old" === JsString("Senior Engineer"))
      assert(resultJson \ "user" \ "job" \ "companyDetails" \ "name__old" === JsString("Ideal Solutions"))
      assert(resultJson \ "user" \ "job" \ "companyDetails" \ "established__old" === JsNumber(1998))
    }

    scenario("Input contains collection of entities"){
      val oldObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <collection name="friends">
          <entity name="1">
            <primitive name="eId" value="paulb"/>
            <primitive name="eType" value="user"/>
          </entity>
          <entity name="2">
            <primitive name="eId" value="johnj"/>
            <primitive name="eType" value="user"/>
          </entity>
        </collection>
      </entity>

      val newObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
        <collection name="friends">
          <entity name="1">
            <primitive name="eId" value="tonya"/>
            <primitive name="eType" value="user"/>
          </entity>
          <entity name="2">
            <primitive name="eId" value="johnj"/>
            <primitive name="eType" value="user"/>
          </entity>
        </collection>
      </entity>

      val resultString = process(newObj, oldObj)
      println(resultString)

      val resultJson = Json.parse(resultString)

      import org.scalatest.Matchers._
      resultJson \ "user" \ "eId" should be (JsString("johnf"))
      resultJson \ "user" \ "friends" \\ "eId" should contain allOf (JsString("johnj"), JsString("tonya"))
    }

    scenario("Input contains collection of value objects"){
      val oldObj = <entity name="user">
        <primitive name="eId" value="johnf"/>
        <primitive name="eType" value="user"/>
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

      val resultString = process(newObj, oldObj)
      println(resultString)

      val resultJson = Json.parse(resultString)

      import org.scalatest.Matchers._
      resultJson \ "user" \ "eId" should be (JsString("johnf"))
      resultJson \ "user" \ "previousAddresses" \\ "postcode" should contain allOf (JsString("MH4 5FD"), JsString("MH4 8FD"))
    }
  }

  def shouldNotHaveOldEntries(input: String): Unit ={
    assert(!input.contains("__old"), "__old entries should not exist when there is no old object snapshot available.")
  }

}
