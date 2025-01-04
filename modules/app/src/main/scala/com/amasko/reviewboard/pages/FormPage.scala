package com.amasko.reviewboard
package pages

import common.Constants
import core.ZJS.*
import http.requests.Login

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontroute.BrowserNavigation
import org.scalajs.dom

trait FormState {

  def showStatus: Boolean
  def maybeSuccess: Option[String]
  def errorList: List[Option[String]]

  def hasErrors: Boolean = errorList.exists(_.isDefined)

  private def maybeError: Option[String] = errorList.find(_.isDefined).flatten

  def maybeStatus: Option[Either[String, String]] =
    maybeError.map(Left(_)).orElse(maybeSuccess.map(Right(_))).filter(_ => showStatus)
}

abstract class FormPage[S <: FormState](val pageTitle: String):
  def initialState: S

  val stateVar: Var[S] = Var(initialState)
  def renderChildren(): List[ReactiveHtmlElement[dom.html.Element]]

  def apply() =
    div(
//      onMountCallback(_ => core.Session.loadUserState()), maybe we need it on reload page?
      onUnmountCallback(_ => stateVar.set(initialState)),
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
          div(cls := "top-section", h1(span(pageTitle))),
          children <-- stateVar.signal
            .map(_.maybeStatus)
            .map(renderStatus)
            .map(_.toList),
          form(
            nameAttr := "signin",
            cls      := "form",
            idAttr   := "form",
            // an input of type text
            renderChildren()
          )
        )
      )
    )

  private def renderStatus(status: Option[Either[String, String]]) = status.map {
    case Left(error) =>
      div(
        cls := "page-status-errors",
        error
      )
    case Right(message) =>
      div(
        cls := "page-status-success",
//        child.text <-- stateVar.signal.map(_.toString) // todo temp debug
        message
      )
  }

  def renderInput(
      name: String,
      uid: String,
      kind: String,
      isRequired: Boolean,
      placeholdr: String,
      updateFn: (S, String) => S
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
            onInput.mapToValue --> stateVar.updater(updateFn)
          )
        )
      )
    )
