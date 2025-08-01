package com.amasko.reviewboard
package config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfigProvider

case class RootConf(
    jwt: JWTConfig,
    recoveryTokens: RecoveryTokensConfig,
    email: EmailConfig,
    invites: InvitePackConfig,
    payment: PaymentConfig
)

case class JWTConfig(secret: String, ttl: Long)
case class RecoveryTokensConfig(ttl: Long)
case class EmailConfig(host: String, port: Int, user: String, password: String)
case class InvitePackConfig(n: Int)
case class PaymentConfig(
    successUrl: String,
    cancelUrl: String,
    price: String,
    key: String,
    secret: String
)

object Configs:
  private val config = deriveConfig[RootConf].mapKey(toKebabCase)

  type Configuration = JWTConfig & RecoveryTokensConfig & EmailConfig & InvitePackConfig &
    PaymentConfig

//  def getConfig[A: DeriveConfig]: IO[Config.Error, A] = TypesafeConfigProvider.fromResourcePath().load(deriveConfig[A].mapKey(toKebabCase))

  private def getLayer[A: zio.Tag](fn: RootConf => A): Layer[Config.Error, A] = ZLayer(
    ZIO.config[A](config.map(fn))
  )

  val layer: Layer[Config.Error, Configuration] =
    getLayer(_.jwt) ++ getLayer(_.recoveryTokens) ++ getLayer(_.email) ++ getLayer(
      _.invites
    ) ++ getLayer(_.payment)
