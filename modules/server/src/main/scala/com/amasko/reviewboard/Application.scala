package com.amasko.reviewboard

import repositories.*
import services.*
import http.HttpApi
import config.Configs

import zio.*
import zio.http.Server
import sttp.tapir.server.ziohttp.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

object Application extends ZIOAppDefault:

  override val bootstrap: zio.ZLayer[ZIOAppArgs, Any, Any] = zio.Runtime
    .setConfigProvider(zio.config.typesafe.TypesafeConfigProvider.fromResourcePath())
//    ++ zio.Runtime.removeDefaultLoggers ++ SLF4J.slf4j

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
      ReviewServiceLive.layer,
      CompanyRepoLive.layer,
      ReviewRepoLive.layer,
      Quill.Postgres.fromNamingStrategy(SnakeCase),
      Quill.DataSource.fromPrefix("db"),
      Configs.layer,
      JWTServiceLive.layer,
      UserRepoLive.layer,
      UserServiceLive.layer,
      EmailServiceLive.layer,
      RecoveryTokensRepoLive.layer
    )
