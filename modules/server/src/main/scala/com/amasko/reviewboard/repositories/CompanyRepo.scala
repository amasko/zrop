package com.amasko.reviewboard
package repositories

import domain.data.Company

import io.getquill.jdbczio.Quill
import io.getquill.*

import zio.*
trait CompanyRepo:
  def create(c: Company): zio.Task[Company]
  def update(id: Long, op: Company => Company): zio.Task[Company]
  def delete(id: Long): zio.Task[Company]
  def getById(id: Long): zio.Task[Option[Company]]
  def getAll: zio.Task[List[Company]]
  def getBySlug(slug: String): zio.Task[Option[Company]]

class CompanyRepoLive(quill: Quill.Postgres[SnakeCase]) extends CompanyRepo:
  import quill.*
  inline given SchemaMeta[Company] = schemaMeta[Company]("companies")
  inline given InsertMeta[Company] = insertMeta[Company](_.id)
  inline given UpdateMeta[Company] = updateMeta[Company](_.id)

  override def create(c: Company): Task[Company] = run(
    query[Company].insertValue(lift(c)).returningGenerated(_.id)
  ).map(id => c.copy(id = id))

  override def update(id: Long, op: Company => Company): Task[Company] =
    for
      company <- getById(id).someOrFail(new RuntimeException("Company not found"))
      updated <- run(
        query[Company].filter(_.id == lift(id)).updateValue(lift(op(company))).returning(a => a)
      )
    yield updated

  override def delete(id: Long): Task[Company] =
    run(query[Company].filter(_.id == lift(id)).delete.returning(r => r))
//        .map(res => if res > 0 then Some(Company(id, "", "", "", None, None)) else None)

  override def getById(id: Long): Task[Option[Company]] =
    run(query[Company].filter(_.id == lift(id))).map(_.headOption)

  override def getAll: Task[List[Company]] =
    run(query[Company])

  override def getBySlug(slug: String): Task[Option[Company]] =
    run(query[Company].filter(_.slug == lift(slug))).map(_.headOption)

object CompanyRepoLive:
  val layer = ZLayer.fromFunction((pg: Quill.Postgres[SnakeCase]) => CompanyRepoLive(pg))

//object RepoDemo extends ZIOAppDefault:
//
//  val program = for
//    repo <- ZIO.service[CompanyRepo]
//    _    <- repo.create(Company(0L, "slug", "name", "url", None, None))
//    c    <- repo.getAll
//    _    <- ZIO.logInfo(c.mkString(", "))
//  yield ()
//
//  override def run =
//    program
//      .provide(
//        CompanyRepoLive.layer,
//        Quill.Postgres.fromNamingStrategy(SnakeCase),
//        Quill.DataSource.fromPrefix("zrop.db")
//      )
