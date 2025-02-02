package com.amasko.reviewboard
package repositories

import io.getquill.jdbczio.Quill
import io.getquill.*

import domain.data.Review

import zio.*

trait ReviewRepo:
  def create(r: Review): zio.Task[Review]
  def update(id: Long, op: Review => Review): zio.Task[Review]
  def delete(id: Long): zio.Task[Review]
  def getById(id: Long): zio.Task[Option[Review]]
  def getByUserId(userId: Long): zio.Task[Option[Review]]
  def getByCompanyId(companyId: Long): zio.Task[List[Review]]
  def getAll: zio.Task[List[Review]]
//  def getByCompanyId(companyId: Long): zio.Task[List[Review]]

case class ReviewRepoLive(quill: Quill.Postgres[io.getquill.SnakeCase]) extends ReviewRepo:
  import quill.*
  inline given SchemaMeta[Review] = schemaMeta[Review]("reviews")
  inline given InsertMeta[Review] = insertMeta[Review](_.id)
  inline given UpdateMeta[Review] = updateMeta[Review](_.id)

  override def create(r: Review): zio.Task[Review] = run(
    query[Review].insertValue(lift(r)).returningGenerated(_.id)
  ).map(id => r.copy(id = id))

  override def update(id: Long, op: Review => Review): zio.Task[Review] =
    for
      review <- getById(id).someOrFail(new RuntimeException("Review not found"))
      updated <- run(
        query[Review].filter(_.id == lift(id)).updateValue(lift(op(review))).returning(a => a)
      )
    yield updated

  override def delete(id: Long): zio.Task[Review] =
    run(query[Review].filter(_.id == lift(id)).delete.returning(r => r))

  override def getById(id: Long): zio.Task[Option[Review]] =
    run(query[Review].filter(_.id == lift(id))).map(_.headOption)

  override def getAll: zio.Task[List[Review]] =
    run(query[Review])

  override def getByUserId(userId: Long): Task[Option[Review]] =
    run(query[Review].filter(_.userId == lift(userId))).map(_.headOption)

  override def getByCompanyId(companyId: Long): Task[List[Review]] =
    run(query[Review].filter(_.companyId == lift(companyId)))

//  override def getByCompanyId(companyId: Long): zio.Task[List[Review]] =
//    run(query[Review].filter(_.companyId == lift(companyId)))

object ReviewRepoLive:
  val layer = ZLayer.fromFunction((pg: Quill.Postgres[SnakeCase]) => ReviewRepoLive(pg))
