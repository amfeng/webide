package code.comet

import code.model._
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

import java.io._

sealed trait CollabCmds

case class ReRender(val actor: CollabEditor) extends CollabCmds

sealed trait ProjectCollabCmds {
  def project: Project
}

/**
 * Indicates that file in proj has been modified, so working copies should
 * sync up with the project
 */
case class FileModified(val project: Project, file: File) extends ProjectCollabCmds

/**
 * Indicates that file in proj has been added, so working copies should sync
 * up with the project
 */
case class FileAdded(val project: Project, file: File) extends ProjectCollabCmds

/**
 * Indicates that file in proj has been deleted, so working copies should do
 * the same 
 */
case class FileDeleted(val project: Project, file: File) extends ProjectCollabCmds

object CollabEditor extends LiftActor with ListenerManager {
  private var cmd: CollabCmds = _
  def createUpdate = cmd
  override def lowPriority = {
    case newCmd: CollabCmds =>
      cmd = newCmd
      updateListeners()
  }
}

class CollabEditor extends CometActor with CometListener {
  def registerWith = CollabEditor

  private lazy val project = Project.get(name.open_!)
  private lazy val workingCopy = project.newWorkingCopy

  override def shouldUpdate: PartialFunction[Any, Boolean] = {
    case ReRender(someActor) if this == someActor => true
    case cmd: ProjectCollabCmds if project == cmd.project => true
    case _ => false
  }

  override def lowPriority = {
    case ReRender(thisActor) =>
      assert(thisActor == this)
      println("Got rerender message")
      //reRender(false)
      partialUpdate(Alert("RERENDER YOURSELF"))
    case FileModified(thisProj: Project, modifiedFile: File) =>
      assert(thisProj == project)
      workingCopy.syncWithProject(modifiedFile)
      reRender(false)
  }

  def compile(): JsCmd = {
    workingCopy.compile() match {
      case Nil =>
        SetHtml("console", JsRaw("Successful complation"))
      case xs =>
        SetHtml("console", JsRaw("<p>Compilation failure:<br/>" + xs.mkString("<br/>") + "</p>"))
    }
  }

  def windowCallback(file: File)(c: String) = {
    println("Callback called for file " + file + " with code: " + c)
    workingCopy.dirtyFiles += ((file -> c))
    //CollabEditor ! newCode
    compile()
  }

  def render = {
    val view = workingCopy.viewOf 
    println("render: workingCopy.viewOf %s".format(view.map(_._1)))
    <div id="tabs">
      <ul>
      {
        view.zipWithIndex.map(t => <li><a href={"#window-%d".format(t._2)}>{t._1._1}</a></li>)
      }
      </ul>
      {
        view.zipWithIndex.map(t =>
          <div id={"#window-%d".format(t._2)}>
          {
            SHtml.ajaxTextarea(t._1._2, windowCallback(t._1._1) _)
          }
          </div>)
      }
    </div> ++
    SHtml.ajaxForm(
      SHtml.text("", (newFileName: String) => {
        // TODO: validate newFileName
        if (!newFileName.endsWith(".java"))
          throw new RuntimeException("name must end in .java: " + newFileName)
        workingCopy.dirtyFiles += ((workingCopy.sourceFile(newFileName) -> ""))
        println("created new file: %s".format(newFileName))
        //reRender(false)
        CollabEditor ! ReRender(this)
      }) ++
      <input type="submit" value="new file"/>
    )
  }

  //def render = {
  //  bind("e",
  //    "textbox" -> SHtml.ajaxTextarea(
  //        code,
  //        (c: String) => { 
  //          println("Callback called with: " + c)
  //          code = c 
  //          compile(c) 
  //        },
  //        "id" -> "editorpane"),
  //    "compile" -> SHtml.ajaxButton("compile", () => compile(code)))
  //} 
}
