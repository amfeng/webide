package code.comet

import code.util._

import net.liftweb._
import http._
import actor._
import util._
import js._ 
import JsCmds._
import JE._
import S._
import Helpers._

object CollabEditor extends LiftActor with ListenerManager {
  private var code = ""
  def createUpdate = code
  override def lowPriority = {
    case c: String =>
      code = c
      updateListeners()
  }
}

class CollabEditor extends CometActor with CometListener {
  def registerWith = CollabEditor
  private var code = JavacUtil.defaultCode
  override def lowPriority = {
    case c: String =>
      code = c
      reRender(false)
  }

  def compile(newCode: String): JsCmd = {
    CollabEditor ! newCode
    JavacUtil.compile(newCode) match {
      case None =>
        SetHtml("console", JsRaw("SUCCESS"))
      case Some(errors) =>
        SetHtml("console", JsRaw("Compilation failure:<br/>" + errors.mkString("<br/>")))
    }
  }

  def render = {
    bind("e",
      "textbox" -> SHtml.ajaxTextarea(
          code,
          (c: String) => { 
            println("Callback called with: " + c)
            code = c 
            compile(c) 
          },
          "id" -> "editorpane"),
      "compile" -> SHtml.ajaxButton("compile", () => compile(code)))
  } 
}
