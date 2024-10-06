package com.amasko.reviewboard
package http
package controllers

import services.CompanyService
import domain.data.Company
import endpoints.CompanyEndpoints

import zio.*

class CompanyController private (service: CompanyService) extends BaseController with CompanyEndpoints:

  val createCompany = createEndpoint
    .serverLogicSuccess[Task](request =>
      service.create(request.toCompany(0L))
    )

  val getAllCompanies = getAllEndpoint
    .serverLogicSuccess[Task](_ => service.getCompanies)
  
  val getCompanyById = getById
    .serverLogicSuccess[Task](id => 
      id.toLongOption match
        case Some(id) => service.getCompany(id)
        case None => service.getCompany(id)
    )
  
  val routes = List(createCompany, getAllCompanies, getCompanyById)

object CompanyController:
  def makeZIO =
    for
      service <- ZIO.service[CompanyService]
    yield new CompanyController(service)
