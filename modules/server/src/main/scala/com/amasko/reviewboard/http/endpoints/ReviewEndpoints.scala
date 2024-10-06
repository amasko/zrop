package com.amasko.reviewboard
package http
package endpoints

import com.amasko.reviewboard.domain.data.Review
import requests.CreateReviewRequest
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.generic.auto.*

trait ReviewEndpoints:
  
  val createEndpoint =
    endpoint.tag("reviews")
      .name("create")
      .description("create a review")
      .in("reviews")
      .post
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getAllEndpoint =
    endpoint.tag("reviews")
      .name("getAll")
      .description("get all reviews")
      .in("reviews")
      .get
      .out(jsonBody[List[Review]])

  val getById =
    endpoint.tag("reviews")
      .name("getById")
      .description("get a review by id")
      .in("reviews" / path[String]("id"))
      .get
      .out(jsonBody[Option[Review]])
