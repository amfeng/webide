package code.snippet

import code.comet._

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

class PublishCode {
  def publish(xhtml: NodeSeq): NodeSeq = {
    val fromId = S.param("id").open_!
    val newCode = S.param("code").open_!
    CollabEditor ! CodeUpdate(fromId, newCode)
    <div></div>
  }
}
