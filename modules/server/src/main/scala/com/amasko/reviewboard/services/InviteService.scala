package com.amasko.reviewboard
package services

import com.amasko.reviewboard.config.InvitePackConfig
import com.amasko.reviewboard.domain.data.InviteNameRecord
import com.amasko.reviewboard.repositories.{CompanyRepo, InviteRepo}
import zio.*

trait InviteService:
  def getByUserId(userName: String): Task[List[InviteNameRecord]]
  def sendInvites(userName: String, companyId: Long, receivers: List[String]): Task[Int]
  def addInvitePack(userName: String, companyId: Long): Task[Long]

final case class InviteServiceLive(
    repo: InviteRepo,
    companyRepo: CompanyRepo,
    emailService: EmailService,
    conf: InvitePackConfig
) extends InviteService:
  override def getByUserId(userName: String): Task[List[InviteNameRecord]] =
    repo.getByUserName(userName)

  override def sendInvites(userName: String, companyId: Long, receivers: List[String]): Task[Int] =
    for {
      company <- companyRepo
        .getById(companyId)
        .someOrFail(new NoSuchElementException(s"Company with id $companyId not found"))
      invitesMarked <- repo.markInvites(userName, companyId, receivers.size)
      _ <- ZIO
        .foreachParDiscard(receivers.take(invitesMarked)) { receiver =>
          emailService.sentReviewInvite(receiver, company)
        }
        .withParallelism(2)
    } yield invitesMarked

  override def addInvitePack(userName: String, companyId: Long): Task[Long] =
    for
      _ <- companyRepo
        .getById(companyId)
        .someOrFail(new NoSuchElementException(s"Company with id $companyId not found"))
      currentPack <- repo.getInvitePack(userName, companyId)
      id <- currentPack match
        case Some(_) =>
          ZIO.fail(
            new IllegalStateException(
              s"Invite pack already exists for user $userName and company $companyId"
            )
          )
        case None => repo.addInvitePack(userName, companyId, conf.n)
//      _ <- repo.activatePack(id) /// todo remove later
    yield id

end InviteServiceLive

object InviteServiceLive:
  val layer
      : ZLayer[InviteRepo & CompanyRepo & EmailService & InvitePackConfig, Nothing, InviteService] =
    ZLayer.fromFunction(InviteServiceLive.apply)
