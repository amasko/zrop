package com.amasko.reviewboard
package components

import com.amasko.reviewboard.http.requests.CreateReviewRequest
import com.raquo.laminar.api.L.{*, given}
import domain.data.Review
import org.scalajs.dom
import core.ZJS.*

class AddReviewCard(companyId: Long, triggerBus: EventBus[Unit], onDisable: () => Unit):
  case class State(
      review: Review,
      showErrors: Boolean,
      upstreamErrors: Option[String]
  )

  object State:
    def empty(companyId: Long): State =
      State(Review.empty(companyId), showErrors = false, upstreamErrors = None)

  val stateVar = Var(State.empty(0))

  val submitter = Observer[State]: s =>
    if s.upstreamErrors.nonEmpty then stateVar.update(_.copy(showErrors = true))
    else
      dom.console.log("=>> Current State: " + s) // todo temp
      val pp = callBackend(
        _.callSecure(_.reviews.createEndpoint)(CreateReviewRequest.fromReview(s.review))
      )
        .mapBoth(
          { err =>
            stateVar.update(_.copy(showErrors = true, upstreamErrors = Some(err.getMessage)))
          },
          { _ =>
            //            core.Session.setUserState(token)
            onDisable()
            //            BrowserNavigation.replaceState("/")
          }
        )
        .merge

      pp.emitTo(triggerBus)

  def run() =
    stateVar.set(State.empty(companyId))
    div(
      cls := "container",
      div(
        cls := "markdown-body overview-section",
        div(
          cls := "company-description add-review",
          div(
            // score dropdowns
            div(
              cls := "add-review-scores",
              renderDropdown("Would recommend", (r, v) => r.copy(wouldRecommend = v)),
              renderDropdown("Management", (r, v) => r.copy(management = v)),
              renderDropdown("Culture", (r, v) => r.copy(culture = v)),
              renderDropdown("Salary", (r, v) => r.copy(salary = v)),
              renderDropdown("Benefits", (r, v) => r.copy(benefits = v))
            ),
            // text area for the text review
            div(
              cls := "add-review-text",
              label(forId := "add-review-text", "Your review - supports Markdown"),
              textArea(
                idAttr      := "add-review-text",
                cls         := "add-review-text-input",
                placeholder := "Write your review here",
                onInput.mapToValue --> stateVar.updater { (s: State, value: String) =>
                  s.copy(review = s.review.copy(review = value))
                }
              )
            ),
            button(
              `type` := "button",
              cls    := "btn btn-warning rock-action-btn",
              "Post review",
              onClick.preventDefault.mapTo(stateVar.now()) --> submitter
            ),
            a(
              cls  := "add-review-cancel",
              href := "#",
              "Cancel",
              onClick --> (_ => onDisable())
            ),
            children <-- stateVar.signal
              .map { s =>
                s.upstreamErrors.filter(_ => s.showErrors)
              }
              .map(maybeRenderError)
              .map(_.toList)
          )
        )
      )
    )

  private def maybeRenderError(error: Option[String]) =
    error.map(err => div(cls := "page-status-errors", err))

  private def renderDropdown(name: String, updFn: (Review, Int) => Review) =
    val selectorId = name.split(" ").map(_.toLowerCase()).mkString("-")
    div(
      cls := "add-review-score",
      label(forId := selectorId, s"$name:"),
      select(
        idAttr := selectorId,
        (1 to 5).reverse.map { v =>
          option(v.toString)
          // TODO set state here (mm??)
        },
        onInput.mapToValue --> stateVar.updater { (s: State, value: String) =>
          dom.console.log(s"Updating $name to $value") /// todo temp
          s.copy(review = updFn(s.review, value.toInt))
        }
      )
    )

end AddReviewCard
