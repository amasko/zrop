package com.amasko.reviewboard
package core

import com.amasko.reviewboard.http.endpoints.CompanyEndpoints
import sttp.client3.impl.zio.FetchZioBackend
import sttp.client3.*
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.*
import sttp.client3.httpclient.zio.SttpClient
import sttp.tapir.PublicEndpoint

trait BackendClient:
  def companies: CompanyEndpoints

  def call[I, O](e: PublicEndpoint[I, Throwable, O, Any], input: I): Task[O]
  def callEndpoint[I, O](endpointFn: BackendClient => PublicEndpoint[I, Throwable, O, Any])(
      input: I
  ): Task[O]

case class BackendClientLive(
    config: BackendConfig,
    be: SttpClient,
    interpreter: SttpClientInterpreter
) extends BackendClient:
  self =>

  override val companies: CompanyEndpoints = new CompanyEndpoints {}

  def call[I, O](e: PublicEndpoint[I, Throwable, O, Any], input: I): Task[O] =
    val req = interpreter.toRequestThrowDecodeFailures(e, Some(uri"${config.url}")).apply(input)
    be.send(req).map(_.body).absolve

  override def callEndpoint[I, O](
      endpointFn: BackendClient => PublicEndpoint[I, Throwable, O, Any]
  )(input: I): Task[O] =
    call(endpointFn(self), input)

object BackendClientLive:
  type Deps = BackendConfig & SttpClient & SttpClientInterpreter

  val layer: ZLayer[Deps, Nothing, BackendClientLive] =
    ZLayer.fromFunction(BackendClientLive.apply)

  val configuredLayer: ZLayer[Any, Nothing, BackendClientLive] =
    ZLayer.succeed(BackendConfig("http://localhost:8080")) ++ ZLayer.succeed(
      FetchZioBackend()
    ) ++ ZLayer.succeed(SttpClientInterpreter()) >>> layer
