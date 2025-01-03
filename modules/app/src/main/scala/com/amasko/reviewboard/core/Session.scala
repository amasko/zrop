package com.amasko.reviewboard
package core

import domain.data.UserToken
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
//import scala.org.scalajs.js.*

import scala.scalajs.js.Date

object Session:
  val stateKey                          = "userState"
  val userState: Var[Option[UserToken]] = Var(Option.empty)

  def isActive: Boolean =
    loadUserState()
    userState.now().nonEmpty

  def setUserState(token: UserToken): Unit = {
    userState.set(Some(token))
    Storage.set(stateKey, token)

  }

  def loadUserState(): Unit =
    Storage.get[UserToken](stateKey).filter(_.expires * 1000 <= Date.now())
    userState
      .set(
        Storage
          .get[UserToken](stateKey)
      )

  def clearState(): Unit =
    Storage.remove(stateKey)
    userState.set(Option.empty)

  def getToken: Option[UserToken] =
    loadUserState()
    userState.now()
