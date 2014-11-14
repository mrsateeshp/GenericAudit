package com.thoughtstream.audit.process

import scala.xml.Elem

/**
 *
 * @author Sateesh
 * @since 14/11/2014
 */
trait AuditMessageProcessor {
  def process(newObject: Elem, oldObject: Elem = <root/>): String
}
