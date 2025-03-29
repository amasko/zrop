package com.amasko.reviewboard
package components

import scala.scalajs.js.*
import scala.scalajs.*
import scala.scalajs.js.annotation.*

@js.native
@JSGlobal
class Moment extends js.Object:
  def format(): String  = js.native
  def fromNow(): String = js.native

@JSImport("moment", JSImport.Default)
@js.native
object MomentLib extends js.Object:
  def unix(millis: Long): Moment = js.native

object Time:
  def unixToHunanReadable(millis: Long): String =
    MomentLib.unix(millis / 1000).fromNow()
