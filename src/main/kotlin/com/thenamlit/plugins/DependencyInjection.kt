package com.thenamlit.plugins

import com.thenamlit.di.mainModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin


fun Application.configureDependencyInjection() {
    install(Koin) {
        modules(mainModule)
    }
}
