package com.amasko.reviewboard
package core

import com.raquo.laminar.api.L.{*, given}
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

    def runJs: Unit =
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.fork(z.provide(BackendClientLive.configuredLayer))
      }

    def toEventSteam: EventStream[A] =
      val bus = EventBus[A]()
      emitTo(bus)
      bus.events

  def callBackend = ZIO.serviceWithZIO[BackendClient]
