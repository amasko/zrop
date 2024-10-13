package com.amasko.reviewboard
package http
package endpoints

import sttp.tapir.*

trait HealthEndpoint extends BaseEndpoint:

  val healthEndpoint = baseEndpoint.get
    .in("health")
    .out(stringBody)
    .description("Health check endpoint")
    .name("health")

end HealthEndpoint
