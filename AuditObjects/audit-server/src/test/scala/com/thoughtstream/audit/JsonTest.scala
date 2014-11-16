package com.thoughtstream.audit

import com.thoughtstream.audit.domain._
import com.thoughtstream.audit.process._
import org.json4s.Xml.{toJson, toXml}
import org.json4s.JValue
import org.scalatest.FunSuite

import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.util.matching.Regex

/**
 *
 * @author Sateesh
 * @since 11/11/2014
 */
@deprecated
class JsonTest extends FunSuite{

  test("sample") {

  }
  /*def convertToJson(source: Map[String, VariableType]): JValue = {

    val (key,value) = source.head
    val result:JValue = value match {
      case x:Primitive if source.tail.isEmpty => "name" -> x.value
      case x:Primitive =>  ("name" -> x.value) ++ convertToJson(source.tail)
      case x:Entity => key -> convertToJson(source.tail)
    }
    result
  }

  def convertToJson(source: Seq[(String, VariableType)]): JValue = {

    val (key,value) = source.head
    val jsonKey = key.substring(key.lastIndexOf('/')+1)
    val result:JValue = value match {
      case x:Primitive if source.tail.isEmpty =>  jsonKey -> x.value
      case x:Primitive => (jsonKey -> x.value) ~ (jsonKey -> x.value)
//      case x:Primitive => (jsonKey -> x.value) ++ convertToJson(source.tail)
      case x:Entity => jsonKey -> convertToJson(source.tail)
    }
    result
  }

  def doConvertToJsonWithString(source: Seq[(String, VariableType)]): String = {
    val (key,value) = source.head
    val jsonKey = key.substring(key.lastIndexOf('/')+1)
    val result:String = value match {
      case x:Primitive if source.tail.isEmpty =>  "\"" + jsonKey +"\":" + "\"" + x.value + "\""
      case x:Primitive => "\"" + jsonKey +"\":" + "\"" + x.value + "\" , " + doConvertToJsonWithString(source.tail)
      case x:Entity => "\"" + jsonKey +"\": {" + doConvertToJsonWithString(source.tail) + "}"
      case x:ValueObject => "\"" + jsonKey +"\": {" + doConvertToJsonWithString(source.tail) + "}"
      case x:Collection =>
        var collResult = ""

        doConvertToJsonWithString(source.tail)
        "\"" + jsonKey +"\": [{" +  + "}]"
    }
    result
  }
  def convertToJsonWithString(source: Seq[(String, VariableType)]): JValue = {
    val result = doConvertToJsonWithString(source)
    println(result)
    parse("{"+result+"}").
  }

  test("testing Json4s with real code"){


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

    val auditMessageProcessingService = new JsonAuditMessageProcessingService

    val result = auditMessageProcessingService.performDiffAndMerge(oldObj, newObj)

    var resultJson: JValue = null
    for((k,v) <- result) {
      resultJson = (k -> v.toString) ~ (k -> v.toString)
    }

    /*val resultJson = result.map{ w =>
      w._1 -> 1
    }*/

    println(pretty(render(convertToJsonWithString(result))))
  }


  test("testing Json4s lib"){


    val input = <entity name="user">
      <primitive name="eId" value="johnf"/>
      <primitive name="eType" value="user"/>
      <primitive name="uid" value="123" numeric="true"/>
      <!--with no "name" attribute-->
      <primitive name="uid" value="123" numeric="true"/>
    </entity>

    val result = extractVariablesWithXpaths(input)

    var resultJson: JValue = null
    for((k,v) <- result) {
      resultJson = (k -> v.toString) ~ (k -> v.toString)
    }

    /*val resultJson = result.map{ w =>
      w._1 -> 1
    }*/

    println(pretty(render(convertToJson(result.tail))))
  }

  test("testing Json4s lib 123"){
    val xml = <primitive>2004</primitive>
          <primitive>2005</primitive>
          <primitive>2006</primitive>;

    val json = toJson(xml)
    println(pretty(render(json)))
  }*/
}
