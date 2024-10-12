package com.amasko.reviewboard
package repositories

import domain.data.User

import io.getquill.jdbczio.Quill
import io.getquill.*
import zio.*

trait UserRepo:
  def findUserByEmail(email: String): Task[Option[User]]
  def findUserById(id: Long): Task[Option[User]]
  def createUser(user: User): Task[User]
  def updateUser(id: Long, op: User => User): Task[User]
  def deleteUser(id: Long): Task[User]

case class UserRepoLive(quill: Quill.Postgres[io.getquill.SnakeCase]) extends UserRepo:

  import quill.*

  inline given SchemaMeta[User] = schemaMeta[User]("users")
  inline given InsertMeta[User] = insertMeta[User](_.id)
  inline given UpdateMeta[User] = updateMeta[User](_.id)

  override def findUserByEmail(email: String): Task[Option[User]] =
    run(query[User].filter(_.email == lift(email))).map(_.headOption)

  override def findUserById(id: Long): Task[Option[User]] =
    run(query[User].filter(_.id == lift(id))).map(_.headOption)

  override def createUser(user: User): Task[User] =
    run(
      query[User].insertValue(lift(user)).returningGenerated(_.id)
    ).map(id => user.copy(id = id))

  override def updateUser(id: Long, op: User => User): Task[User] =
    for
      user <- findUserById(id).someOrFail(new RuntimeException("User not found"))
      updated <- run(
        query[User].filter(_.id == lift(user.id)).updateValue(lift(op(user))).returning(a => a)
      )
    yield updated

  override def deleteUser(id: Long): Task[User] =
    run(query[User].filter(_.id == lift(id)).delete.returning(r => r))

end UserRepoLive

object UserRepoLive:
  val layer = ZLayer.fromFunction((pg: Quill.Postgres[SnakeCase]) => UserRepoLive(pg))
