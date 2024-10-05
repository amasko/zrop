package com.amasko.reviewboard
package services

trait CompanyService {

}


object CompanyService:
  val dummyLayer = zio.ZLayer.succeed(new CompanyService {})
