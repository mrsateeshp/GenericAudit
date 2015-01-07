package com.thoughtstream.audit

import com.github.simplyscala.MongoEmbedDatabase
import com.thoughtstream.audit.bean.MongoDBInstance

/**
 *
 * @author Sateesh
 * @since 27/12/2014
 */

object MongoEmbeddedServer extends MongoEmbedDatabase{
  val mongoDbInstance = MongoDBInstance("mongodb://localhost:27227/AuditObjects", "AuditObjects")
  val mongoProps = mongoStart(27227)
  def start(){}

  def stop(){ mongoStop(mongoProps)}
}
