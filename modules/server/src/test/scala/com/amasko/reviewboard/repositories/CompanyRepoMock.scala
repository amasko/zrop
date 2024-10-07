package com.amasko.reviewboard
package repositories

import domain.data.Company
import zio.ULayer

class CompanyRepoMock(db: zio.Ref[Map[Long, Company]]) extends CompanyRepo:
  override def create(c: Company): zio.Task[Company] = db.modify { map =>
    val c1 = c.copy(id = map.size + 1)
    val updated = map + (c1.id -> c1)
    c1 -> updated
  }
  override def update(id: Long, op: Company => Company): zio.Task[Company] =
    getById(id).someOrFail(new RuntimeException("Company not found")).flatMap(c => create(op(c))) *>
    db.modify { map =>
    map.get(id) match
      case Some(c) =>
        val c1 = op(c)
        val updated = map + (c1.id -> c1)
        c1 -> updated
      case None => throw new Exception(s"Company with id $id not found")

  }
  override def delete(id: Long): zio.Task[Company] = ???
  override def getById(id: Long): zio.Task[Option[Company]] = db.get.map(_.get(id))
  override def getAll: zio.Task[List[Company]] = ???
  override def getBySlug(slug: String): zio.Task[Option[Company]] = ???
  
end CompanyRepoMock

object CompanyRepoMock:
  val layer: ULayer[CompanyRepo] = zio.ZLayer.fromZIO(
    zio.Ref.make(Map.empty[Long, Company]).map(ref => new CompanyRepoMock(ref))
  )

end CompanyRepoMock
