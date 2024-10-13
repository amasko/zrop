package com.amasko.reviewboard
package http
package controllers

import domain.data.UserID
import zio.*
import sttp.tapir.*
import endpoints.UserEndpoints
import services.UserService
import services.JWTService
import responses.UserResponse

case class UserController private (userService: UserService, jwt: JWTService)
    extends BaseController
    with UserEndpoints:

  val createUser = registerUser.serverLogic[Task] { user =>
    userService
      .registerUser(user.email, user.password)
      .map(r => UserResponse(r.email))
      .either
  }

  val login = loginEndpoint.serverLogic[Task] { req =>
    userService.generateToken(req.email, req.password).either
  }

  val updatePass = updatePasswordEndpoint
    .securityIn(auth.bearer[String]())
    .serverSecurityLogic[UserID, Task] { tok =>
      jwt
        .verifyToken(tok)
//      .map(r => UserResponse(r.email))
        .either
    }
    .serverLogic { useId => req =>
      userService
        .updatePassword(req.email, req.oldPassword, req.newPassword)
        .map(r => UserResponse(r.email))
        .either
    }

  val deleteUser = deleteUserEndpoint
    .securityIn(auth.bearer[String]())
    .serverSecurityLogic[UserID, Task] { tok =>
      jwt.verifyToken(tok).either
    }
    .serverLogic(useId =>
      log =>
        userService
          .deleteUser(log.email, log.password)
          .map(r => UserResponse(r.email))
          .either
    )

  override val routes = List(createUser, login, deleteUser, updatePass)


object UserController:
  def makeZIO = 
    for
      users <- ZIO.service[UserService]
      jwt <- ZIO.service[JWTService]
    yield UserController(users, jwt)