package com.sample

import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import com.sample.dao.*

fun Route.userPage(dao: DAOFacade) {

    get<UserPage> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        val pageUser = dao.user(it.user)

        if (pageUser == null) {
            call.respond(HttpStatusCode.NotFound.description("User ${it.user} doesn't exist"))
        } else {
            val etag = (user?.userId)
            call.respond(FreeMarkerContent("user.ftl", mapOf("user" to user, "pageUser" to pageUser), etag))
        }
    }
}
