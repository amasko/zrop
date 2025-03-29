package com.amasko.reviewboard
package components

import scala.scalajs.js.*
import scala.scalajs.*
import scala.scalajs.js.annotation.*

@JSImport("showdown", JSImport.Default)
@js.native
object MarkdownLib extends js.Object:
  @js.native
  class Converter extends js.Object:
    def makeHtml(text: String): String = js.native

object Markdown:
  def toHtml(text: String): String =
    new MarkdownLib.Converter().makeHtml(text)
