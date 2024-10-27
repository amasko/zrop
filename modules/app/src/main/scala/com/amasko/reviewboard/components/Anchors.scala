package com.amasko.reviewboard
package components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.StringAsIsCodec
import org.scalajs.dom
import frontroute.*

object Anchors {
  
  def renderNavLink(text: String, location: String, cssClasses: String = "") = {
    a(
      cls := "nav-link",
      href := location,
      text
    )
  }

}
