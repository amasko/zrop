package com.amasko.reviewboard.domain.data

case class User(id: Long, email: String, hashedPasswd: String):
  def toUserID: UserID = UserID(id, email)

case class UserID(id: Long, email: String)
