package com.amasko.reviewboard
package http
package requests

case class PasswordRecovery(email: String, token: String, newPassword: String)
    derives zio.json.JsonCodec
