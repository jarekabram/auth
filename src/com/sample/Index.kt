package com.sample

import com.sample.dao.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

@KtorExperimentalLocationsAPI
fun Route.index(dao: DAOFacade) {
    get<Index> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }

        val etagString = user?.userId
        val etag = etagString.hashCode()

        call.respond(FreeMarkerContent("index.ftl", mapOf("user" to user), etag.toString()))
    }
}
