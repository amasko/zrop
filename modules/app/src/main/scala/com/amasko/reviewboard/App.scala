package com.amasko.reviewboard

import components.*

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.LinkHandler

object App {

  val app = div(
    cls := "container",
    Header(),
    Router()
  )
    .amend(LinkHandler.bind) // for internal links

  def main(args: Array[String]): Unit = {
    val appContainer = dom.document.querySelector("#app")
    render(
      appContainer,
      app
    )
  }
}
