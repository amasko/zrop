package com.amasko.reviewboard
package http
package requests

case class RegisterUser(email: String, password: String) derives zio.json.JsonCodec
