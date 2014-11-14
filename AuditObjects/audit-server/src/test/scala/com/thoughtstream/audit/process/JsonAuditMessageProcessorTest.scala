package com.thoughtstream.audit.process

import com.thoughtstream.audit.domain.{Collection, ValueObject, Entity, Primitive}
import org.scalatest.FunSuite

/**
 *
 * @author Sateesh
 * @since 12/11/2014
 */
class JsonAuditMessageProcessorTest extends FunSuite {

  import JsonAuditMessageProcessor._

  test("Testing performDiffAndMerge"){
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

    var result = performDiffAndMerge(oldObj, newObj)

    assert(result.size == 6)

    assert(result.head._1.equals("/user"))
    assert(result.head._2.isInstanceOf[Entity])

    result = result.tail
    assert(result.head._1.equals("/user/eId"))
    assert(result.head._2 === Primitive("johnf", false))

    result = result.tail
    assert(result.head._1.equals("/user/eType"))
    assert(result.head._2 === Primitive("user"))

    result = result.tail
    assert(result.head._1.equals("/user/uid"))
    assert(result.head._2 === Primitive("456", true))

    result = result.tail
    assert(result.head._1.equals("/user/uid__old"))
    assert(result.head._2 === Primitive("123", true))

    result = result.tail
    assert(result.head._1.equals("/user/uidWife"))
    assert(result.head._2 === Primitive("123", true))
  }

  test("testing 'extractVariablesWithXpaths' with only primitives") {
    val input = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="123" numeric="true"/>
      <!--with no "name" attribute-->
      <primitive value="123" numeric="true"/>
    </entity>

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 4)
    assert(result.get("/user").get.isInstanceOf[Entity])
    assert(result.get("/user/eId").get match { case Primitive("johnf", false) => true})
    assert(result.get("/user/eType").get === Primitive("user"))
    assert(result.get("/user/uid").get === Primitive("123", true))
  }

  test("testing 'extractVariablesWithXpaths' with nested entities") {
    val input = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="123" numeric="true"/>
      <entity name="wife">
        <primitive name="eId" value="marryk"/>
        <primitive name="eType" value="user"/>
      </entity>
    </entity>

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 7)
    assert(result.get("/user").get.isInstanceOf[Entity])
    assert(result.get("/user/eId").get match { case Primitive("johnf", false) => true})
    assert(result.get("/user/eType").get === Primitive("user"))
    assert(result.get("/user/uid").get === Primitive("123", true))

    assert(result.get("/user/wife").get.isInstanceOf[Entity])
    assert(result.get("/user/wife/eId").get === Primitive("marryk"))
    assert(result.get("/user/wife/eType").get === Primitive("user"))
  }

  test("testing 'extractVariablesWithXpaths' with nested valueObjects") {
    val input = <entity name="user">
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
    </entity>

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 14)
    assert(result.get("/user").get.isInstanceOf[Entity])
    assert(result.get("/user/eId").get match { case Primitive("johnf", false) => true})
    assert(result.get("/user/eType").get === Primitive("user"))
    assert(result.get("/user/uid").get === Primitive("123", true))

    assert(result.get("/user/wife").get.isInstanceOf[Entity])
    assert(result.get("/user/wife/eId").get === Primitive("marryk"))
    assert(result.get("/user/wife/eType").get === Primitive("user"))

    assert(result.get("/user/address").get.isInstanceOf[ValueObject])
    assert(result.get("/user/address/appNo").get === Primitive("23", true))
    assert(result.get("/user/address/fLine").get === Primitive("April Street"))
    assert(result.get("/user/address/postcode").get === Primitive("MA2 4HL"))
    assert(result.get("/user/address/town").get.isInstanceOf[Entity])
    assert(result.get("/user/address/town/eId").get === Primitive("New York"))
    assert(result.get("/user/address/town/eType").get === Primitive("place"))
  }

  //Edge test case
  test("testing 'extractVariablesWithXpaths' with no value for primitive") {
    val input = <entity name="user">
      <primitive name="eId"/>
    </entity>

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 1)
    assert(result.get("/user").get.isInstanceOf[Entity])
  }

  test("testing 'extractVariablesWithXpaths' with collection of primitives") {
    val input = <entity name="user">
      <collection name="yearsOfEmployment">
        <primitive name="1" value="2005" numeric="TRUE"/>
        <primitive name="2" value="2001" numeric="TRUE"/>
      </collection>
    </entity>

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 4)
    assert(result.get("/user").get.isInstanceOf[Entity])
    assert(result.get("/user/yearsOfEmployment").get.isInstanceOf[Collection])
    assert(result.get("/user/yearsOfEmployment/1").get === Primitive("2005", true))
    assert(result.get("/user/yearsOfEmployment/2").get === Primitive("2001", true))
  }

  test("testing 'extractVariablesWithXpaths' with collection of entities") {
    val input = <entity name="user">
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
    </entity>

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 8)
    assert(result.get("/user").get.isInstanceOf[Entity])
    assert(result.get("/user/friends").get.isInstanceOf[Collection])
    assert(result.get("/user/friends/1/eId").get === Primitive("rajeshsv", false))
    assert(result.get("/user/friends/1/eType").get === Primitive("user", false))
    assert(result.get("/user/friends/2/eId").get === Primitive("maheshm", false))
    assert(result.get("/user/friends/2/eType").get === Primitive("user", false))
  }

  test("testing 'extractVariablesWithXpaths' with collection of valueObjects") {
    val input = <entity name="user">
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

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 8)
    assert(result.get("/user").get.isInstanceOf[Entity])
    assert(result.get("/user/previousAddresses").get.isInstanceOf[Collection])
    assert(result.get("/user/previousAddresses/1/firstLine").get === Primitive("23 May St", false))
    assert(result.get("/user/previousAddresses/1/postcode").get === Primitive("MH4 5FD", false))
    assert(result.get("/user/previousAddresses/2/firstLine").get === Primitive("23 June St", false))
    assert(result.get("/user/previousAddresses/2/postcode").get === Primitive("MH4 8FD", false))
  }

  test("testing 'extractVariablesWithXpaths' with complete set of elements") {
    val input = <entity name="user">
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

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 31)
    assert(result.get("/user").get.isInstanceOf[Entity])
    assert(result.get("/user/eId").get match { case Primitive("johnf", false) => true})
    assert(result.get("/user/eType").get === Primitive("user"))
    assert(result.get("/user/uid").get === Primitive("123", true))

    assert(result.get("/user/wife").get.isInstanceOf[Entity])
    assert(result.get("/user/wife/eId").get === Primitive("marryk"))
    assert(result.get("/user/wife/eType").get === Primitive("user"))

    assert(result.get("/user/address").get.isInstanceOf[ValueObject])
    assert(result.get("/user/address/appNo").get === Primitive("23", true))
    assert(result.get("/user/address/fLine").get === Primitive("April Street"))
    assert(result.get("/user/address/postcode").get === Primitive("MA2 4HL"))
    assert(result.get("/user/address/town").get.isInstanceOf[Entity])
    assert(result.get("/user/address/town/eId").get === Primitive("New York"))
    assert(result.get("/user/address/town/eType").get === Primitive("place"))

    assert(result.get("/user/yearsOfEmployment").get.isInstanceOf[Collection])
    assert(result.get("/user/yearsOfEmployment/1").get === Primitive("2005", true))
    assert(result.get("/user/yearsOfEmployment/2").get === Primitive("2001", true))

    assert(result.get("/user/friends").get.isInstanceOf[Collection])
    assert(result.get("/user/friends/1/eId").get === Primitive("rajeshsv", false))
    assert(result.get("/user/friends/1/eType").get === Primitive("user", false))
    assert(result.get("/user/friends/2/eId").get === Primitive("maheshm", false))
    assert(result.get("/user/friends/2/eType").get === Primitive("user", false))

    assert(result.get("/user/previousAddresses").get.isInstanceOf[Collection])
    assert(result.get("/user/previousAddresses/1/firstLine").get === Primitive("23 May St", false))
    assert(result.get("/user/previousAddresses/1/postcode").get === Primitive("MH4 5FD", false))
    assert(result.get("/user/previousAddresses/2/firstLine").get === Primitive("23 June St", false))
    assert(result.get("/user/previousAddresses/2/postcode").get === Primitive("MH4 8FD", false))
  }
}
