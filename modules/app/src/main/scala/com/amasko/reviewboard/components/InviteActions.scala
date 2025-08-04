package com.amasko.reviewboard
package components

import common.Constants
import core.ZJS.*
import domain.data.InviteNameRecord
import http.requests.InviteRequest

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import zio.*

object InviteActions:

  private val inviteListBus = EventBus[List[InviteNameRecord]]()

  private val inviteRefreshList =
    callBackend(_.callSecure(_.invites.getByUserIdEndpoint)(()))
//      .emitTo(inviteListBus)

  def apply() = {
    div(
      onMountCallback(_ => inviteRefreshList.emitTo(inviteListBus)),
      cls := "profile-sections",
      h3(span("Invites")),
//      child.text <-- inviteListBus.events.map(_.toString), // todo test this
      children <-- inviteListBus.events.map { invites =>
        if (invites.isEmpty) {
          List(div("You have no invites"))
        } else {
          invites.map(renderInviteSection)
        }
      }
    )
  }

  private def renderInviteSection(i: InviteNameRecord) = {
    val emailListVar = Var[Array[String]](Array())
    val errorVar     = Var[Option[String]](None)
    val inviteSubmitter = () => {
      val emails = emailListVar.now()
      if (emails.isEmpty) {
        errorVar.set(Some("Please enter at least one email address."))
      } else {
        // Here you would typically call the backend to submit the invites
        dom.console.log(
          s"Submitting invites for ${i.companyName}: ${emails.mkString(", ")}"
        ) // todo temp
        // Reset the email list after submission
        val list = emails.toList
        if list.exists(a => !a.matches(Constants.emailRegex)) then
          errorVar.set(Some("Invalid email format. Please check your entries."))
        else {
          val prog = for
            email <- callBackend(
              _.callSecure(_.invites.inviteEndpoint)(
                InviteRequest(companyId = i.companyId, emails = list)
              )
            )
            _           <- ZIO.logInfo(s"Invite sent to: $email") // todo temp
            invitesLeft <- inviteRefreshList
          yield invitesLeft

          errorVar.set(None) // Clear any previous errors
          prog.emitTo(inviteListBus)
        }
      }
    }

    div(
      cls := "list-group-item",
      h5(span(i.companyName)),
      p(s"${i.nInvites} invites available"),
      textArea(
        cls         := "form-control",
        readOnly    := false,
        placeholder := "Enter emails to invite",
        onInput.mapToValue --> { value =>
          emailListVar.set(value.split("\n").map(_.trim).filter(_.nonEmpty))
        }
        // update state
      ),
      button(
        `type` := "button",
        cls    := "btn btn-primary",
        span("Send Invites"),
        onClick --> { _ =>
//          dom.console.log(s"Invite clicked: $i") // todo temp
          inviteSubmitter()
        }
      ),
      child.maybe <-- errorVar.signal.map {
        case Some(error) =>
          Some(
            div(
              cls := "page-status-errors",
              error
            )
          )
        case None => None
      }
    )
  }
