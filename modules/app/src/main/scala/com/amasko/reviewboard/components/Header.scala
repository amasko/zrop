package com.amasko.reviewboard
package components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.StringAsIsCodec
import org.scalajs.dom

import scala.scalajs.js
import com.amasko.reviewboard.common.*
import com.amasko.reviewboard.domain.data.UserToken

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
                  children <-- core.Session.userState.signal.map(renderNavLinks)
                )
              )
            )
          )
        )
      )
    )

  private def renderNavLinks(maybeUser: Option[UserToken]) =
    val variableLinks =
      if maybeUser.nonEmpty then
        List(
          renderNavLink("Add Company", "/post"),
          renderNavLink("Profile", "/profile"),
          renderNavLink("Log Out", "/logout")
        )
      else
        List(
          renderNavLink("Log In", "/login"),
          renderNavLink("Sign Up", "/signup")
        )

    List(
      renderNavLink("Home", "/"),
      renderNavLink("Companies", "/companies")
    ) ++ variableLinks

  private def renderNavLink(text: String, location: String) =
    li(
      cls := "nav-item",
      Anchors.renderNavLink(text, location, "nav-link jvm-item")
    )

  private def renderLogo() = {
    a(
      cls  := "navbar-brand",
      href := "/",
      img(
//        src := Constants.logoImg, todo fix this shit!
        src := "data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs=",
        alt := "JVM Logo",
        cls := "home-logo"
      )
    )
  }

}
