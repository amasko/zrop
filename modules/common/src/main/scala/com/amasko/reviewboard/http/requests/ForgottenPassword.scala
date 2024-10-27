package com.amasko.reviewboard
package http
package requests

case class ForgottenPassword(email: String) derives zio.json.JsonCodec
