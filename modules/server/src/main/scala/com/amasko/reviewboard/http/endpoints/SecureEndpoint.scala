package com.amasko.reviewboard
package http
package endpoints

import domain.data.UserID
import services.JWTService

import sttp.tapir.*
import zio.Task

trait SecureEndpoint(jwt: JWTService) extends BaseEndpoint:
  val secureBaseEndpoint = baseEndpoint
    .securityIn(auth.bearer[String]())
    .serverSecurityLogic[UserID, Task] { tok =>
      jwt
        .verifyToken(tok)
        .either
    }

end SecureEndpoint
