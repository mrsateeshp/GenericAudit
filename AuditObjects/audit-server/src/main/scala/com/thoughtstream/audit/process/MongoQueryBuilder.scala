package com.thoughtstream.audit.process

/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
trait MongoQueryBuilder {
  def getJsonQuery(xpathQuery: String): String
}

trait SimpleMongoQueryBuilder extends MongoQueryBuilder {
  override def getJsonQuery(xpathQuery: String): String = "{'user.uid': {$exists: true}}"
}
