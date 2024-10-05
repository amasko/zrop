package com.amasko.reviewboard
package http
package controllers

import services.CompanyService
import domain.data.Company
import endpoints.CompanyEndpoints

import zio.*

import scala.collection.mutable


class CompanyController private (service: CompanyService) extends BaseController with CompanyEndpoints:

  private val db = mutable.Map[Long, Company]()

  val createCompany = createEndpoint
    .serverLogicSuccess[Task](request =>
      ZIO.succeed {
        val id = db.size + 1
        val slug = request.name.toLowerCase.replaceAll(" ", "-")
        val company = request.toCompany(id)
        db += ((id: Long) -> company)
        company
      })

  val getAllCompanies = getAllEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed(db.values.toList))
  
  val getCompanyById = getById
    .serverLogicSuccess[Task](id => ZIO.attempt(id.toLong).map(db.get))
  
  val routes = List(createCompany, getAllCompanies, getCompanyById)

object CompanyController:
  def makeZIO =
    for
      service <- ZIO.service[CompanyService]
    yield new CompanyController(service)
