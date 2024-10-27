package com.amasko.reviewboard
package pages

import domain.data.Company
import common.Constants
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import components.Anchors

object CompaniesPage:

  val dummyCompany = Company(1, "dummy-company", "Dummy Super company", "https://dummy.com", Some("Some city"), Some("Some country"), None, List("tag1", "tag2"))

  def apply() =
    sectionTag(
      cls := "section-1",
      div(
        cls := "container company-list-hero",
        h1(
          cls := "company-list-title",
          "Rock the JVM Companies Board"
        )
      ),
      div(
        cls := "container",
        div(
          cls := "row jvm-recent-companies-body",
          div(
            cls := "col-lg-4",
            div("TODO filter panel here")
          ),
          div(
            cls := "col-lg-8",
            renderCompany(dummyCompany),
            renderCompany(dummyCompany)
          )
        )
      )
    )

  private def renderPicture(c: Company) =
    img(
      cls := "img-fluid",
      src := c.image.getOrElse(Constants.defaultCompanyLogo),
      alt := c.name
    )
    
  private def locationString(c: Company) =
    c.location.map(l => s"$l, ${c.country.getOrElse("Unknown country")}").getOrElse("Unknown location")

  private def renderDetail(icon: String, value: String) =
    div(
      cls := "company-detail",
      i(cls := s"fa fa-$icon company-detail-icon"),
      p(
        cls := "company-detail-value",
        value
      )
    )

  private def renderOverview(c: Company) =
    div(
      cls := "company-summary",
      renderDetail("location-dot", locationString(c)),
      renderDetail("tags",c.tags.mkString(", ")),
    )
    
  private def renderAction(c: Company) =
    div(
      cls := "jvm-recent-companies-card-btn-apply",
      a(
        href   := c.url,
        target := "blank",
        button(
          `type` := "button",
          cls    := "btn btn-danger rock-action-btn",
          "Website"
        )
      )
    )

  def renderCompany(c: Company) =
    div(
      cls := "jvm-recent-companies-cards",
      div(
        cls := "jvm-recent-companies-card-img",
        renderPicture(c)
      ),
      div(
        cls := "jvm-recent-companies-card-contents",
        h5(
          Anchors.renderNavLink(
            c.name,
            s"/company/${c.id}",
            "company-title-link"
          )
        ),
        renderOverview(c)
      ),
      renderAction(c)
    )
