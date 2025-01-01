package com.amasko.reviewboard
package components

import core.ZJS.*
import domain.data.CompanyFilter
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import com.raquo.laminar.codecs.StringAsIsCodec
import zio.*

object FilterPanel:
  case class CheckedValue(groupName: String, value: String, isChecked: Boolean)

  private val group_location   = "Locations"
  private val group_countries  = "Countries"
  private val group_industries = "Industries"
  private val group_tags       = "Tags"

  private val filterBus   = Var[CompanyFilter](CompanyFilter())
  private val checkEvents = EventBus[CheckedValue]()
//  val clicks = checkEvents.events.map(_.toString).foreach(dom.console.log(_))
  private val clicks  = EventBus[Unit]() // click events on the Apply Filters button
  private val isDirty = clicks.events.mapTo(false).mergeWith(checkEvents.events.mapTo(true))

  private val filterState: Signal[CompanyFilter] = checkEvents.events
    .scanLeft(Map[String, Set[String]]()) {
      case (acc, CheckedValue(group, value, true)) =>
        acc.updatedWith(group)(_.map(_ + value).orElse(Some(Set(value))))
      case (acc, CheckedValue(group, value, false)) => acc.updatedWith(group)(_.map(_ - value))
    }
    .map(m =>
      CompanyFilter(
        locations = m.getOrElse(group_location, Set.empty).toList,
        countries = m.getOrElse(group_countries, Set.empty).toList,
        industries = m.getOrElse(group_industries, Set.empty).toList,
        tags = m.getOrElse(group_tags, Set.empty).toList
      )
    )

  val triggerFilters: EventStream[CompanyFilter] = clicks.events.withCurrentValueOf(filterState)

  def apply() =
    div(
      onMountCallback(_ =>
        callBackend(_.call(_.companies.allFilters)(())).map(filterBus.set).runJs
      ),
//      child.text <-- filterBus.signal.map(_.toString),
//      child.text <-- triggerFilters.map(_.toString),
      cls    := "accordion accordion-flush",
      idAttr := "accordionFlushExample",
      div(
        cls := "accordion-item",
        h2(
          cls    := "accordion-header",
          idAttr := "flush-headingOne",
          button(
            cls                                         := "accordion-button",
            idAttr                                      := "accordion-search-filter",
            `type`                                      := "button",
            htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
            htmlAttr("data-bs-target", StringAsIsCodec) := "#flush-collapseOne",
            htmlAttr("aria-expanded", StringAsIsCodec)  := "true",
            htmlAttr("aria-controls", StringAsIsCodec)  := "flush-collapseOne",
            div(
              cls := "jvm-recent-companies-accordion-body-heading",
              h3(
                span("Search"),
                " Filters"
              )
            )
          )
        ),
        div(
          cls                                          := "accordion-collapse collapse show",
          idAttr                                       := "flush-collapseOne",
          htmlAttr("aria-labelledby", StringAsIsCodec) := "flush-headingOne",
          htmlAttr("data-bs-parent", StringAsIsCodec)  := "#accordionFlushExample",
          div(
            cls := "accordion-body p-0",
            renderFilterOptions(group_location, _.locations),
            renderFilterOptions(group_countries, _.countries),
            renderFilterOptions(group_industries, _.industries),
            renderFilterOptions(group_tags, _.tags),
            renderApplyButton()
          )
        )
      )
    )

  private def renderApplyButton() =
    div(
      cls := "jvm-accordion-search-btn",
      button(
        disabled <-- isDirty.toSignal(false).map(!_), // todo toSignal here?
        onClick.mapTo(()) --> clicks,
        cls    := "btn btn-primary",
        `type` := "button",
        "Apply Filters"
      )
    )

  private def renderFilterOptions(groupName: String, optionsFn: CompanyFilter => List[String]) =
    div(
      cls := "accordion-item",
      h2(
        cls    := "accordion-header",
        idAttr := s"heading$groupName",
        button(
          cls                                         := "accordion-button collapsed",
          `type`                                      := "button",
          htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
          htmlAttr("data-bs-target", StringAsIsCodec) := s"#collapse$groupName",
          htmlAttr("aria-expanded", StringAsIsCodec)  := "false",
          htmlAttr("aria-controls", StringAsIsCodec)  := s"collapse$groupName",
          groupName
        )
      ),
      div(
        cls                                          := "accordion-collapse collapse",
        idAttr                                       := s"collapse$groupName",
        htmlAttr("aria-labelledby", StringAsIsCodec) := "headingOne",
        htmlAttr("data-bs-parent", StringAsIsCodec)  := "#accordionExample",
        div(
          cls := "accordion-body",
          div(
            cls := "mb-3",
//            children <-- filterBus.events.map(filt => optionsFn(filt).map { value =>
            children <-- filterBus.signal.map(filt =>
              optionsFn(filt).map { value =>
                renderCheckbox(groupName, value)
              }
            )
          )
        )
      )
    )

  private def renderCheckbox(groupName: String, value: String) =
    div(
      cls := "form-check",
      label(
        cls   := "form-check-label",
        forId := s"filter-$groupName-$value",
        value
      ),
      input(
        cls    := "form-check-input",
        `type` := "checkbox",
        idAttr := s"filter-$groupName-$value",
        onChange.mapToChecked.map(CheckedValue(groupName, value, _)) --> checkEvents
//        inContext { el =>
//          onInput.mapTo(el.checked).map(CheckedValue(groupName, value, _)) --> checkEvent
//        }
      )
    )
