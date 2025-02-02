package com.amasko.reviewboard
package http
package controllers

import services.{JWTService, ReviewService}
import domain.data.{Review, UserID}
import endpoints.ReviewEndpoints
import zio.*

class ReviewController private (service: ReviewService, jwt: JWTService)
    extends BaseController
    with ReviewEndpoints
    with SecureEndpoint(jwt):

  val createReview = createEndpoint
    .serverSecurityLogic[UserID, Task](verify)
    .serverLogic { user => request =>
      val result = for
        now <- Clock.instant
        review = request.toReview(user.id, now)
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

  val getReviewsByCompanyId = getByCompanyId
    .serverLogic[Task] { id =>
      val result = id.toLongOption match
        case Some(id) => service.getByCompanyId(id)
        case None     => ZIO.fail(new Exception("Invalid id"))

      result.either
    }

  val routes = List(getReviewsByCompanyId, createReview, getAllReviews, getReviewById)

object ReviewController:
  def makeZIO =
    for
      service <- ZIO.service[ReviewService]
      jwt     <- ZIO.service[JWTService]
    yield new ReviewController(service, jwt)
