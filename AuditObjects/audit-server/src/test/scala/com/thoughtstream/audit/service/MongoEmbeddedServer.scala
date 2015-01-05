package com.thoughtstream.audit.service

import com.github.simplyscala.MongoEmbedDatabase

/**
 *
 * @author Sateesh
 * @since 27/12/2014
 */

object MongoEmbeddedServer extends MongoEmbedDatabase{
  val mongoProps = mongoStart(27227)
  def start(){}

//  def stop(){ mongoStop(mongoProps)}
}
