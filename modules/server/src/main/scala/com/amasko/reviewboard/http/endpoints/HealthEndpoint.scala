package com.amasko.reviewboard.http.endpoints

import sttp.tapir.*

trait HealthEndpoint:

  val healthEndpoint = endpoint.get
    .in("health")
    .out(stringBody)
    .description("Health check endpoint")
    .name("health")

end HealthEndpoint
