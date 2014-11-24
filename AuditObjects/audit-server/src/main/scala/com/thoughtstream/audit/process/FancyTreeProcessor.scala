package com.thoughtstream.audit.process

import com.thoughtstream.audit.bean.JsNode
import play.api.libs.json._

/**
 *
 * @author Sateesh
 * @since 23/11/2014
 */
object FancyTreeProcessor {

  def transformToPresentableJsNodes(jsValue: JsValue): Seq[JsNode] = {
    jsValue match {
      case x: JsString => Seq(JsNode(x.value))
      case x: JsNumber => Seq(JsNode(x.value.toString()))
      case x: JsBoolean => Seq(JsNode(x.value.toString))
      case JsNull => Seq(JsNode(""))
      case x: JsObject => x.fields.map(y => JsNode(y._1, transformToPresentableJsNodes(y._2))).toSeq
      case x: JsArray => x.value.map(transformToPresentableJsNodes).zip(1 to x.value.size).map(y => JsNode("item_" + y._2, y._1))
    }
  }
}
