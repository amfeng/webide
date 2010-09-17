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

import scala.xml._

import java.util.concurrent.atomic._

case class CodeUpdate(from: String, newCode: String)

case object GetCode
case class CodeResp(curCode: String)

object CollabEditor extends LiftActor with ListenerManager {
  private var codeUpdate: Option[CodeUpdate] = None
  def createUpdate = codeUpdate.getOrElse(null)
  override def lowPriority = {
    case c: CodeUpdate =>
      println("Got code update to: " + c.newCode)
      codeUpdate = Some(c)
      updateListeners()
    case GetCode =>
      reply(CodeResp(codeUpdate.map(_.newCode).getOrElse(JavacUtil.defaultCode)))
  }
}

class CollabEditor extends CometActor with CometListener {
  def registerWith = CollabEditor

  //override def shouldUpdate: PartialFunction[Any, Boolean] = {
  //  case CodeUpdate(otherId, _) if otherId != uniqueId => true
  //  case _ => false
  //}

  override def lowPriority = {
    case CodeUpdate(thisId, newCode) =>
      //reRender(false)
      println("calling editAreaLoader.setValue, from: " + thisId)
      partialUpdate(Call("editAreaLoader.setValue", Str("editorpane"), Str(newCode)))
  }

  def compile(newCode: String): JsCmd = {
    CollabEditor ! CodeUpdate(uniqueId, newCode)
    JavacUtil.compile(newCode) match {
      case None =>
        SetHtml("console", Text("SUCCESS"))
      case Some(errors) =>
        SetHtml("console", Text("Compilation failure:<br/>" + errors.mkString("<br/>")))
    }
  }

  def render = {
    val code = CollabEditor !? GetCode match {
      case CodeResp(c) => c
    }
    bind("e",
      "theID" -> <div id="__ID__">{uniqueId}</div>,
      "textbox" -> SHtml.textarea(code, (c: String) => {}, "id" -> "editorpane"),
      "compile" -> SHtml.ajaxButton("compile", () => compile(code)))
  } 
}
