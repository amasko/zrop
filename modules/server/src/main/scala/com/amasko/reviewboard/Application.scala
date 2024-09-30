package com.amasko.reviewboard

import http.HttpApi
import zio.*
import zio.http.Server
import sttp.tapir.server.ziohttp.*

object Application extends ZIOAppDefault:

    private val serverProgram = for
      routes <- HttpApi.routesZIO
        _ <- Server.serve(
          ZioHttpInterpreter(
            ZioHttpServerOptions.default
          ).toHttp(
            routes
          )
        )
    yield ()

    override def run =
      serverProgram.provide(
        Server.default
      )
