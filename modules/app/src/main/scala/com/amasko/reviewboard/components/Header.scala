package com.amasko.reviewboard
package components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.StringAsIsCodec
import org.scalajs.dom
import scala.scalajs.js

import com.amasko.reviewboard.common.*

object Header {
  def apply() =
    div(
      cls := "container-fluid p-0",
      div(
        cls := "jvm-nav",
        div(
          cls := "container",
          navTag(
            cls := "navbar navbar-expand-lg navbar-light JVM-nav",
            div(
              cls := "container",
              renderLogo(),
              button(
                cls                                         := "navbar-toggler",
                `type`                                      := "button",
                htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
                htmlAttr("data-bs-target", StringAsIsCodec) := "#navbarNav",
                htmlAttr("aria-controls", StringAsIsCodec)  := "navbarNav",
                htmlAttr("aria-expanded", StringAsIsCodec)  := "false",
                htmlAttr("aria-label", StringAsIsCodec)     := "Toggle navigation",
                span(cls := "navbar-toggler-icon")
              ),
              div(
                cls    := "collapse navbar-collapse",
                idAttr := "navbarNav",
                ul(
                  cls := "navbar-nav ms-auto menu align-center expanded text-center SMN_effect-3",
                  renderNavLinks()
                )
              )
            )
          )
        )
      )
    )

  def renderNavLinks() = List(
    renderNavLink("Home", "/"),
    renderNavLink("Companies", "/companies"),
    renderNavLink("Log In", "/login"),
    renderNavLink("Sign Up", "/signup")
  )
  def renderNavLink(text: String, location: String) =
    li(
        cls := "nav-item",
        Anchors.renderNavLink(text, location, "nav-link jvm-item")
    )

  def renderLogo() = {
    a(
      cls := "navbar-brand",
      href := "/",
      img(
        src := Constants.logoImg,
        alt := "JVM Logo",
        cls := "home-logo"
      )
    )
  }






}
