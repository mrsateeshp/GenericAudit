package com.thoughtstream.audit.process

import com.thoughtstream.audit.domain.{ValueObject, Primitive, Entity}
import org.scalatest.FunSuite

/**
 *
 * @author Sateesh
 * @since 10/11/2014
 */
//todo: still need to write test cases for Collections.
class package$Test extends FunSuite {

  test("testing 'extractVariablesWithXpaths' with only primitives"){
    val input = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="123" numeric="true"/>
      <!--with no "name" attribute-->
      <primitive name="uid" value="123" numeric="true"/>
    </entity>

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 4)
    assert(result.get("/user").get.isInstanceOf[Entity])
    assert(result.get("/user/eId").get match {case Primitive("johnf", false) => true})
    assert(result.get("/user/eType").get === Primitive("user"))
    assert(result.get("/user/uid").get === Primitive("123", true))
  }

  test("testing 'extractVariablesWithXpaths' with nested entities"){
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
    assert(result.get("/user/eId").get match {case Primitive("johnf", false) => true})
    assert(result.get("/user/eType").get === Primitive("user"))
    assert(result.get("/user/uid").get === Primitive("123", true))

    assert(result.get("/user/wife").get.isInstanceOf[Entity])
    assert(result.get("/user/wife/eId").get === Primitive("marryk"))
    assert(result.get("/user/wife/eType").get === Primitive("user"))
  }

  test("testing 'extractVariablesWithXpaths' with nested valueObjects"){
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
    assert(result.get("/user/eId").get match {case Primitive("johnf", false) => true})
    assert(result.get("/user/eType").get === Primitive("user"))
    assert(result.get("/user/uid").get === Primitive("123", true))

    assert(result.get("/user/wife").get.isInstanceOf[Entity])
    assert(result.get("/user/wife/eId").get === Primitive("marryk"))
    assert(result.get("/user/wife/eType").get === Primitive("user"))

    assert(result.get("/user/address").get.isInstanceOf[ValueObject])
    assert(result.get("/user/address/appNo").get === Primitive("23",true))
    assert(result.get("/user/address/fLine").get === Primitive("April Street"))
    assert(result.get("/user/address/postcode").get === Primitive("MA2 4HL"))
    assert(result.get("/user/address/town").get.isInstanceOf[Entity])
    assert(result.get("/user/address/town/eId").get === Primitive("New York"))
    assert(result.get("/user/address/town/eType").get === Primitive("place"))
  }

//  Edge test case
  test("testing 'extractVariablesWithXpaths' with no value for primitive"){
    val input = <entity name="user">
      <primitive name="eId"/>
    </entity>

    val result = extractVariablesWithXpaths(input)

    assert(result.size == 1)
    assert(result.get("/user").get.isInstanceOf[Entity])
  }

}
