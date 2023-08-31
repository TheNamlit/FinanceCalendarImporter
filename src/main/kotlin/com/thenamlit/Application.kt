package com.thenamlit

import com.thenamlit.plugins.configureDependencyInjection
import com.thenamlit.plugins.configureRouting
import com.thenamlit.plugins.configureSerialization
import io.ktor.server.application.*


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDependencyInjection()
    configureSerialization()
    configureRouting()
}

/*
Dependency Injection:
https://github.com/philipplackner/SocialNetworkTwitchServer/blob/master/src/main/kotlin/com/plcoding/di/MainModule.kt


Routing Settings:
https://github.com/philipplackner/SocialNetworkTwitchServer/blob/master/src/main/kotlin/com/plcoding/plugins/Routing.kt

Route:
https://github.com/philipplackner/SocialNetworkTwitchServer/blob/master/src/main/kotlin/com/plcoding/routes/UserRoutes.kt
Service:
https://github.com/philipplackner/SocialNetworkTwitchServer/blob/master/src/main/kotlin/com/plcoding/service/UserService.kt
Repository:
https://github.com/philipplackner/SocialNetworkTwitchServer/blob/master/src/main/kotlin/com/plcoding/data/repository/user/UserRepositoryImpl.kt
 */
