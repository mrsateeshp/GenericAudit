package com.thoughtstream.audit.service

import com.thoughtstream.audit.bean.MongoDB
import com.thoughtstream.audit.process.JsonAuditMessageProcessor
import org.scalatest.FunSuite

/**
 * Integration test
 * @author Sateesh
 * @since 13/11/2014
 */
class MongoAuditMessageStoringServiceTest extends FunSuite {


  test("first record save to MongoDb") {
    val processor = JsonAuditMessageProcessor
    val consumer = new MongoDB(("localhost",27017),"AuditObjects") with MongoAuditMessageStoringService

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
  }
}
