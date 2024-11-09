package com.amasko.reviewboard
package domain
package data

case class CompanyFilter(
    locations: List[String] = Nil,
    companies: List[String] = Nil,
    countries: List[String] = Nil,
    industries: List[String] = Nil,
    tags: List[String] = Nil
) derives zio.json.JsonCodec
