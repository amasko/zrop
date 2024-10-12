package com.amasko.reviewboard.domain.errors

import sttp.model.StatusCode

case class HttpErr(statusCode: StatusCode, message: String, cause: Throwable)
    extends RuntimeException(message, cause)

object HttpErr:
  def decode(statusCode: StatusCode, message: String): Throwable =
    HttpErr(statusCode, message, RuntimeException(message))
//  def encode(err: HttpErr): (StatusCode, String) = (err.statusCode, err.message)
  def encode(err: Throwable): (StatusCode, String) =
    (StatusCode.InternalServerError, err.getMessage)
