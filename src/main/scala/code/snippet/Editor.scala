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
import code.model._

class Editor {
  def editor(xhtml: NodeSeq): NodeSeq = {
    val name = S.param("name").open_!
    val project = Project.getOrCreate(name)
    <h2>{name}</h2>
    <lift:comet type="CollabEditor" name={name}/>
    <div id="console"/>
  }
}
