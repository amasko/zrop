package com.amasko.reviewboard
package http
package endpoints

import domain.errors.HttpErr

import sttp.tapir.*

trait BaseEndpoint:

  val baseEndpoint = endpoint
    .errorOut(statusCode and plainBody[String])
//    .mapErrorOut(HttpErr.encode)(HttpErr.decode)
//    .mapErrorOut(Mapping.from(HttpErr.decode.tupled)(HttpErr.encode))
    .mapErrorOut[Throwable](HttpErr.decode.tupled)(HttpErr.encode)

end BaseEndpoint
