package com.sample

import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import com.sample.dao.*
import com.sample.model.*

fun Route.register(dao: DAOFacade, hashFunction: (String) -> String) {

    post<Register> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        if (user != null) return@post call.redirect(UserPage(user.userId))

        val registration = call.receive<Parameters>()
        val userId = registration["userId"] ?: return@post call.redirect(it)
        val password = registration["password"] ?: return@post call.redirect(it)
        val displayName = registration["displayName"] ?: return@post call.redirect(it)
        val email = registration["email"] ?: return@post call.redirect(it)

        val error = Register(userId, displayName, email)
        when {
            password.length < 6 -> call.redirect(error.copy(error = "Password should be at least 6 characters long"))
            userId.length < 4 -> call.redirect(error.copy(error = "Login should be at least 4 characters long"))
            !validateUserName(userId) -> call.redirect(error.copy(error = "Login should be consists of digits, letters, dots or underscores"))
            dao.user(userId) != null -> call.redirect(error.copy(error = "User with the following login is already registered"))
            else -> {
                val hash = hashFunction(password)
                val newUser = User(userId, email, displayName, hash)

                try {
                    dao.createUser(newUser)
                } catch (e: Throwable) {
                    when {
                        // NOTE: This is security issue that allows to enumerate/verify registered users. Do not do this in real app :)
                        dao.user(userId) != null -> call.redirect(error.copy(error = "User with the following login is already registered"))
                        dao.userByEmail(email) != null -> call.redirect(error.copy(error = "User with the following email $email is already registered"))
                        else -> {
                            application.log.error("Failed to register user", e)
                            call.redirect(error.copy(error = "Failed to register"))
                        }
                    }
                }

                call.sessions.set(Session(newUser.userId))
                call.redirect(UserPage(newUser.userId))
            }
        }
    }

    get<Register> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        if (user != null) {
            call.redirect(UserPage(user.userId))
        } else {
            call.respond(FreeMarkerContent("register.ftl", mapOf("pageUser" to User(it.userId, it.email, it.displayName, ""), "error" to it.error), ""))
        }
    }
}
