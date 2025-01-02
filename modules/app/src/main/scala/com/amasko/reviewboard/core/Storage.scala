package com.amasko.reviewboard
package core

import org.scalajs.dom
import zio.json.*

object Storage:
  def set[A](key: String, value: A)(using encoder: JsonEncoder[A]): Unit =
    dom.window.localStorage.setItem(key, value.toJson)

  def get[A: JsonDecoder](key: String): Option[A] =
    Option(dom.window.localStorage.getItem(key)).filter(_.nonEmpty).flatMap(_.fromJson[A].toOption)

  def remove(key: String): Unit =
    dom.window.localStorage.removeItem(key)
