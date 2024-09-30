package com.amasko.reviewboard.http.requests

import com.amasko.reviewboard.domain.data.Company

final case class CreateCompanyRequest(
  name: String,
  url: String,
  location: Option[String],
  country: Option[String],
                                     ):
  def toCompany(id: Long): Company = Company(id, CreateCompanyRequest.toSlug(name), name, url, location, country)

object CreateCompanyRequest:
  import zio.json.{DeriveJsonCodec, JsonCodec}
  given JsonCodec[CreateCompanyRequest] = DeriveJsonCodec.gen[CreateCompanyRequest]
  
  def toSlug(name: String): String = name.toLowerCase.replaceAll(" ", "-")