package com.amasko.reviewboard
package pages

import common.Constants
import core.ZJS.*
import http.requests.Login

import com.raquo.laminar.api.L.{*, given}
import frontroute.BrowserNavigation
import org.scalajs.dom

case object LoginPage:
  case class State(
      email: String,
      password: String,
      showStatus: Boolean,
      upstreamError: Option[String]
  ) {

    val userEmailError: Option[String] =
      Option.when(email.isEmpty)("User email can't be empty")

    val emailError: Option[String] =
      Option.when(!email.matches(Constants.emailRegex))("Email is invalid")

    val passwordError: Option[String] = Option.when(password.isEmpty)("Password can't be empty")

    val errors             = List(userEmailError, emailError, passwordError, upstreamError).flatten
    val hasErrors: Boolean = errors.nonEmpty

  }

  object State:
    val empty: State = State("", "", false, None)

  val state = Var(State.empty)

  val submitter = Observer[State] { s =>
//    dom.console.log("Current State: " + s) // todo temp debug
    if s.hasErrors then state.update(_.copy(showStatus = true))
    else
//      dom.console.log("Current State: " + s)
      callBackend(_.call(_.users.loginEndpoint)(Login(s.email, s.password)))
        .mapBoth(
          { err =>
            state.update(_.copy(showStatus = true, upstreamError = Some(err.getMessage)))
            err
          },
          { token =>
            // todo set token to local storage
            state.set(State.empty)
            BrowserNavigation.replaceState("/")
          }
        )
        .runJs
  }

  def apply() =
    div(
      cls := "row",
      div(
        cls := "col-md-5 p-0",
        div(
          cls := "logo",
          img(
            src := Constants.logoImg,
            alt := "JVM Logo"
          )
        )
      ),
      div(
        cls := "col-md-7",
        // right
        div(
          cls := "form-section",
          div(cls := "top-section", h1(span("Log In"))),
          children <-- state.signal.map(s =>
            if s.showStatus then s.errors.map(renderError) else Nil
          ),
          renderSuccess(),
          form(
            nameAttr := "signin",
            cls      := "form",
            idAttr   := "form",
            // an input of type text
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
              "Log In",
              onClick.preventDefault.mapTo(state.now()) --> submitter
            )
          )
        )
      )
    )

  private def renderError(error: String) =
    div(
      cls := "page-status-errors",
      error
    )

  private def renderSuccess(show: Boolean = false) =
    if show then
      div(
        cls := "page-status-success",
        child.text <-- state.signal.map(_.toString) // todo temp debug
      )
    else div()

  private def renderInput(
      name: String,
      uid: String,
      kind: String,
      isRequired: Boolean,
      placeholdr: String,
      updateFn: (State, String) => State
  ) =
    div(
      cls := "row",
      div(
        cls := "col-md-12",
        div(
          cls := "form-input",
          label(
            forId := uid,
            cls   := "form-label",
            if isRequired then span("*") else span(),
            name
          ),
          input(
            `type`      := kind,
            cls         := "form-control",
            idAttr      := uid,
            placeholder := placeholdr,
            onInput.mapToValue --> state.updater(updateFn)
          )
        )
      )
    )
