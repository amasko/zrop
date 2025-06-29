package com.amasko.reviewboard
package services

import com.amasko.reviewboard.domain.data.InviteNameRecord
import com.amasko.reviewboard.repositories.{CompanyRepo, InviteRepo}
import zio.*

trait InviteService:
  def getByUserId(userName: String): Task[List[InviteNameRecord]]
  def sendInvites(userName: String, companyId: Long, receivers: List[String]): Task[Int]
  def addInvitePack(userName: String, companyId: Long): Task[Long]

final case class InviteServiceLive(repo: InviteRepo, companyRepo: CompanyRepo)
    extends InviteService:
  override def getByUserId(userName: String): Task[List[InviteNameRecord]] =
    repo.getByUserName(userName)

  override def sendInvites(userName: String, companyId: Long, receivers: List[String]): Task[Int] =
    ZIO.succeed(receivers.size) // Placeholder for actual implementation

  override def addInvitePack(userName: String, companyId: Long): Task[Long] =
    for
      _ <- companyRepo.getById(companyId).flatMap {
        case Some(_) => ZIO.unit
        case None => ZIO.fail(new NoSuchElementException(s"Company with id $companyId not found"))
      }
      currentPack <- repo.getInvitePack(userName, companyId)
      id <- currentPack match
        case Some(_) =>
          ZIO.fail(
            new IllegalStateException(
              s"Invite pack already exists for user $userName and company $companyId"
            )
          )
        case None => repo.addInvitePack(userName, companyId, 200)
      _ <- repo.activatePack(id) /// todo remove later
    yield 23L

object InviteServiceLive:
  val layer: ZLayer[InviteRepo & CompanyRepo, Nothing, InviteService] =
    ZLayer.fromFunction(InviteServiceLive.apply)
