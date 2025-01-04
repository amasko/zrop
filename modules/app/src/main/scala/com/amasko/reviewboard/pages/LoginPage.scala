package com.amasko.reviewboard
package pages

import components.Anchors
import common.Constants
import core.ZJS.*

import http.requests.Login
import com.raquo.laminar.api.L.{*, given}
import frontroute.BrowserNavigation

case class LoginFormState(
    email: String,
    password: String,
    showStatus: Boolean,
    upstreamError: Option[String]
) extends FormState {
  private val emailError: Option[String] =
    Option.when(!email.matches(Constants.emailRegex))("Email is invalid")

  private val passwordError: Option[String] =
    Option.when(password.isEmpty)("Password can't be empty")

  override val errorList    = List(emailError, passwordError, upstreamError)
  override val maybeSuccess = None

}

object LoginFormState {
  val empty: LoginFormState = LoginFormState(
    "",
    "",
    false,
    None
  )
}

object LoginPage extends FormPage[LoginFormState]("Log In"):
  override def initialState: LoginFormState = LoginFormState.empty

  val submitter = Observer[LoginFormState] { s =>
    if s.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
//      dom.console.log("Current State: " + s)
      callBackend(_.call(_.users.loginEndpoint)(Login(s.email, s.password)))
        .mapBoth(
          { err =>
            stateVar.update(_.copy(showStatus = true, upstreamError = Some(err.getMessage)))
            err
          },
          { token =>
            core.Session.setUserState(token)
            stateVar.set(LoginFormState.empty)
            BrowserNavigation.replaceState("/")
          }
        )
        .runJs
  }

  override def renderChildren() = List(
    renderInput(
      "Email",
      "email-input",
      "text",
      true,
      "Your email",
      (s, v) => s.copy(email = v, showStatus = false, upstreamError = None)
    ),
    renderInput(
      "Password",
      "password-input",
      "password",
      true,
      "Your password",
      (s, v) => s.copy(password = v, showStatus = false, upstreamError = None)
    ),
    button(
      `type` := "button",
      pageTitle,
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    ),
    Anchors.renderNavLink("Forgot password?", "/forgot", "nav-link jvm-item")
  )

end LoginPage
