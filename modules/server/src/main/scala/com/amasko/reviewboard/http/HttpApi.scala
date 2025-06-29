package com.amasko.reviewboard
package http

import http.controllers.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object HttpApi:
  private def gatherRouts(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)

  private def makeControllers =
    for
      healthController  <- HealthController.makeZIO
      companyController <- CompanyController.makeZIO
      reviewController  <- ReviewController.makeZIO
      userController    <- UserController.makeZIO
      invites           <- InviteController.makeZIO
    yield List(healthController, companyController, reviewController, userController, invites)

  val routesZIO = makeControllers.map(gatherRouts).map { rts =>
    SwaggerInterpreter()
      .fromServerEndpoints(rts, "zrop", "1.0.0") ::: rts
  }
