package com.thoughtstream.audit.process

/**
 *
 * @author Sateesh
 * @since 02/01/2015
 */
object XpathToJsonConverter {
  def apply(xpath: String): Set[String] = {
    if (xpath == null || xpath.isEmpty) {
      Set()
    } else {

      var result = Set[String]()
      val tokens = xpath.split("/")
      var count = 0
      var xpathFromToken = ""
      for (token <- tokens if !token.isEmpty) {
        xpathFromToken = xpathFromToken + "/" + token
        count += 1
        result += "{xpath:" + "'" + xpathFromToken + "'" + ", slashCount: " + count + ", letterCount: " + xpathFromToken.length + "}"
      }

      result
    }
  }

  def apply(xpaths: Set[String]): Set[String] = {
    var result = Set[String]()

    for (xpath <- xpaths) {
      result ++= apply(xpath)
    }

    result
  }
}
