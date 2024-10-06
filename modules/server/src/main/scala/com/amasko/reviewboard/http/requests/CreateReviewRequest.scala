package com.amasko.reviewboard
package http
package requests

import domain.data.Review

import java.time.Instant

case class CreateReviewRequest(
                              companyId: Long,
                              userId: Long,
  management: Int,
  culture: Int,
  salary: Int,
  benefits: Int,
  wouldRecommend: Int,
  review: String
):
  def toReview(id: Long, i: Instant): Review =
    Review(
      id,
      companyId,
      userId,
      management,
      culture,
      salary,
      benefits,
      wouldRecommend,
      review,
      i,
      i
    )


object CreateReviewRequest:
  import zio.json.{DeriveJsonCodec, JsonCodec}
  given JsonCodec[CreateReviewRequest] = DeriveJsonCodec.gen[CreateReviewRequest]
  
  
