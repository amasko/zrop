package com.amasko.reviewboard

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object App {

    def main(args: Array[String]): Unit = {
        val appContainer = dom.document.querySelector("#app")
        render(appContainer, div("Hello, world!"))
    }
}
  