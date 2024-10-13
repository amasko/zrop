package com.amasko.reviewboard
package http
package requests

case class DeleteAccount (
    email: String,
    password: String
                         ) derives zio.json.JsonCodec
