package com.amasko.reviewboard
package services

import domain.data.Company
import config.EmailConfig

import java.util.Properties
import javax.mail.{Authenticator, PasswordAuthentication, Session}
import javax.mail.internet.{InternetAddress, MimeMessage}
import zio.*

trait EmailService:
  def sendEmail(to: String, subject: String, body: String): Task[Unit]
  def sendRecoveryToken(to: String, token: String): Task[Unit]
  def sentReviewInvite(to: String, company: Company): Task[Unit]
end EmailService

case class EmailServiceLive private (conf: EmailConfig) extends EmailService:
  private val host     = conf.host
  private val port     = conf.port.toString
  private val user     = conf.user
  private val password = conf.password

  def sendEmail(to: String, subject: String, body: String): Task[Unit] =
    for
      props   <- propsResource
      session <- createSession(props)
      message <- createMessage(session, user, to, subject, body)
      _ <- ZIO.attempt {
        message.saveChanges()
        javax.mail.Transport.send(message)
      }
    yield ()

  def sendRecoveryToken(to: String, token: String): Task[Unit] =
    val body =
      s"""
        <html>
          <div style="
              font-family: Arial, sans-serif;
              font-size: 16px;
              color: #333;
              padding: 20px;
              border: 1px solid #ccc;
              border-radius: 5px;
              width: 500px;
              margin: 0 auto;
            ">
            <body>
              <h1>Password recovery token</h1>
              <p>Your password recovery token is: <strong>$token</strong></p>
            </body>
          </div>
        </html>
        """
    sendEmail(to, "Password recovery token", body)

  private def propsResource: Task[Properties] = ZIO.attempt {
    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", host)
    props.put("mail.smtp.port", port)
    props.put("mail.smtp.ssl.trust", host)
    props
  }

  private def createSession(props: Properties): Task[Session] = ZIO.attempt {
    val session = Session.getInstance(
      props,
      new Authenticator {
        override def getPasswordAuthentication: PasswordAuthentication = {
          new PasswordAuthentication(user, password)
        }
      }
    )
    session
  }

  private def createMessage(
      session: Session,
      from: String,
      to: String,
      subject: String,
      body: String
  ): Task[MimeMessage] = ZIO.attempt {
    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(from))
    message.setRecipients(javax.mail.Message.RecipientType.TO, to)
    message.setSubject(subject)
    message.setContent(body, "text/html; charset=utf-8")
    message
  }

  override def sentReviewInvite(to: String, company: Company): Task[Unit] =
    val body =
      s"""
        <html>
          <div style="
              font-family: Arial, sans-serif;
              font-size: 16px;
              color: #333;
              padding: 20px;
              border: 1px solid #ccc;
              border-radius: 5px;
              width: 500px;
              margin: 0 auto;
            ">
            <body>
              <h1>Review Invitation</h1>
              <p>
                You have been invited to review the company <strong>${company.name}</strong>.
                <br />
                Go to <a href="http://localhost:1234/company/${company.id}"</a> to see the details.
              </p>
            </body>
          </div>
        </html>
        """
    sendEmail(to, "Review Invitation", body)

end EmailServiceLive

object EmailServiceLive:
  val layer = ZLayer.fromFunction(EmailServiceLive.apply)

object EmailServiceDemo extends ZIOAppDefault:
//  val emailService = EmailServiceLive(EmailConfig("smtp.gmail.com", 587, "

  val program = for
    service <- ZIO.service[EmailService]
    _       <- service.sendRecoveryToken("batman12@gotham.com", "ABC12345678")
    _       <- Console.printLine("Email sent")
  yield ()

  override def run = program.provide(
    ZLayer.succeed(
      EmailConfig(
        host = "smtp.ethereal.email",
        port = 587,
        user = "stone67@ethereal.email",
        password = "GWCRUKqzSeUwrasVE3"
      )
    ),
    EmailServiceLive.layer
  )
