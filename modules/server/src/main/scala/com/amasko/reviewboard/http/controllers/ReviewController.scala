package com.amasko.reviewboard
package http
package controllers

import services.ReviewService
import domain.data.Review
import endpoints.ReviewEndpoints

import zio.*

class ReviewController private (service: ReviewService) extends BaseController with ReviewEndpoints:

  val createReview = createEndpoint
    .serverLogic[Task] { request =>
      val result = for
        now <- Clock.instant
        review = request.toReview(0L, now)
        created <- service.create(review)
      yield created

      result.either
    }

  val getAllReviews = getAllEndpoint
    .serverLogic[Task](_ => service.getReviews.either)

  val getReviewById = getById
    .serverLogic[Task] { id =>
      val result = id.toLongOption match
        case Some(id) => service.getReview(id)
        case None     => ZIO.fail(new Exception("Invalid id"))

      result.either
    }

  val routes = List(createReview, getAllReviews, getReviewById)

object ReviewController:
  def makeZIO =
    for service <- ZIO.service[ReviewService]
    yield new ReviewController(service)
