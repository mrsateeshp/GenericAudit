package com.thoughtstream.audit

import org.scalatest.FunSuite

import scala.xml.XML

/**
 *
 * @author Sateesh
 * @since 08/11/2014
 */
class package$Test extends FunSuite {

  test("testing 'findAllAuditableXpaths' with only primitives"){
    val input = <entity name="user">
      <primitive name="id" value="pinnamas"/>
      <primitive name="type" value="user"/> </entity>
    val result = findAllAuditableXpaths(input)

    assert(result.size == 3)

    assert(result.exists(x => x._1.equals("/user")))
    assert(result.get("/user/id").isDefined)
    assert(result.get("/user/type").isDefined)
  }
}
