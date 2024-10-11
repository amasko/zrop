package com.amasko.reviewboard
package http
package controllers

import sttp.tapir.server.ServerEndpoint

trait BaseController:
  val routes: List[ServerEndpoint[Any, zio.Task]]
