package com.amasko.reviewboard
package common

import scala.scalajs.js.annotation.*
import scala.scalajs.js

object Constants:
  @JSImport("/static/img/fiery-lava 128x128.png", JSImport.Default)
  @js.native
  val logoImg: String = js.native

  @JSImport("/static/img/generic_company.png", JSImport.Default)
  @js.native
  val defaultCompanyLogo: String = js.native

  val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""

  val urlRegex = """^(https?):\/\/(([^:/?#]+)(?::(\d+))?)(\/[^?#]*)?(\?[^#]*)?(#.*)?"""
