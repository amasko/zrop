package com.amasko.reviewboard.domain.data

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class Company(
    id: Long,
    slug: String,
    name: String,
    url: String,
    location: Option[String],
    country: Option[String],
    image: Option[String],
    tags: List[String]
)

object Company:
  given JsonCodec[Company] = DeriveJsonCodec.gen[Company]
