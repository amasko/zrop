package com.amasko.reviewboard
package http
package responses

case class UserResponse(email: String) derives zio.json.JsonCodec
