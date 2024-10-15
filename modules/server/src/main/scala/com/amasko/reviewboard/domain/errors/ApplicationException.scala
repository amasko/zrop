package com.amasko.reviewboard
package domain
package errors

sealed abstract class ApplicationException(message: String) extends RuntimeException(message)

case object UnauthorizedException             extends ApplicationException("Unauthorized")
case class NotFoundException(message: String) extends ApplicationException(message)
