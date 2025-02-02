package com.amasko.reviewboard
package http
package endpoints

import domain.data.Review
import requests.CreateReviewRequest

import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import sttp.tapir.*

trait ReviewEndpoints extends BaseEndpoint:

  val createEndpoint =
    secureBaseEndpoint
      .tag("reviews")
      .name("create")
      .description("create a review")
      .in("reviews")
      .post
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getAllEndpoint =
    baseEndpoint
      .tag("reviews")
      .name("getAll")
      .description("get all reviews")
      .in("reviews")
      .get
      .out(jsonBody[List[Review]])

  val getById =
    baseEndpoint
      .tag("reviews")
      .name("getById")
      .description("get a review by id")
      .in("reviews" / path[String]("id"))
      .get
      .out(jsonBody[Option[Review]])
    
  val getByCompanyId =
    baseEndpoint
      .tag("reviews")
      .name("getByCompanyId")
      .description("get all reviews for a company")
      .in("reviews" / "company" / path[String]("id"))
      .get
      .out(jsonBody[List[Review]])
