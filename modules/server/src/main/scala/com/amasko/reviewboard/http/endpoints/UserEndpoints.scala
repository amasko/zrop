package com.amasko.reviewboard
package http
package endpoints

import services.JWTService
import domain.data.{UserID, UserToken}
import requests.{ForgottenPassword, Login, PasswordRecovery, RegisterUser, UpdatePassword}
import responses.UserResponse

import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.generic.auto.*


trait UserEndpoints extends SecureEndpoint:

  val registerUser =
    baseEndpoint
      .tag("Users")
      .name("register")
      .description("register a user")
      .in("users")
      .post
      .in(jsonBody[RegisterUser])
      .out(jsonBody[UserResponse])

  val updatePasswordEndpoint =
    secureBaseEndpoint
      .tag("Users")
      .name("updatePassword")
      .description("update a user's password")
      .in("users" / "password")
      .put
      .in(jsonBody[UpdatePassword])
      .out(jsonBody[UserResponse])

  val deleteUserEndpoint =
    secureBaseEndpoint
      .tag("Users")
      .name("delete")
      .description("delete a user")
      .in("users")
      .delete
      .in(jsonBody[Login])
      .out(jsonBody[UserResponse])

  val loginEndpoint =
    baseEndpoint
      .tag("Users")
      .name("login")
      .description("login a user")
      .in("users" / "login")
      .post
      .in(jsonBody[Login])
      .out(jsonBody[UserToken])

  val forgottenPasswordEndpoint =
    baseEndpoint
      .tag("Users")
      .name("forgottenPassword")
      .description("password forgotten")
      .in("users" / "password" / "forgotten")
      .post
      .in(jsonBody[ForgottenPassword])
      .out(jsonBody[UserResponse])

  val recoverPasswordEndpoint =
    baseEndpoint
      .tag("Users")
      .name("recoverPassword")
      .description("recover a password")
      .in("users" / "password" / "recover")
      .post
      .in(jsonBody[PasswordRecovery])
      .out(jsonBody[UserResponse])
