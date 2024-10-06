package com.amasko.reviewboard
package domain
package data

import java.time.Instant

import zio.json.{DeriveJsonCodec, JsonCodec}

case class Review(
                   id: Long, // PK 
                   companyId: Long, // FK
                   userId: Long, // FK
                   // scores
                   management: Int,
                   culture: Int,
                   salary: Int,
                   benefits: Int,
                   wouldRecommend: Int,
                   review: String,
                   created: Instant,
                   updated: Instant
                 )

object Review:
  given JsonCodec[Review] = DeriveJsonCodec.gen[Review]
  