package com.amasko.reviewboard
package http
package endpoints

import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.generic.auto.*

import domain.data.{UserID, UserToken}
import requests.RegisterUser
import requests.UpdatePassword
import requests.Login
import responses.UserResponse

trait UserEndpoints extends BaseEndpoint:

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
    baseEndpoint
      .tag("Users")
      .name("updatePassword")
      .description("update a user's password")
      .in("users" / "password")
      .put
      .in(jsonBody[UpdatePassword])
      .out(jsonBody[UserResponse])

  val deleteUserEndpoint =
    baseEndpoint
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
