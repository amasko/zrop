package com.amasko.reviewboard
package http
package controllers

import endpoints.HealthEndpoint
import zio.*

class HealthController extends BaseController with HealthEndpoint:

  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("OK"))
  
  val routes = List(health)

end HealthController

object HealthController:
  def makeZIO = ZIO.succeed(new HealthController)