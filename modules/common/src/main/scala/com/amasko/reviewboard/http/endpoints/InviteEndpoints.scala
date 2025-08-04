package com.amasko.reviewboard
package http
package endpoints

import com.amasko.reviewboard.domain.data.InviteNameRecord
import com.amasko.reviewboard.http.requests.InviteRequest
import com.amasko.reviewboard.http.responses.{InvitePackRequest, InviteResponse}
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import sttp.tapir.*

trait InviteEndpoints extends BaseEndpoint:

    val addInvitePackEndpoint =
        secureBaseEndpoint
        .tag("Invites")
        .name("add invites")
        .description("get invite tokens")
        .in("invite" / "add")
        .post
        .in(jsonBody[InvitePackRequest])
        .out(stringBody)

    val inviteEndpoint =
      secureBaseEndpoint
        .tag("Invites")
        .name("invites")
        .description("send emails to invite users to review")
        .in("invites")
        .post
        .in(jsonBody[InviteRequest])
        .out(jsonBody[InviteResponse])

    val getByUserIdEndpoint =
        secureBaseEndpoint
        .tag("Invites")
        .name("getByUserId")
        .description("get all active invites by user id")
        .in("invites" / "all")
        .get
        .out(jsonBody[List[InviteNameRecord]])

    val addInvitePackPromotedEndpoint =
      secureBaseEndpoint
        .tag("Invites (promoted)")
        .name("add invites (promoted)")
        .description("get invite tokens (promoted)")
        .in("invite" / "promoted")
        .post
        .in(jsonBody[InvitePackRequest])
        .out(stringBody) // stripe url
      
    val webhookEndpoint =
      baseEndpoint
        .tag("Invites")
        .name("webhook")
        .description("stripe webhook for invite packs")
        .in("invite" / "webhook")
        .post
        .in(header[String]("Stripe-Signature"))
        .in(stringBody) // raw body
        .out(emptyOutput) // response message


end InviteEndpoints