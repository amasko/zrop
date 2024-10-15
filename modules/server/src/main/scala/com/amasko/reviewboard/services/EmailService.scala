package com.amasko.reviewboard
package services

import zio.*

trait EmailService:
  def sendEmail(to: String, subject: String, body: String): Task[Unit]
  def sendRecoveryToken(to: String, token: String): Task[Unit]
end EmailService

case class EmailServiceLive private () extends EmailService:
  def sendEmail(to: String, subject: String, body: String): Task[Unit] = ???
  def sendRecoveryToken(to: String, token: String): Task[Unit]         = ???

object EmailServiceLive:
  val layer = ZLayer.succeed(EmailServiceLive())
