package com.amasko.reviewboard
package pages

import com.amasko.reviewboard.common.Constants
import com.amasko.reviewboard.http.requests.PasswordRecovery
import core.ZJS.*
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html.Element

case class RecoverPasswordState(
    email: String = "",
    token: String = "",
    newPassword: String = "",
    confirmPassword: String = "",
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
) extends FormState:
  override val errorList: List[Option[String]] = List(
    Option.when(!email.matches(Constants.emailRegex))("Email is invalid"),
    Option.when(token.isEmpty)("Token cannot be empty"),
    Option.when(newPassword.isEmpty)("New Pass cannot be empty"),
    Option.when(confirmPassword != newPassword)("Passwords must match")
  ) ++ upstreamStatus.map(_.left.toOption).toList

  override val maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)

object RecoverPasswordPage extends FormPage[RecoverPasswordState]("Recover password"):
  override def initialState = RecoverPasswordState()
  override def renderChildren(): List[ReactiveHtmlElement[Element]] = List(
    renderInput(
      "Email",
      "email-input",
      "text",
      true,
      "Your email",
      (s, v) => s.copy(email = v, showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "Recovery token",
      "token-input",
      "text",
      true,
      "Your req token",
      (s, v) => s.copy(token = v, showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "Password",
      "password-input",
      "password",
      true,
      "Your password",
      (s, v) => s.copy(newPassword = v, showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "Confirm Password",
      "confirm-password-input",
      "password",
      true,
      "Confirm password",
      (s, v) => s.copy(confirmPassword = v, showStatus = false, upstreamStatus = None)
    ),
    button(
      `type` := "button",
      pageTitle,
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    )
  )

  val submitter = Observer[RecoverPasswordState] { s =>
    if s.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
      //      dom.console.log("Current State: " + s)
      callBackend(
        _.call(_.users.recoverPasswordEndpoint)(PasswordRecovery(s.email, s.token, s.newPassword))
      )
        .mapBoth(
          { err =>
            stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Left(err.getMessage))))
            err
          },
          { _ =>
            stateVar.update(
              _.copy(
                showStatus = true,
                upstreamStatus = Some(Right("You can log in now!"))
              )
          }
        )
        .runJs
  }
