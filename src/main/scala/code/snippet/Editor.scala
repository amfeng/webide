package code.snippet

import code.util._

import net.liftweb._
import http._
import util._
import js._ 
import JsCmds._
import JE._
import S._
import Helpers._
import scala.xml._

import code.comet._

class Editor {
  def editor(xhtml: NodeSeq): NodeSeq = {

    def compile(code: String): JsCmd = {
      CollabEditor ! code
      JavacUtil.compile(code) match {
        case None =>
          SetHtml("console", JsRaw("SUCCESS"))
        case Some(errors) =>
          SetHtml("console", JsRaw("Compilation failure:<br/>" + errors.mkString("<br/>")))
      }
    }

    def run(): JsCmd = {
      val output = JavacUtil.run("Foo") 
      println("output is: " + output)
      SetHtml("console", JsRaw("Output: " + output))
    }

    //SHtml.ajaxForm(
    //  bind("e", xhtml,
    //    "textbox" -> SHtml.textarea(JavacUtil.defaultCode, c => code = c, "id" -> "editorpane"),
    //    "run" -> SHtml.ajaxButton("run", () => run()),
    //    "submit" -> SHtml.submit("submit", () => {})) ++
    //  SHtml.hidden(() => compile()))
    var code = JavacUtil.defaultCode
    bind("e", xhtml,
      "textbox" -> SHtml.ajaxTextarea(
          JavacUtil.defaultCode,
          (c: String) => { 
            println("Callback called with: " + c)
            code = c 
            compile(c) 
          },
          "id" -> "editorpane"),
      "compile" -> SHtml.ajaxButton("compile", () => compile(code)))
      


  }
}
