package com.amasko.reviewboard
package http
package controllers

import endpoints.HealthEndpoint
import zio.*

class HealthController extends HealthEndpoint:

  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("OK"))

object HealthController:
  def makeZIO = ZIO.succeed(new HealthController)