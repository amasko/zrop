package com.amasko.reviewboard
package http

import com.amasko.reviewboard.http.controllers.*

object HttpApi:
  private def gatherRouts(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)

  private def makeControllers =
    for
      healthController  <- HealthController.makeZIO
      companyController <- CompanyController.makeZIO
      reviewController  <- ReviewController.makeZIO
      userController    <- UserController.makeZIO
    yield List(healthController, companyController, reviewController, userController)

  val routesZIO = makeControllers.map(gatherRouts)
