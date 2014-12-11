package com.thoughtstream.audit

/**
 *
 * @author Sateesh
 * @since 15/11/2014
 */
object Utils {

  def isNumeric(input: String): Boolean = !input.trim.isEmpty && input.trim().replaceFirst("\\.", "").forall(_.isDigit)
}
