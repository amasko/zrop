package com.amasko.reviewboard
package repositories

import com.amasko.reviewboard.config.RecoveryTokensConfig
import io.getquill.jdbczio.Quill
import io.getquill.*
import zio.*
import domain.data.PasswordRecoveryToken

trait RecoveryTokensRepo:
  def createOrReplaceRecoveryToken(email: String): Task[String]
  def getRecoveryToken(email: String): Task[Option[PasswordRecoveryToken]]
  def deleteRecoveryToken(email: String): Task[Unit]
  def checkToken(email: String, token: String): Task[Boolean]

//todo temp remove extra
case class RecoveryTokensRepoLive private (
    quill: Quill.Postgres[io.getquill.SnakeCase],
    tokenConf: RecoveryTokensConfig
) extends RecoveryTokensRepo:
  import quill.*

  inline given SchemaMeta[PasswordRecoveryToken] =
    schemaMeta[PasswordRecoveryToken]("recovery_tokens")
  inline given InsertMeta[PasswordRecoveryToken] = insertMeta[PasswordRecoveryToken]()
  inline given UpdateMeta[PasswordRecoveryToken] = updateMeta[PasswordRecoveryToken](_.email)

  private val duration = tokenConf.ttl

  private def getRandomUppercaseString(length: Int): Task[String] = {
    // scala.util.Random.alphanumeric.take(length).mkString.toUpperCase
    val chars = ('A' to 'Z') ++ ('0' to '9')
    ZIO.foreach(1 to length)(_ => Random.nextIntBounded(chars.length).map(chars(_))).map(_.mkString)
  }

  private def replaceToken(email: String): Task[String] =
    for
      token <- getRandomUppercaseString(8)
      now   <- Clock.instant
      t <- run(
        query[PasswordRecoveryToken]
          .updateValue(
            lift(PasswordRecoveryToken(email, token, now.plusSeconds(duration).getEpochSecond))
          )
          .returning(r => r)
      )
    yield token

  private def generateToken(email: String): Task[String] =
    for
      token <- getRandomUppercaseString(8)
      now   <- Clock.instant
      t <- run(
        query[PasswordRecoveryToken]
          .insertValue(
            lift(PasswordRecoveryToken(email, token, now.plusSeconds(duration).getEpochSecond))
          )
          .returning(r => r)
      )
    yield token

  def createOrReplaceRecoveryToken(email: String): Task[String] =
    getRecoveryToken(email).flatMap { // also: insert on conflict update
      case Some(_) => replaceToken(email)
      case None    => generateToken(email)
    }

  def getRecoveryToken(email: String): Task[Option[PasswordRecoveryToken]] =
    run(query[PasswordRecoveryToken].filter(_.email == lift(email))).map(_.headOption)

  def deleteRecoveryToken(email: String): Task[Unit] =
    run(query[PasswordRecoveryToken].filter(_.email == lift(email)).delete).unit

  def checkToken(email: String, token: String): Task[Boolean] =
    for
      now <- Clock.instant
      result <- run(
        query[PasswordRecoveryToken]
          .filter(t =>
            t.email == lift(email)
              && t.token == lift(token)
              && t.expiration > lift(now.getEpochSecond)
          )
          .nonEmpty
      )
    yield result

object RecoveryTokensRepoLive:
  val layer = ZLayer.fromFunction(RecoveryTokensRepoLive.apply)
