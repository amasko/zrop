package com.amasko.reviewboard
package components

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*

import pages.*

object Router {
  val externalUrlBus = EventBus[String]()

  def apply() =
    mainTag(
      onMountCallback(ctx =>
        // Load user state on mount
        externalUrlBus.events.foreach { url =>
          dom.window.location.href = url
        }(using ctx.owner)
      ),
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
          path("changepassword") {
            ChangePasswordPage()
          },
          path("profile") {
            ProfilePage()
          },
          path("forgot") {
            ForgotPasswordPage()
          },
          path("recover") {
            RecoverPasswordPage()
          },
          path("post") {
            CreateCompanyPage()
          },
          path("company" / long) { companyId =>
            CompanyPage(companyId)
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
