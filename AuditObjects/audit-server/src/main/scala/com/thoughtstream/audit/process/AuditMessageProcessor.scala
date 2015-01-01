package com.thoughtstream.audit.process

/**
 *
 * @author Sateesh
 * @since 14/11/2014
 */
trait AuditMessageProcessor[I,O] {
  def process(newObject: I, oldObject: I): O
}
