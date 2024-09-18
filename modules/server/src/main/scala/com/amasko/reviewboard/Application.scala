package com.amasko.reviewboard

import http.controllers.HealthController
import zio.*
import zio.http.Server
import sttp.tapir.server.ziohttp.*

object Application extends ZIOAppDefault:

    private val serverProgram = for
      controller <- HealthController.makeZIO
        _ <- Server.serve(
          ZioHttpInterpreter(
            ZioHttpServerOptions.default
          ).toHttp(
            controller.health
          )
        )
    yield ()

    override def run =
      serverProgram.provide(
        Server.default
      )
