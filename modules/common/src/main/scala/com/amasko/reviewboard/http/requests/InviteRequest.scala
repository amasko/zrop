package com.amasko.reviewboard
package http
package requests

case class InviteRequest (companyId: Long, emails: List[String]) derives zio.json.JsonCodec
