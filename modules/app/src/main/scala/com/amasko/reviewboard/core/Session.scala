package com.amasko.reviewboard
package core

import domain.data.UserToken
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
//import scala.org.scalajs.js.*

import scala.scalajs.js.Date

object Session:
  val userState: Var[Option[UserToken]] = Var(Option.empty)

  def isActive: Boolean = userState.now().nonEmpty

  def setUserState(token: UserToken) = {
    userState.set(Some(token))
    Storage.set("userState", token)

  }

  def loadUserState(): Unit =
//    Storage.get[UserToken]("userState").filter(t => t.expires <= new Date().getTime())
    Storage.get[UserToken]("userState").filter(_.expires * 1000 <= Date.now())

    userState
      .set(
        Storage
          .get[UserToken]("userState")
      )
