package com.amasko.reviewboard
package http
package controllers

import com.amasko.reviewboard.http.requests.Login
import domain.data.{UserID, UserToken}
import zio.*
import endpoints.UserEndpoints
import services.UserService
import services.JWTService
import responses.UserResponse

case class UserController private (userService: UserService, jwt: JWTService)
    extends BaseController
    with UserEndpoints
    with SecureEndpoint(jwt):

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
    .serverSecurityLogic[UserID, Task](verify)
    .serverLogic { useId => req =>
      userService
        .updatePassword(req.email, req.oldPassword, req.newPassword)
        .map(r => UserResponse(r.email))
        .either
    }

  val deleteUser = deleteUserEndpoint
    .serverSecurityLogic[UserID, Task](verify)
    .serverLogic(useId =>
      log =>
        userService
          .deleteUser(log.email, log.password)
          .map(r => UserResponse(r.email))
          .either
    )

  val forgotPassword = forgottenPasswordEndpoint.serverLogic[Task] { req =>
    userService
      .sendPasswordRecoveryToken(req.email)
      .map(r => UserResponse(req.email))
      .either
  }

  val recoverPassword = recoverPasswordEndpoint.serverLogic[Task] { req =>
    userService
      .recoverPassword(req.email, req.token, req.newPassword)
      .map(r => UserResponse(req.email)) // todo throw Unauthorized here?
      .either
  }

  override val routes =
    List(createUser, login, deleteUser, updatePass, recoverPassword, forgotPassword)

object UserController:
  def makeZIO =
    for
      users <- ZIO.service[UserService]
      jwt   <- ZIO.service[JWTService]
    yield UserController(users, jwt)
