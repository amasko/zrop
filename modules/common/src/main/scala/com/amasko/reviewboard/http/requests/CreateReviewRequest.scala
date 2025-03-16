package com.amasko.reviewboard
package http
package requests

import domain.data.Review

import java.time.Instant

case class CreateReviewRequest(
    companyId: Long,
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String
):
  def toReview(userId: Long, i: Instant): Review =
    Review(
      -0L,
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

  def fromReview(r: Review): CreateReviewRequest =
    CreateReviewRequest(
      r.companyId,
      r.management,
      r.culture,
      r.salary,
      r.benefits,
      r.wouldRecommend,
      r.review
    )
