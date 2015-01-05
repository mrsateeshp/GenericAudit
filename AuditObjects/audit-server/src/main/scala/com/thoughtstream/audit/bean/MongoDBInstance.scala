package com.thoughtstream.audit.bean

/**
 *
 * @author Sateesh
 * @since 13/11/2014
 */
case class MongoDBInstance(serviceEndpoint: (String, Int), databaseName: String ){
  def this(server: String, port: Int, databaseName: String) = this((server,port),databaseName)
}
