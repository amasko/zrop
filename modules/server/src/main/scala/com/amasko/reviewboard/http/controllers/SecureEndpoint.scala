package com.amasko.reviewboard
package http
package controllers

import domain.data.UserID
import services.JWTService
import zio.URIO

trait SecureEndpoint(jwt: JWTService):

  val verify: String => URIO[Any, Either[Throwable, UserID]] = (token: String) => jwt.verifyToken(token).either

end SecureEndpoint
