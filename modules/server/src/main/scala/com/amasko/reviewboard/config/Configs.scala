package com.amasko.reviewboard
package config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfigProvider

case class RootConf(jwt: JWTConfig, recoveryTokens: RecoveryTokensConfig)

case class JWTConfig(secret: String, ttl: Long)
case class RecoveryTokensConfig(ttl: Long)

object Configs:
  private val config = deriveConfig[RootConf].mapKey(toKebabCase)

//  def getConfig[A: DeriveConfig]: IO[Config.Error, A] = TypesafeConfigProvider.fromResourcePath().load(deriveConfig[A].mapKey(toKebabCase))

  private def getLayer[A: zio.Tag](fn: RootConf => A): Layer[Config.Error, A] = ZLayer(
    ZIO.config[A](config.map(fn))
  )

  val layer: Layer[Config.Error, JWTConfig & RecoveryTokensConfig] =
    getLayer(_.jwt) ++ getLayer(_.recoveryTokens)
//    ZLayer(
//     ZIO.config[JWTConfig](config.map(_.jwt))
//  )
