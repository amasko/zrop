package com.amasko.reviewboard
package components

import com.raquo.laminar.api.L.{*, given}
import domain.data.Company
import common.Constants

object AddReviewCard:

  def apply(companyId: Long, onCancel: () => Unit) =
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
              div(
                cls := "add-review-score",
                label(forId := "would-recommend-selector", s"Would Recommend:"),
                select(
                  idAttr := "would-recommend-selector",
                  (1 to 5).reverse.map { v =>
                    option(v.toString)
                    // TODO set state here
                  }
                )
              )
              // TODO do the same for all score fields
            ),
            // text area for the text review
            div(
              cls := "add-review-text",
              label(forId := "add-review-text", "Your review - supports Markdown"),
              textArea(
                idAttr      := "add-review-text",
                cls         := "add-review-text-input",
                placeholder := "Write your review here"
                // TODO set state here
              )
            ),
            button(
              `type` := "button",
              cls    := "btn btn-warning rock-action-btn",
              "Post review"
              // TODO post the review on this button
            ),
            a(
              cls  := "add-review-cancel",
              href := "#",
              "Cancel",
              onClick --> (_ => onCancel())
            )
            // TODO show potential errors here
          )
        )
      )
    )
