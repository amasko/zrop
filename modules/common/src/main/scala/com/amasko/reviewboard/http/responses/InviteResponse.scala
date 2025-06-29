package com.amasko.reviewboard
package http
package responses

case class InviteResponse (status: String, nInvites: Int) derives zio.json.JsonCodec
