package com.thenamlit.plugins

import com.thenamlit.features.forex_factory_calendar.resource.routing.forexFactoryRouting
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*


fun Application.configureRouting() {
    install(Resources)

    routing {
        forexFactoryRouting()
    }
}
