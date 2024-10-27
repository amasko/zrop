package com.amasko.reviewboard
package pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object SignUpPage:
  def apply() =
    div(
      cls := "container-fluid",
      div(
        cls := "row",
        div(
          cls := "col-12",
          h1("Sign Up")
        )
      )
    )
