package com.sample

import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import com.sample.dao.*

/**
 * Registers the [ViewKweet] route. (/kweet/{id})
 */
fun Route.viewKweet(dao: DAOFacade, hashFunction: (String) -> String) {
    /**
     * This page shows the [Kweet] content and its replies.
     * If there is an user logged in, and the kweet is from her/him, it will provide secured links to remove it.
     */
    get<ViewKweet> {
        val user = call.sessions.get<KweetSession>()?.let { dao.user(it.userId) }
        val date = System.currentTimeMillis()
        val code = if (user != null) call.securityCode(date, user, hashFunction) else null

        call.respond(FreeMarkerContent("view-kweet.ftl", mapOf("user" to user, "kweet" to dao.getKweet(it.id), "date" to date, "code" to code), user?.userId ?: ""))
    }
}
