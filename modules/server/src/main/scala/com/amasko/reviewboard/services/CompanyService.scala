package com.amasko.reviewboard
package services

import domain.data.Company
import repositories.CompanyRepo

import zio.*

trait CompanyService {
  def getCompany(id: Long): Task[Option[Company]]
  def getCompany(slug: String): Task[Option[Company]]
  def getCompanies: Task[List[Company]]
  def create(company: Company): Task[Company]
//  def update(company: Company): Task[Company]
  def delete(id: Long): Task[Company]

}


final case class  CompanyServiceLive(repo: CompanyRepo) extends CompanyService:
  override def getCompany(id: Long): Task[Option[Company]] = repo.getById(id)
  override def getCompanies: Task[List[Company]] = repo.getAll
  override def create(company: Company): Task[Company] = repo.create(company)
//  override def update(company: Company): Company = repo.update(company.id, _ => company)
  override def delete(id: Long): Task[Company] = repo.delete(id)

  override def getCompany(slug: String): Task[Option[Company]] = repo.getBySlug(slug)


object CompanyServiceLive:
  val layer: ZLayer[CompanyRepo, Nothing, CompanyServiceLive] = zio.ZLayer.fromFunction(CompanyServiceLive.apply)