package com.amasko.reviewboard
package config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfigProvider


case class RootConf(jwt: JWTConfig)
case class JWTConfig(secret: String, ttl: Long)


object Configs:
  val config = deriveConfig[RootConf].mapKey(toKebabCase)

//  def getConfig[A: DeriveConfig]: IO[Config.Error, A] = TypesafeConfigProvider.fromResourcePath().load(deriveConfig[A].mapKey(toKebabCase))

//  def getLayer[A: Tag](fn: RootConf => A): Layer[Config.Error, A] = ZLayer(ZIO.config[A](config.map(fn)))

  val layer: Layer[Config.Error, JWTConfig] = ZLayer(
    ZIO.config[JWTConfig](config.map(_.jwt))
  )
