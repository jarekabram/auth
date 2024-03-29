package com.sample

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@Location("/styles/main.css")
class MainCss()

fun Route.styles() {
    get<MainCss> {
        call.respond(call.resolveResource("blog.css")!!)
    }
}
