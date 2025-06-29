package com.amasko.reviewboard.domain.data

case class InviteRecord (
                          id: Long, // PK
                          userName: String, // FK
                          companyId: Long, // FK
                          nInvites: Int,
                          active: Boolean,
                        )
