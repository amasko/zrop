package com.amasko.reviewboard
package pages

import com.amasko.reviewboard.http.requests.UpdatePassword
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html
import core.ZJS.*

case class ChangePasswordState(
    password: String,
    newPassword: String,
    confirmPassword: String,
    override val showStatus: Boolean,
    upstreamStatus: Option[Either[String, String]]
) extends FormState:
  override val errorList: List[Option[String]] = List(
    Option.when(password.isEmpty)("Pass cannot be empty"),
    Option.when(newPassword.isEmpty)("New Pass cannot be empty"),
    Option.when(confirmPassword != newPassword)("Passwords must match")
  ) ++ upstreamStatus.map(_.left.toOption).toList

  override val maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)
end ChangePasswordState

object ChangePasswordState:
  val empty: ChangePasswordState = ChangePasswordState("", "", "", false, None)

object ProfilePage extends FormPage[ChangePasswordState]("Change password"):
  override val stateVar: Var[ChangePasswordState] = Var(ChangePasswordState.empty)

  override def renderChildren(): List[ReactiveHtmlElement[html.Element]] =
    if core.Session.isActive then
      List(
        renderInput(
          "Password",
          "password-input",
          "password",
          true,
          "Your password",
          (s, v) => s.copy(password = v, showStatus = false, upstreamStatus = None)
        ),
        renderInput(
          "New Password",
          "confirm-password-input",
          "password",
          true,
          "New password",
          (s, v) => s.copy(newPassword = v, showStatus = false, upstreamStatus = None)
        ),
        renderInput(
          "Confirm New Password",
          "confirm-password-input",
          "password",
          true,
          "Confirm New password",
          (s, v) => s.copy(confirmPassword = v, showStatus = false, upstreamStatus = None)
        ),
        button(
          `type` := "button",
          pageTitle,
          onClick.preventDefault.mapTo(stateVar.now()) --> submitter
        )
      )
    else
      List(
        div(
          cls := "centered-status",
          "You're not logged in yet!"
        )
      )

  val submitter = Observer[ChangePasswordState] { s =>
    if s.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
      //      dom.console.log("Current State: " + s)
      val email =
        core.Session.getToken.map(_.email).getOrElse("") // will fail on callSecure if empty
      callBackend(
        _.callSecure(_.users.updatePasswordEndpoint)(
          UpdatePassword(email, s.password, s.newPassword)
        )
      )
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
                upstreamStatus = Some(Right("Pass changed!"))
              )
            )
            //            BrowserNavigation.replaceState("/")
          }
        )
        .runJs
  }
