package com.amasko.reviewboard
package config

import zio.{Config, IO, Layer, ZIO, ZLayer}
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfigProvider

object Configs:
  val config = deriveConfig[RootConf].mapKey(toKebabCase)

//  def getConfig[A: DeriveConfig]: IO[Config.Error, A] = TypesafeConfigProvider.fromResourcePath().load(deriveConfig[A].mapKey(toKebabCase))

  val layer: Layer[Config.Error, JWTConfig] = ZLayer(
    ZIO.config[JWTConfig](config.map(_.jwt))
  )
