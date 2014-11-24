package com.thoughtstream.audit.bean

/**
 * this is to show the json in the UI using Fantacy Tree
 * @author Sateesh
 * @since 23/11/2014
 */
case class JsNode(title: String, children: Seq[JsNode] = null) {
  val isIsFolder: Boolean = children != null && children.size > 0

  def ++(node: JsNode): Seq[JsNode] = Seq(this, node)

  override def toString: String = {
    if (children == null)
      "{\"title\":\"" + title + "\",\"children\":[],\"isFolder\":false}"
    else
      "{\"title\":\"" + title + "\",\"children\":[" + children.mkString(",") + "],\"isFolder\":true}"
  }
}