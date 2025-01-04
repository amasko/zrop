package com.amasko.reviewboard
package pages

import com.amasko.reviewboard.http.requests.RegisterUser
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import common.Constants
import core.ZJS.*
import org.scalajs.dom.HTMLDivElement

case class SignupFormState(
    email: String,
    password: String,
    confirmPassword: String,
    upstreamStatus: Option[Either[String, String]],
    showStatus: Boolean
) extends FormState:

  private val emailError: Option[String] =
    Option.when(!email.matches(Constants.emailRegex))("Email is invalid")

  private val passwordError: Option[String] =
    Option.when(password.isEmpty)("Password can't be empty")
  private val confPasswordError: Option[String] =
    Option.when(password != confirmPassword)("Confirmation Password must match")

  val errorList: List[Option[String]] =
    List(emailError, passwordError, confPasswordError) ++ upstreamStatus.map(_.left.toOption).toList

  val maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)

object SignupFormState:
  val empty: SignupFormState = SignupFormState("", "", "", None, false)

object SignUpPage extends FormPage[SignupFormState](pageTitle = "Sign Up"):
  override def initialState: SignupFormState = SignupFormState.empty
  override def renderChildren() = List(
    renderInput(
      "Email",
      "email-input",
      "text",
      true,
      "Your email",
      (s, v) => s.copy(email = v, showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "Password",
      "password-input",
      "password",
      true,
      "Your password",
      (s, v) => s.copy(password = v, showStatus = false, upstreamStatus = None)
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

  val submitter = Observer[SignupFormState] { s =>
    if s.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
      //      dom.console.log("Current State: " + s)
      callBackend(_.call(_.users.registerUser)(RegisterUser(s.email, s.password)))
        .mapBoth(
          { err =>
            stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Left(err.getMessage))))
            err
          },
          { _ =>
//            core.Session.setUserState(token)
            stateVar.update(
              _.copy(
                showStatus = true,
                upstreamStatus = Some(Right("Account Created: log in or die!"))
              )
            )
//            BrowserNavigation.replaceState("/")
          }
        )
        .runJs
  }
