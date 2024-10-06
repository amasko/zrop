package com.amasko.reviewboard
package repositories

import domain.data.Company
import zio.ULayer

class CompanyRepoMock extends CompanyRepo:
  override def create(c: Company): zio.Task[Company] = ???
  override def update(id: Long, op: Company => Company): zio.Task[Company] = ???
  override def delete(id: Long): zio.Task[Company] = ???
  override def getById(id: Long): zio.Task[Option[Company]] = ???
  override def getAll: zio.Task[List[Company]] = ???
  override def getBySlug(slug: String): zio.Task[Option[Company]] = ???
  
end CompanyRepoMock

object CompanyRepoMock:
  val layer: ULayer[CompanyRepo] = zio.ZLayer.succeed(CompanyRepoMock())

end CompanyRepoMock
