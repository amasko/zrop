package com.amasko.reviewboard
package http
package controllers

import services.ReviewService
import domain.data.Review
import endpoints.ReviewEndpoints

import zio.*

class ReviewController private (service: ReviewService) extends ReviewEndpoints:

  val createReview = createEndpoint
    .serverLogicSuccess[Task](request =>
      for
        now <- Clock.instant
        review = request.toReview(0L, now)
        created <- service.create(review)
      yield created
    )

  val getAllReviews = getAllEndpoint
    .serverLogicSuccess[Task](_ => service.getReviews)

  val getReviewById = getById
    .serverLogicSuccess[Task](id =>
      id.toLongOption match
        case Some(id) => service.getReview(id)
        case None     => ZIO.fail(new Exception("Invalid id"))
    )

  val routes = List(createReview, getAllReviews, getReviewById)
