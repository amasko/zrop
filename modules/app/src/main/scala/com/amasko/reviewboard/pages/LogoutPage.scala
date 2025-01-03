package com.amasko.reviewboard
package pages

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html

case class LogoutState() extends FormState:
  override def errorList: List[Option[String]] = Nil

  override def maybeStatus: Option[Either[String, String]] = None
  override def maybeSuccess: Option[String]                = None

  override def showStatus: Boolean = false

object LogoutPage extends FormPage[LogoutState]("Log out"):
  override val stateVar: Var[LogoutState] = Var(LogoutState())

  override def renderChildren(): List[ReactiveHtmlElement[html.Element]] = List(
    div(
      onMountCallback(_ => core.Session.clearState()),
      cls := "centered-status",
      "You've been logged out"
    )
  )
