package com.amasko.reviewboard
package http
package controllers

import domain.data.UserID
import services.{CompanyService, JWTService}

import endpoints.CompanyEndpoints
import sttp.tapir.server.ServerEndpoint.Full
import zio.*

class CompanyController private (service: CompanyService, jwt: JWTService)
    extends BaseController
    with CompanyEndpoints
    with SecureEndpoint(jwt):

  val createCompany =
    createEndpoint
      .serverSecurityLogic[UserID, Task](verify)
      .serverLogic(user => request => service.create(request.toCompany(0L)).either)

  val getAllCompanies = getAllEndpoint
    .serverLogic[Task](_ => service.getCompanies.either)

  val getCompanyById = getById
    .serverLogic[Task](id =>
      id.toLongOption match
        case Some(id) => service.getCompany(id).either
        case None     => service.getCompany(id).either
    )

  val getCompanyFilters = allFilters
    .serverLogic[Task](_ => service.getFilters.either)

  val search = searchEndpoint
    .serverLogic[Task](filter => service.search(filter).either)

  val routes = List(createCompany, getAllCompanies, getCompanyFilters, search, getCompanyById)

object CompanyController:
  def makeZIO =
    for
      service <- ZIO.service[CompanyService]
      jwt     <- ZIO.service[JWTService]
    yield new CompanyController(service, jwt)
