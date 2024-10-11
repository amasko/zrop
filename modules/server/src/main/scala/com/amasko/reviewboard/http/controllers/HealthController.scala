package com.amasko.reviewboard
package http
package controllers

import endpoints.HealthEndpoint
import zio.*

class HealthController extends BaseController with HealthEndpoint:

  val health = healthEndpoint
    .serverLogic[Task](_ => ZIO.succeed("OK").map(Right(_)))
  
  val routes = List(health)

end HealthController

object HealthController:
  def makeZIO = ZIO.succeed(new HealthController)