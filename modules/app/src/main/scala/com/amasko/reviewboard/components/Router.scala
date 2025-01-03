package com.amasko.reviewboard
package components

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*

import pages.*

object Router {
  def apply() =
    mainTag(
      routes(
        div(
          cls := "container-fluid",
          (pathEnd | path("companies")) {
            CompaniesPage()
          },
          path("login") {
            LoginPage()
          },
          path("signup") {
            SignUpPage()
          },
          path("logout") {
            LogoutPage()
          },
          path("profile") {
            ProfilePage()
          },
          noneMatched {
            NotFoundPage()
          }
        )
//          div(
//            cls := "container-fluid",
//            pathEnd {
//                div(
//                    cls := "row",
//                    div(
//                    cls := "col-12",
//                    h1("Review Board")
//                    )
//                )
//            },
//            path("companies") {
//                div(
//                    cls := "row",
//                    div(
//                    cls := "col-12",
//                    h1("Companies")
//                    )
//                )
//            },
//          )
      )
    )
}
