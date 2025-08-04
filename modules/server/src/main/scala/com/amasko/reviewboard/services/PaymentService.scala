package com.amasko.reviewboard
package services

import com.amasko.reviewboard.repositories.InviteRepo
import config.PaymentConfig
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams

import scala.jdk.OptionConverters.*
import zio.*

trait PaymentService:
  def createCheckoutSession(packId: Long, userName: String): Task[Option[Session]]

  def handleWebhook(
      signature: String, // TODO: use typed objects/opaque types
      payload: String
  ): Task[Boolean]

case class PaymentServiceLive(stripeConf: PaymentConfig, inviteRepo: InviteRepo)
    extends PaymentService:

  override def createCheckoutSession(packId: Long, userName: String): Task[Option[Session]] =
    ZIO
      .attempt {
        SessionCreateParams
          .builder()
          .setMode(SessionCreateParams.Mode.PAYMENT)
          .addLineItem( // describe product properties
            SessionCreateParams.LineItem
              .builder()
              .setPrice(stripeConf.price)
              .setQuantity(1L)
              .build()
          )
          .setSuccessUrl(stripeConf.successUrl)
          .setCancelUrl(stripeConf.cancelUrl)
          .setCustomerEmail(userName)
          .setClientReferenceId(packId.toString)
          .setPaymentIntentData(
            SessionCreateParams.PaymentIntentData.builder().setReceiptEmail(userName).build()
          )
          .setInvoiceCreation(
            SessionCreateParams.InvoiceCreation
              .builder()
              .setEnabled(true)
              .build()
          )
          .build()
      }
      .flatMap(p =>
        ZIO.attempt(Session.create(p)) <* ZIO.logInfo(s"Stripe session created for pack $packId")
      )
      .logError("Stripe session failed")
      .option

  override def handleWebhook(
      signature: String,
      payload: String
  ): Task[Boolean] = // todo generalize on return type
    val result = for
      event <- ZIO.attempt(
        com.stripe.net.Webhook.constructEvent(payload, signature, stripeConf.secret)
      )
      _ <- ZIO.logInfo(s"Stripe webhook event received: ${event.getType}")
      pack <- event.getType match
        case "checkout.session.completed" =>
          ZIO
            .attempt {
//              val session = event.getDataObjectDeserializer.getObject.toScala.map(
//                _.asInstanceOf[com.stripe.model.checkout.Session]
//              ) // wtf?
              val ev = event.getDataObjectDeserializer
              val stripeObject =
                if (ev.getObject.isPresent) ev.getObject.get
                else
                  throw new RuntimeException(
                    "Stripe event data object is not present"
                  )
              stripeObject.asInstanceOf[com.stripe.model.checkout.Session]
            }
            .flatMap { session =>
              ZIO
                .attempt(session.getClientReferenceId.toLong)
                .logError(
                  s"Failed to parse client reference ID from session ${session.getClientReferenceId}"
                )
                .asSome
            }
        case _ => ZIO.logWarning(s"Unhandled event type: ${event.getType}").as(None)
      _ <- ZIO.logInfo(s"Activating Pack ${pack}")
      isActivated <- pack match
        case Some(id) => inviteRepo.activatePack(id)
        case None     => ZIO.succeed(false)
    yield isActivated

    result
      .logError("Stripe webhook handling failed")
      .catchSome {
        case e: com.stripe.exception.SignatureVerificationException => // todo return 400 bad request
          ZIO.logError(s"Stripe signature verification failed: ${e.getMessage}").as(false)
      }

end PaymentServiceLive

object PaymentServiceLive:
  val layer: ZLayer[PaymentConfig & InviteRepo, Throwable, PaymentServiceLive] = ZLayer {
    for
      conf <- ZIO.service[PaymentConfig]
      repo <- ZIO.service[InviteRepo]
      _ <- ZIO.attempt {
        com.stripe.Stripe.apiKey = conf.key // Set the Stripe API key
      }
    yield PaymentServiceLive(conf, repo)
  }
