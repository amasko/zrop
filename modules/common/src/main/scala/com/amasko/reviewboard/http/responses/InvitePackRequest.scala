package com.amasko.reviewboard
package http
package responses

case class InvitePackRequest (companyId: Long) derives zio.json.JsonCodec
