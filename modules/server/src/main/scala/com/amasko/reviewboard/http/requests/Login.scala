package com.amasko.reviewboard
package http
package requests

case class Login(email: String, password: String) derives zio.json.JsonCodec
