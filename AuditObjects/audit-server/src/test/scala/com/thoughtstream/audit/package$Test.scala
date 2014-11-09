package com.thoughtstream.audit

import org.scalatest.FunSuite

import scala.xml.XML

/**
 *
 * @author Sateesh
 * @since 08/11/2014
 */
class package$Test extends FunSuite {

  test("testing indexes"){
    val input = XML.loadFile("C:\\Sateesh\\Dropbox\\Development\\AuditObjects\\audit-server\\src\\main\\resources\\AuditGenericSchema.xml")
    println(findAllAuditableXpaths(input))
  }

  test("testing primitive values"){
    val input = XML.loadFile("C:\\Sateesh\\Dropbox\\Development\\AuditObjects\\audit-server\\src\\main\\resources\\AuditGenericSchema.xml")
    println(findAllPrimitiveValuesWithXpath(input))
  }
}
