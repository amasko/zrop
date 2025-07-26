package com.amasko.reviewboard
package core

import http.endpoints.{CompanyEndpoints, InviteEndpoints, ReviewEndpoints, UserEndpoints}
import sttp.client3.impl.zio.FetchZioBackend
import sttp.client3.*
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.*
import sttp.client3.httpclient.zio.SttpClient
import sttp.tapir.{Endpoint, PublicEndpoint}

trait BackendClient:
  def companies: CompanyEndpoints
  def users: UserEndpoints
  def reviews: ReviewEndpoints
  def invites: InviteEndpoints

  def call[I, O](endpointFn: BackendClient => PublicEndpoint[I, Throwable, O, Any])(
      input: I
  ): Task[O]

  def callSecure[I, O](endpointFn: BackendClient => Endpoint[String, I, Throwable, O, Any])(
      input: I
  ): Task[O]

case class BackendClientLive(
    config: BackendConfig,
    be: SttpClient,
    interpreter: SttpClientInterpreter
) extends BackendClient:
  self =>

  override val companies: CompanyEndpoints = new CompanyEndpoints {}
  override val users: UserEndpoints        = new UserEndpoints {}
  override val reviews: ReviewEndpoints    = new ReviewEndpoints {}
  override val invites: InviteEndpoints    = new InviteEndpoints {}

  override def call[I, O](
      endpointFn: BackendClient => PublicEndpoint[I, Throwable, O, Any]
  )(input: I): Task[O] =
    val req = interpreter
      .toRequestThrowDecodeFailures(endpointFn(self), Some(uri"${config.url}"))
      .apply(input)
    be.send(req).map(_.body).absolve

  override def callSecure[I, O](
      endpointFn: BackendClient => Endpoint[String, I, Throwable, O, Any]
  )(input: I): Task[O] =
    for
      t <- ZIO
        .from(core.Session.getToken)
        .orElseFail(new IllegalAccessException("Not logged in bitch!"))
      req = interpreter
        .toSecureRequestThrowDecodeFailures(endpointFn(self), Some(uri"${config.url}"))
        .apply(t.token)(input)
      res <- be.send(req).map(_.body).absolve
    yield res

object BackendClientLive:
  type Deps = BackendConfig & SttpClient & SttpClientInterpreter

  val layer: ZLayer[Deps, Nothing, BackendClientLive] =
    ZLayer.fromFunction(BackendClientLive.apply)

  val configuredLayer: ZLayer[Any, Nothing, BackendClientLive] =
    ZLayer.succeed(BackendConfig("http://localhost:8080")) ++ ZLayer.succeed(
      FetchZioBackend()
    ) ++ ZLayer.succeed(SttpClientInterpreter()) >>> layer
