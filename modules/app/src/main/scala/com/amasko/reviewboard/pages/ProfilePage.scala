package com.amasko.reviewboard
package pages

import components.Header
import core.Session
import http.requests.UpdatePassword
import common.Constants
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html
import core.ZJS.*

object ProfilePage:
  def apply() =
    div(
      //      onMountCallback(_ => core.Session.loadUserState()), maybe we need it on reload page?
      cls := "row",
      div(
        cls := "col-md-5 p-0",
        div(
          cls := "logo",
          img(
//              src := Constants.logoImg,
            src := "data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs=",
            alt := "JVM Logo"
          )
        )
      ),
      div(
        cls := "col-md-7",
        // right
        div(
          cls := "form-section",
          child <-- Session.userState.signal.map(user =>
            if user.isEmpty then renderInvalid()
            else renderContent()
          )
        )
      )
    )

  private def renderInvalid() =
    div(
      cls := "top-section",
      h1(span("Not logged in!")),
      p("Please, log in to see your profile.")
    )

  private def renderContent() =
    div(
      cls := "top-section",
      h1(span("Profile")),
      p("You can change your password here."),
      Header.renderNavLink("Change password", "/changepassword")
    )
