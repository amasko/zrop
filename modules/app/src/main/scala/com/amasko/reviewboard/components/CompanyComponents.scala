package com.amasko.reviewboard
package components

import com.raquo.laminar.api.L.{*, given}
import domain.data.Company
import common.Constants

object CompanyComponents:
  def renderPicture(c: Company) =
    img(
      cls := "img-fluid",
      src := c.image.getOrElse(Constants.defaultCompanyLogo),
      alt := c.name
    )

  def locationString(c: Company) =
    c.location
      .map(l => s"$l, ${c.country.getOrElse("Unknown country")}")
      .getOrElse("Unknown location")

  def renderDetail(icon: String, value: String) =
    div(
      cls := "company-detail",
      i(cls := s"fa fa-$icon company-detail-icon"),
      p(
        cls := "company-detail-value",
        value
      )
    )

  def renderOverview(c: Company) =
    div(
      cls := "company-summary",
      renderDetail("location-dot", locationString(c)),
      renderDetail("tags", c.tags.mkString(", "))
    )
