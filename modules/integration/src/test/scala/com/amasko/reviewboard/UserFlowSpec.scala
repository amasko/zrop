package com.amasko.reviewboard

import com.amasko.reviewboard.config.JWTConfig
import com.amasko.reviewboard.domain.data.UserToken
import zio.*
import zio.test.*
import zio.test.Assertion.*
import com.amasko.reviewboard.http.controllers.*
import com.amasko.reviewboard.http.requests.*
import com.amasko.reviewboard.http.responses.*
import com.amasko.reviewboard.services.*
import com.amasko.reviewboard.repositories.*
import sttp.client3.*
import sttp.model.Method
import sttp.tapir.generic.auto.*
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.json.*

object UserFlowSpec extends ZIOSpecDefault:

  given MonadError[Task] = new RIOMonadError[Any]

  val backend =
    for
      companyController <- UserController.makeZIO
      backend <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointsRunLogic(companyController.routes)
          // .whenEndpoint(companyController.createEndpoint)
          .backend()
      )
    yield backend

  extension [A: JsonCodec](backend: SttpBackend[Task, Any])
    def sendM[B: JsonCodec](
        method: Method,
        path: String,
        payload: A,
        maybeToken: Option[String] = None
    ): Task[Either[String, B]] =
      basicRequest
        .method(method, uri"$path")
        .body(payload.toJson)
        .auth
        .bearer(maybeToken.getOrElse(""))
        .send(backend)
        .map(_.body)
        .map(_.flatMap(_.fromJson[B]))

  def spec = suite("UserFlowSpec")(
    test("should create a user") {
      for
        b <- backend
        response <- b.sendM[UserResponse](
          Method.POST,
          "/users",
          RegisterUser(email = "test@test.com", password = "testPass")
        )
      yield assertTrue(response == Right(UserResponse("test@test.com")))
    },
    test("should login a user") {
      for
        b <- backend
        response0 <- basicRequest
          .post(uri"/users")
          .body(
            RegisterUser(
              email = "test1@test.com",
              password = "testPass1"
            ).toJson
          )
          .send(b)
        response <- basicRequest
          .post(uri"/users/login")
          .body(
            Login(
              email = "test1@test.com",
              password = "testPass1"
            ).toJson
          )
          .send(b)
        body = response.body.flatMap(_.fromJson[UserToken])
      yield assertTrue(
        response.code.code == 200
          && response0.code.code == 200
          && body.map(_.email) == Right("test1@test.com")
          && body.map(_.token) == Right(
            "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJyZXZpZXdib2FyZC5jb20iLCJpYXQiOjAsImV4cCI6NjA0ODAwLCJ1c2VybmFtZSI6InRlc3QxQHRlc3QuY29tIiwic3ViIjoiMSJ9.CK5gShYqXkVTIe_O4vIKyiq7oULa5bjUAEESgPvq5I0J7qFxQtidQielm2Veazy_00Cn_e1cj1dIRplg2OjoLg"
          )
      )
    }
  )
    .provide(
      UserServiceLive.layer,
      UserRepoLive.layer,
      JWTServiceLive.layer,
      PostgresTestContainer.dsLayer,
      ZLayer.succeed(JWTConfig("secret", 3600))
    )
