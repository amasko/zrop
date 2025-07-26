package com.amasko.reviewboard
package http
package controllers

import com.amasko.reviewboard.domain.data.UserID
import com.amasko.reviewboard.services.{InviteService, JWTService, PaymentService}
import zio.*
import responses.InviteResponse
import zio.json.*
import endpoints.InviteEndpoints

class InviteController(jwt: JWTService, service: InviteService, paymentService: PaymentService)
    extends BaseController
    with InviteEndpoints
    with SecureEndpoint(jwt):

  val addPack =
    addInvitePackEndpoint
      .serverSecurityLogic[UserID, Task](verify)
      .serverLogic { user => request =>
        val result =
          for created <- service.addInvitePack(user.email, request.companyId)
          yield created.toString

        result.either
      }

  val invite = inviteEndpoint
    .serverSecurityLogic[UserID, Task](verify)
    .serverLogic { user => request =>
      val result =
        for sent <- service.sendInvites(user.email, request.companyId, request.emails)
        yield InviteResponse(
          "Invites sent successfully or not really",
          sent
        )
      result.either
    }

  val getByUserId = getByUserIdEndpoint
    .serverSecurityLogic[UserID, Task](verify)
    .serverLogic { user => _ =>
      service.getByUserId(user.email).either
    }

  val addPackPromoted = addInvitePackPromotedEndpoint
    .serverSecurityLogic[UserID, Task](
      verify
    )
    .serverLogic { user => request =>
      val result =
        for
          createdPackId <- service.addInvitePack(user.email, request.companyId)
          session <- paymentService
            .createCheckoutSession(createdPackId, user.email)
            .someOrFail(new RuntimeException("Failed to create checkout session"))
        yield session.getUrl

      result.either
    }

  val webhook = webhookEndpoint
    .serverLogic[Task] { (signature, payload) =>
      paymentService.handleWebhook(signature, payload).unit.either
    }

  override val routes = List(addPack, invite, getByUserId, addPackPromoted, webhook)

end InviteController

object InviteController:
  def makeZIO =
    for
      jwt     <- ZIO.service[JWTService]
      service <- ZIO.service[InviteService]
      payment <- ZIO.service[PaymentService]
    yield new InviteController(jwt, service, payment)
