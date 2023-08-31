package com.thenamlit.features.forex_factory_calendar.resource.routing

import io.ktor.resources.*


@Resource("forex_factory_calendar")
class ForexFactoryCalendar(val year: Int, val month: String)
