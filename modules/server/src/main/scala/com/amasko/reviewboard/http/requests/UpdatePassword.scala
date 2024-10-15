package com.amasko.reviewboard
package http
package requests

case class UpdatePassword(
    email: String,
    oldPassword: String,
    newPassword: String
) derives zio.json.JsonCodec
