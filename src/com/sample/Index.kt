package com.sample

import com.sample.dao.*
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

/**
 * Register the index route of the website.
 */
@KtorExperimentalLocationsAPI
fun Route.index(dao: DAOFacade) {
    // Uses the location feature to register a get route for '/'.
    get<Index> {
        // Tries to get the user from the session (null if failure)
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }


        // Generates an ETag unique string for this route that will be used for caching.
        val etagString = user?.userId
        val etag = etagString.hashCode()

        // Uses FreeMarker to render the page.
        call.respond(FreeMarkerContent("index.ftl", mapOf("user" to user), etag.toString()))
    }
}
