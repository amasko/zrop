package com.amasko.reviewboard

import repositories.*
import services.{PaymentService, *}
import http.HttpApi
import config.Configs
import zio.*
import zio.http.Server
import sttp.tapir.server.ziohttp.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import sttp.tapir.server.interceptor.cors.CORSInterceptor

object Application extends ZIOAppDefault:

  override val bootstrap: zio.ZLayer[ZIOAppArgs, Any, Any] = zio.Runtime
    .setConfigProvider(zio.config.typesafe.TypesafeConfigProvider.fromResourcePath())
//    ++ zio.Runtime.removeDefaultLoggers ++ SLF4J.slf4j

  private val serverProgram = for
//    _ <- Helper.populateCompanies
    routes <- HttpApi.routesZIO
    _ <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default.appendInterceptor(
          CORSInterceptor.default
        )
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
      InviteRepoLive.layer,
      PaymentServiceLive.layer,
      Quill.Postgres.fromNamingStrategy(SnakeCase),
      Quill.DataSource.fromPrefix("db"),
      Configs.layer,
      JWTServiceLive.layer,
      UserRepoLive.layer,
      UserServiceLive.layer,
      EmailServiceLive.layer,
      InviteServiceLive.layer,
      RecoveryTokensRepoLive.layer
    )

end Application

object Helper:

  import domain.data.*
  def populateCompanies =
    for
      a1 <- ZIO.serviceWithZIO[CompanyRepo](
        _.create(
          Company(
            id = -1,
            slug = "facebook",
            name = "Facebook",
            url = "https://facebook.com",
            location = Some("Menlo Park"),
            country = Some("USA"),
            industry = Some("Tech"),
            image = None,
            tags = List("social", "ads", "cloud")
          )
        )
      )
      _ <- ZIO.logInfo(a1.id.toString)
      a2 <- ZIO.serviceWithZIO[CompanyRepo](
        _.create(
          Company(
            id = -1,
            slug = "google",
            name = "Google",
            url = "https://google.com",
            location = Some("Mountain View"),
            country = Some("USA"),
            industry = Some("Tech"),
            image = None,
            tags = List("search", "ads", "cloud")
          )
        )
      )
      _ <- ZIO.logInfo(a2.id.toString)
      a3 <- ZIO.serviceWithZIO[CompanyRepo](
        _.create(
          Company(
            id = -1,
            slug = "amazon",
            name = "Amazon",
            url = "https://amazon.com",
            location = Some("Seattle"),
            country = Some("USA"),
            industry = Some("Tech"),
            image = None,
            tags = List("retail", "cloud", "ads")
          )
        )
      )
      _ <- ZIO.logInfo(a3.id.toString)
      a4 <- ZIO.serviceWithZIO[CompanyRepo](
        _.create(
          Company(
            id = -1,
            slug = "microsoft",
            name = "Microsoft",
            url = "https://microsoft.com",
            location = Some("Redmond"),
            country = Some("USA"),
            industry = Some("Tech"),
            image = None,
            tags = List("software", "cloud", "ads")
          )
        )
      )
      _ <- ZIO.logInfo(a4.id.toString)
    yield ()
