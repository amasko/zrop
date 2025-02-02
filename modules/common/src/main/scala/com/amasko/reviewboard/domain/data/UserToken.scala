package com.amasko.reviewboard
package domain
package data

case class UserToken(
    id: Long,
    email: String,
    token: String,
    expires: Long
) derives zio.json.JsonCodec
