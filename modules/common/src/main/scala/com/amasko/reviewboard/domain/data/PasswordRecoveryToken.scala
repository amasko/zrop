package com.amasko.reviewboard
package domain
package data

case class PasswordRecoveryToken(
    email: String,
    token: String,
    expiration: Long
) derives zio.json.JsonCodec
