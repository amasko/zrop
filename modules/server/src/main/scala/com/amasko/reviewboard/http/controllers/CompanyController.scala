package com.amasko.reviewboard
package http
package controllers

import com.amasko.reviewboard.domain.data.Company
import com.amasko.reviewboard.http.requests.CreateCompanyRequest
import services.CompanyService
import endpoints.CompanyEndpoints
import sttp.tapir.server.ServerEndpoint.Full
import zio.*

class CompanyController private (service: CompanyService)
    extends BaseController
    with CompanyEndpoints:

  val createCompany: Full[Unit, Unit, CreateCompanyRequest, Throwable, Company, Any, Task] =
    createEndpoint
      .serverLogic[Task](request => service.create(request.toCompany(0L)).either)

  val getAllCompanies = getAllEndpoint
    .serverLogic[Task](_ => service.getCompanies.either)

  val getCompanyById = getById
    .serverLogic[Task](id =>
      id.toLongOption match
        case Some(id) => service.getCompany(id).either
        case None     => service.getCompany(id).either
    )

  val routes = List(createCompany, getAllCompanies, getCompanyById)

object CompanyController:
  def makeZIO =
    for service <- ZIO.service[CompanyService]
    yield new CompanyController(service)
