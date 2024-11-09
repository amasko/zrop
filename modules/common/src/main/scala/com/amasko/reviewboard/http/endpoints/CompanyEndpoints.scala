package com.amasko.reviewboard
package http
package endpoints

import domain.data.{Company, CompanyFilter}
import requests.CreateCompanyRequest
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import sttp.tapir.*

trait CompanyEndpoints extends BaseEndpoint:
  val createEndpoint =
    secureBaseEndpoint
      .tag("companies")
      .name("create")
      .description("create a listing for a company")
      .in("companies")
      .post
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])

  val getAllEndpoint =
    baseEndpoint
      .tag("companies")
      .name("getAll")
      .description("get all companies")
      .in("companies")
      .get
      .out(jsonBody[List[Company]])

  val getById =
    baseEndpoint
      .tag("companies")
      .name("getById")
      .description("get a company by id")
      .in("companies" / path[String]("id"))
      .get
      .out(jsonBody[Option[Company]])
    
  val allFilters =
    baseEndpoint
      .tag("companies")
      .name("allFilters")
      .description("get all filters")
      .in("companies" / "filters")
      .get
      .out(jsonBody[CompanyFilter])
    
  val searchEndpoint =
    baseEndpoint
      .tag("companies")
      .name("search")
      .description("search for companies")
      .in("companies" / "search")
      .post
      .in(jsonBody[CompanyFilter])
      .out(jsonBody[List[Company]])
