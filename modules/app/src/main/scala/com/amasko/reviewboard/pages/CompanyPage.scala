package com.amasko.reviewboard
package pages

import com.amasko.reviewboard.components.AddReviewCard
import com.amasko.reviewboard.core.Session
import com.amasko.reviewboard.domain.data.UserToken
import common.Constants
import http.endpoints.CompanyEndpoints
import com.raquo.laminar.api.L.{*, given}
//import org.scalajs.dom.*
import zio.*
import domain.data.{Company, Review}
import components.CompanyComponents.*

import core.ZJS.*

object CompanyPage:
  enum Status {
    case Loading
    case Ok(company: Company)
    case NotFound
  }

  // reactive variables
  val addReviewCardActive = Var(false)

  val fetchCompanyBus = EventBus[Option[Company]]()

  val reviewSignal = fetchCompanyBus.events
    .flatMapSwitch {
      case None => EventStream.empty
//    case Some(company) => EventStream.fromSeq(company.reviews)
      case Some(company) =>
        callBackend(_.call(_.reviews.getByCompanyId)(company.id.toString)).toEventSteam
    }
    .scanLeft(List.empty[Review])((acc, reviews) => acc ++ reviews) // todo temp check

  val status = fetchCompanyBus.events.scanLeft(Status.Loading) {
    case (_, None)          => Status.NotFound
    case (_, Some(company)) => Status.Ok(company)
  }

  // render the company page
  def apply(companyId: Long) = {
    div(
      cls := "container-fluid the-rock",
      onMountCallback(_ =>
        callBackend(_.call(_.companies.getById)(companyId.toString)).emitTo(fetchCompanyBus)
      ),
      children <-- status.map {
        case Status.Loading     => List(div("Loading..."))
        case Status.NotFound    => List(div("Company not found"))
        case Status.Ok(company) => render(company, reviewSignal)
      }
    )
  }

  def render(company: Company, reviewsSig: Signal[List[Review]]) = List(
    div(
      cls := "row jvm-companies-details-top-card",
      div(
        cls := "col-md-12 p-0",
        div(
          cls := "jvm-companies-details-card-profile-img",
          renderPicture(company)
        ),
        div(
          cls := "jvm-companies-details-card-profile-title",
          h1(company.name),
          div(
            cls := "jvm-companies-details-card-profile-company-details-company-and-location",
            renderOverview(company)
          )
        ),
        child <-- Session.userState.signal.map { user => maybeRenderUserAction(user, reviewsSig) }
      )
    ),
    div(
      cls := "container-fluid",
      renderCompanySummary,
      children <-- addReviewCardActive.signal.map { active =>
        if active then List(AddReviewCard(company.id, () => addReviewCardActive.set(false)))
        else Nil
      },
//        reviewCard(),
      children <-- reviewSignal.map(reviews => reviews.map(renderStaticReview)),
      div(
        cls := "container",
        div(
          cls := "rok-last",
          div(
            cls := "row invite-row",
            div(
              cls := "col-md-6 col-sm-6 col-6",
              span(
                cls := "rock-apply",
                p("Do you represent this company?"),
                p("Invite people to leave reviews.")
              )
            ),
            div(
              cls := "col-md-6 col-sm-6 col-6",
              a(
                href   := company.url,
                target := "blank",
                button(`type` := "button", cls := "rock-action-btn", "Invite people")
              )
            )
          )
        )
      )
    )
  )

  def maybeRenderUserAction(user: Option[UserToken], reviewSig: Signal[List[Review]]) =
    user match
      case Some(user) =>
        div(
          cls := "jvm-companies-details-card-apply-now-btn",
          child <-- reviewSig.map { reviews =>
            reviews.find(_.userId == user.id) match
              case Some(r) => div("You have already reviewed this company")
              case None =>
                button(
                  `type` := "button",
                  cls    := "btn btn-warning",
                  "Add a review",
                  disabled <-- addReviewCardActive.signal,
                  onClick.mapTo(true) --> addReviewCardActive.writer
                )

          }
        )
      case None =>
        div(
          cls := "jvm-companies-details-card-apply-now-btn",
          "You must be logged in to add a review"
        )

  def renderCompanySummary =
    div(
      cls := "container",
      div(
        cls := "markdown-body overview-section",
        div(
          cls := "company-description",
          "TODO company summary"
        )
      )
    )

  def renderStaticReview(review: Review) =
    div(
      cls := "container",
      div(
        cls := "markdown-body overview-section",
        // TODO add a highlight if this is "your" review
        div(
          cls := "company-description",
          div(
            cls := "review-summary",
            renderReviewDetail("Would Recommend", review.wouldRecommend),
            renderReviewDetail("Management", review.management),
            renderReviewDetail("Culture", review.culture),
            renderReviewDetail("Salary", review.salary),
            renderReviewDetail("Benefits", review.benefits)
          ),
          // TODO parse this Markdown
          div(
            cls := "review-content",
            review.review
          ),
          div(cls := "review-posted", "Posted (TODO) a million years ago")
        )
      )
    )

  def renderReviewDetail(detail: String, score: Int) =
    div(
      cls := "review-detail",
      span(cls := "review-detail-name", s"$detail: "),
      (1 to score).toList.map(_ =>
        svg.svg(
          svg.cls     := "review-rating",
          svg.viewBox := "0 0 32 32",
          svg.path(
            svg.d := "m15.1 1.58-4.13 8.88-9.86 1.27a1 1 0 0 0-.54 1.74l7.3 6.57-1.97 9.85a1 1 0 0 0 1.48 1.06l8.62-5 8.63 5a1 1 0 0 0 1.48-1.06l-1.97-9.85 7.3-6.57a1 1 0 0 0-.55-1.73l-9.86-1.28-4.12-8.88a1 1 0 0 0-1.82 0z"
          )
        )
      )
    )

end CompanyPage
