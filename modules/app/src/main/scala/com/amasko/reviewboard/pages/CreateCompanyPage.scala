package com.amasko.reviewboard
package pages

import com.amasko.reviewboard.common.Constants
import com.amasko.reviewboard.http.requests.CreateCompanyRequest
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html.Element
import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}
import core.ZJS.*

case class CreateCompanyState(
    override val showStatus: Boolean,
    name: String = "",
    url: String = "",
    location: Option[String] = None,
    country: Option[String] = None,
    industry: Option[String] = None,
    image: Option[String] = None,
    tags: List[String] = Nil,
    upstreamStatus: Option[Either[String, String]] = None
) extends FormState:
  override def errorList: List[Option[String]] = List(
    Option.when(name.isEmpty)("Name cannot be empty"),
    Option.when(url.matches(Constants.urlRegex))("URL can only contain letters and numbers")
  ) ++ upstreamStatus.map(_.left.toOption).toList

  override def maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)

object CreateCompanyPage extends FormPage[CreateCompanyState]("Post new company"):
  override def initialState: CreateCompanyState = CreateCompanyState(false)

  private def computeDimensions(width: Int, height: Int): (Int, Int) =
    if width > height then {
      val ratio = width * 1.0 / 256
      val w     = 256
      val h     = height / ratio
      (256, h.toInt)
    } else {
      val (newH, newW) = computeDimensions(height, width)
      newW -> newH
    }

  private val fileUploader = (files: List[dom.File]) => {
    val file = files.headOption.filter(_.size > 0)
    file.foreach { f =>
      val reader = new dom.FileReader()
      reader.onload = _ => {

        val fakeImg = dom.document.createElement("img").asInstanceOf[dom.html.Image]
        fakeImg.addEventListener(
          "load",
          _ => {
            val canvas          = dom.document.createElement("canvas").asInstanceOf[dom.html.Canvas]
            val (width, height) = computeDimensions(fakeImg.width, fakeImg.height)
            canvas.width = width
            canvas.height = height
            val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
            ctx.drawImage(fakeImg, 0, 0, width, height)
            val resized = canvas.toDataURL(f.`type`)
            stateVar.update(_.copy(image = Some(resized)))
          }
        )
        fakeImg.src = reader.result.toString
//        stateVar.update(_.copy(image = Some(result)))
      }
      reader.readAsDataURL(f)
    }
  }

  private def renderLogoUpload(uid: String, name: String, isRequired: Boolean) =
    div(
      cls := "row",
      div(
        cls := "col-md-12",
        div(
          cls := "form-input",
          label(
            forId := uid,
            cls   := "form-label",
            if isRequired then span("*") else span(),
            name
          ),
          input(
            `type` := "file",
            cls    := "form-control",
            idAttr := uid,
            accept := "image/*",
            onChange.mapToFiles --> fileUploader
          )
        )
      )
    )

  override def renderChildren(): List[ReactiveHtmlElement[Element]] = List(
    renderInput(
      "Name",
      "name-input",
      "text",
      true,
      "Company name",
      (s, v) => s.copy(name = v, showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "URL",
      "url-input",
      "text",
      true,
      "Company URL",
      (s, v) => s.copy(url = v, showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "Location",
      "location-input",
      "text",
      false,
      "Company location",
      (s, v) => s.copy(location = Some(v), showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "Country",
      "country-input",
      "text",
      false,
      "Company country",
      (s, v) => s.copy(country = Some(v), showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "Industry",
      "industry-input",
      "text",
      false,
      "Company industry",
      (s, v) => s.copy(industry = Some(v), showStatus = false, upstreamStatus = None)
    ),
    renderLogoUpload(
      "Logo",
      "image-input",
      false
    ),
//    img(
//      src <-- stateVar.signal.map(_.image.getOrElse("")) // todo temp
//    ),
    renderInput(
      "Tags - separate by commas",
      "tags-input",
      "text",
      false,
      "Scala, ZIO, Laminar",
      (s, v) =>
        s.copy(tags = v.split(",").map(_.trim).toList, showStatus = false, upstreamStatus = None)
    ),
    button(
      `type` := "button",
      pageTitle,
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    )
  )

  val submitter = Observer[CreateCompanyState] { s =>
    if s.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
      //      dom.console.log("Current State: " + s)
      callBackend(
        _.callSecure(_.companies.createEndpoint)(
          CreateCompanyRequest(
            s.name,
            s.url,
            s.location,
            s.country,
            s.industry,
            s.image,
            s.tags
          )
        )
      )
        .mapBoth(
          { err =>
            stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Left(err.getMessage))))
          },
          { _ =>
            stateVar.update(
              _.copy(
                showStatus = true,
                upstreamStatus = Some(Right("Company posted!"))
              )
            )
          }
        ).merge
        .runJs
  }
