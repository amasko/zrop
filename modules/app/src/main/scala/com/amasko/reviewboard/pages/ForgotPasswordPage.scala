package com.amasko.reviewboard
package pages

import com.amasko.reviewboard.components.Anchors
import com.amasko.reviewboard.http.requests.ForgottenPassword
import common.Constants
import core.ZJS.*
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html.Element

case class ForgotPasswordState(
    email: String = "",
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
) extends FormState:

  override def maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)

  override def errorList: List[Option[String]] = List(
    Option.when(!email.matches(Constants.emailRegex))("Email is invalid")
  ) ++ upstreamStatus.map(_.left.toOption).toList

object ForgotPasswordPage extends FormPage[ForgotPasswordState]("Forgot my password"):
  override def initialState = ForgotPasswordState()

  override def renderChildren(): List[ReactiveHtmlElement[Element]] = List(
    renderInput(
      "Email",
      "email-input",
      "text",
      true,
      "Your email",
      (s, v) => s.copy(email = v, showStatus = false, upstreamStatus = None)
    ),
    button(
      `type` := "button",
      pageTitle,
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    ),
    Anchors.renderNavLink(
      "Have a recovery token?",
      "/recover",
      "auth-link"
    ) // todo: must be a link in email
  )

  private val submitter = Observer[ForgotPasswordState] { s =>
    if s.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
      //      dom.console.log("Current State: " + s)
      callBackend(_.call(_.users.forgottenPasswordEndpoint)(ForgottenPassword(s.email)))
        .mapBoth(
          { err =>
            stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Left(err.getMessage))))
          },
          { _ =>
            stateVar.update(
              _.copy(
                showStatus = true,
                upstreamStatus = Some(Right("Check your email"))
              )
            )
          }
        )
        .merge
        .runJs
  }
