package com.amasko.reviewboard
package http

import com.amasko.reviewboard.http.controllers.*

object HttpApi:
  private def gatherRouts(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)

  private def makeControllers =
    for
      healthController <- HealthController.makeZIO
      companyController <- CompanyController.makeZIO
    yield List(healthController, companyController)

  val routesZIO = makeControllers.map(gatherRouts)
