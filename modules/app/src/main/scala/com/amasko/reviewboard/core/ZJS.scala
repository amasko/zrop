package com.amasko.reviewboard
package core

import com.raquo.airstream.eventbus.EventBus
import zio.*

object ZJS:
  extension [E <: Throwable, A](z: ZIO[BackendClient, E, A])
    def emitTo(eventBus: EventBus[A]): Unit =
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.fork(
          z.tap(event => ZIO.attempt(eventBus.emit(event)))
            .provide(BackendClientLive.configuredLayer)
        )
      }
      // todo ????
      ()

  def callBackend = ZIO.serviceWithZIO[BackendClient]
