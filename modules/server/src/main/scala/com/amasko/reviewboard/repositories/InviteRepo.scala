package com.amasko.reviewboard
package repositories

import domain.data.{Company, InviteNameRecord, InviteRecord}
import io.getquill.jdbczio.Quill
import io.getquill.*
import zio.*

trait InviteRepo:
  def getByUserName(userName: String): Task[List[InviteNameRecord]]
  def getInvitePack(userName: String, companyId: Long): Task[Option[InviteRecord]]
  def addInvitePack(userName: String, companyId: Long, size: Int): Task[Long]
  def activatePack(id: Long): Task[Boolean]
  def markInvites(userName: String, companyId: Long, nInvites: Int): Task[Int]

case class InviteRepoLive(quill: Quill.Postgres[io.getquill.SnakeCase]) extends InviteRepo {
  import quill.*

  inline given SchemaMeta[InviteRecord] = schemaMeta[InviteRecord]("invites")
  inline given InsertMeta[InviteRecord] = insertMeta[InviteRecord](_.id)
  inline given UpdateMeta[InviteRecord] = updateMeta[InviteRecord](_.id)

  inline given SchemaMeta[Company] = schemaMeta[Company]("companies")
  inline given InsertMeta[Company] = insertMeta[Company](_.id)
  inline given UpdateMeta[Company] = updateMeta[Company](_.id)

  override def getByUserName(userName: String): Task[List[InviteNameRecord]] =
    run {
      for
        inv <- query[InviteRecord]
          .filter(i => i.userName == lift(userName) && i.active && i.nInvites > 0)
        company <- query[Company]
          .filter(_.id == inv.companyId)
      yield InviteNameRecord(company.id, company.name, inv.nInvites)
    }

  override def activatePack(id: Long): Task[Boolean] =
    run {
      query[InviteRecord]
        .filter(_.id == lift(id))
        .update(_.active -> true)
    }.map(a => a > 0)

  override def addInvitePack(userName: String, companyId: Long, size: Int): Task[Long] =
    run {
      query[InviteRecord]
        .insertValue(lift(InviteRecord(-1, userName, companyId, size, active = false)))
        .returningGenerated(_.id)
    }

  override def getInvitePack(userName: String, companyId: Long): Task[Option[InviteRecord]] =
    run {
      query[InviteRecord]
        .filter(i => i.userName == lift(userName) && i.companyId == lift(companyId) && i.active)
    }.map(_.headOption)

  private inline def calc = quote { (current: Int, substact: Int) =>
//    if current > newOne then current - newOne else 0
    current - substact
  }

  override def markInvites(userName: String, companyId: Long, nInvites: Int): Task[Int] =
    run {
      query[InviteRecord]
        .filter(i => i.userName == lift(userName) && i.companyId == lift(companyId) && i.active)
        .update(ir => ir.nInvites -> calc(ir.nInvites, lift(nInvites)))
        .returning(_.nInvites)
    }.map(r => if r > 0 then nInvites else r + nInvites)

}

object InviteRepoLive {
  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, InviteRepo] =
    ZLayer.fromFunction((pg: Quill.Postgres[SnakeCase]) => InviteRepoLive(pg))
}
