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

  val buildDir = FileUtils.newTempDir

  //override def shouldUpdate: PartialFunction[Any, Boolean] = {
  //  case CodeUpdate(otherId, _) if otherId != uniqueId => true
  //  case _ => false
  //}

  override def lowPriority = {
    case CodeUpdate(thisId, newCode) =>
      //reRender(false)
      println("calling editAreaLoader.setValue, from: " + thisId)
      partialUpdate(Seq[JsCmd](
        Call("editAreaLoader.setValue", Str("editorpane"), Str(newCode)),
        compile(newCode)))
  }

  def compile(newCode: String): JsCmd = {
    JavacUtil.compile(buildDir, newCode) match {
      case None =>
        SetHtml("console", Text("Successful compliation"))
      case Some(errors) =>
        Seq[JsCmd](
          SetHtml("console", Text("Compilation failure") ++ <br/> ++ errors.map(err => Text(err.error))),
          Call("clearLines")) ++
        errors.map(err => Call("highlightLine", Str(err.lineNum.toString), Str(err.error)).cmd).toSeq
    }
  }

  def render = {
    val code = CollabEditor !? GetCode match { case CodeResp(c) => c }
    bind("e",
      "theID" -> <div id="__ID__">{uniqueId}</div>,
      "textbox" -> SHtml.textarea(code, (c: String) => {}, "id" -> "editorpane"))
  } 
}
