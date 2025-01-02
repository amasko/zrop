package com.amasko.reviewboard
package pages

import common.Constants
import core.ZJS.*
import http.requests.Login

import com.raquo.laminar.api.L.{*, given}
import frontroute.BrowserNavigation
import org.scalajs.dom

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

  override val stateVar = Var(LoginFormState.empty)

  val submitter = Observer[LoginFormState] { s =>
//    dom.console.log("Current State: " + s) // todo temp debug
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

//  def apply() =
//    div(
//      cls := "row",
//      div(
//        cls := "col-md-5 p-0",
//        div(
//          cls := "logo",
//          img(
//            src := Constants.logoImg,
//            alt := "JVM Logo"
//          )
//        )
//      ),
//      div(
//        cls := "col-md-7",
//        // right
//        div(
//          cls := "form-section",
//          div(cls := "top-section", h1(span("Log In"))),
//          children <-- stateVar.signal.map(s =>
//            if s.showStatus then s.errors.map(renderError) else Nil
//          ),
//          renderSuccess(),
//          form(
//            nameAttr := "signin",
//            cls      := "form",
//            idAttr   := "form",
//            // an input of type text
//            renderChildren()
//          )
//        )
//      )
//    )

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
    )
  )

end LoginPage
