package com.amasko.reviewboard
package services

import com.amasko.reviewboard.config.JWTConfig
import zio.*
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import domain.data.{User, UserID, UserToken}

trait JWTService:
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserID]

case class JWTServiceLive private (jwtConfig: JWTConfig) extends JWTService:
  private val secret         = jwtConfig.secret
  private val issuer         = "reviewboard.com"
  private val algorithm      = Algorithm.HMAC512(secret)
  private val ttl            = 3600 * 24 * 7 // 1 week
  private val username_claim = "username"

  private val verifier = JWT
    .require(algorithm)
    .withIssuer(issuer)
    .asInstanceOf[BaseVerification]
    .build(java.time.Clock.systemUTC())

  def verifyToken(token: String): Task[UserID] = for
    decoded <- ZIO.attempt(verifier.verify(token))
    user <- ZIO.attempt(
      UserID(
        decoded.getSubject.toLong,
        decoded.getClaim(username_claim).asString
      )
    )
  yield user

  def createToken(user: User): Task[UserToken] =
    for
      now <- Clock.instant
      expires = now.plusSeconds(ttl)
      jwt <- ZIO.attempt(
        JWT
          .create()
          .withIssuer(issuer)
          .withIssuedAt(now)
          .withExpiresAt(expires)
          .withClaim(username_claim, user.email)
          .withSubject(user.id.toString)
          .sign(algorithm)
      )
    yield UserToken(user.email, jwt, expires.getEpochSecond)

object JWTServiceLive:
  val layer: URLayer[JWTConfig, JWTService] = ZLayer.fromFunction(JWTServiceLive.apply)
