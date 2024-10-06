package com.amasko.reviewboard

import com.amasko.reviewboard.repositories.CompanyRepoLive
import com.amasko.reviewboard.services.CompanyServiceLive
import http.HttpApi
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
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
        Server.default,
        CompanyServiceLive.layer,
        CompanyRepoLive.layer,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        Quill.DataSource.fromPrefix("zrop.db")
      )
