package com.amasko.reviewboard.domain.data

case class InviteNameRecord (companyId: Long, companyName: String, nInvites: Int) derives zio.json.JsonCodec
