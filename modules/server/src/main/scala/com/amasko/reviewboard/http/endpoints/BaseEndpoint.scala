package com.amasko.reviewboard
package http
package endpoints

import sttp.tapir.*

import domain.errors.HttpErr

trait BaseEndpoint:

  val baseEndpoint = endpoint
    .errorOut(statusCode and plainBody[String])
//    .mapErrorOut(HttpErr.encode)(HttpErr.decode)
//    .mapErrorOut(Mapping.from(HttpErr.decode.tupled)(HttpErr.encode))
    .mapErrorOut[Throwable](HttpErr.decode.tupled)(HttpErr.encode)
  
//  val secureBaseEndpoint = baseEndpoint.in(auth.bearer)

end BaseEndpoint
