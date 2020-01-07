package com.sample

import com.mchange.v2.c3p0.*
import freemarker.cache.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import org.h2.*
import org.jetbrains.exposed.sql.*
import java.io.*
import java.net.*
import javax.crypto.*
import javax.crypto.spec.*
import com.sample.dao.*
import com.sample.model.*

@Location("/")
class Index()

@Location("/user/{user}")
data class UserPage(val user: String)

@Location("/register")
data class Register(val userId: String = "", val displayName: String = "", val email: String = "", val error: String = "")

@Location("/login")
data class Login(val userId: String = "", val error: String = "")

@Location("/logout")
class Logout()

data class Session(val userId: String)

val hexValue = hex("6819b57a326945c1968f45236589")

val dir = File("build/db")
val databaseSettings = ComboPooledDataSource().apply {
    driverClass = Driver::class.java.name
    jdbcUrl = "jdbc:h2:file:${dir.canonicalFile.absolutePath}"
    user = ""
    password = ""
}

val hashedKey = SecretKeySpec(hexValue, "HmacSHA1")

val dao: DAOFacade = DAOFacadeCache(DAOFacadeDatabase(Database.connect(databaseSettings)), File(dir.parentFile, "ehcache"))


fun Application.main() {
    dao.init()
    environment.monitor.subscribe(ApplicationStopped) { databaseSettings.close() }
    mainWithDependencies(dao)
}

fun Application.mainWithDependencies(dao: DAOFacade) {
    install(DefaultHeaders)
    install(CallLogging)
    install(ConditionalHeaders)
    install(PartialContent)
    install(Locations)
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Sessions) {
        cookie<Session>("SESSION") {
            transform(SessionTransportTransformerMessageAuthentication(hexValue))
        }
    }

    val hash = { s: String -> hashString(s) }

    routing {
        styles()
        index(dao)
        userPage(dao)
        login(dao, hash)
        register(dao, hash)
    }
}

fun hashString(password: String): String {
    val algorithm = Mac.getInstance("HmacSHA1")
    algorithm.init(hashedKey)
    return hex(algorithm.doFinal(password.toByteArray(Charsets.UTF_8)))
}

suspend fun ApplicationCall.redirect(location: Any) {
    val host = request.host() ?: "localhost"
    val portSpec = request.port().let { if (it == 80) "" else ":$it" }
    val address = host + portSpec

    respondRedirect("http://$address${application.locations.href(location)}")
}

fun ApplicationCall.securityCode(date: Long, user: User, hash: (String) -> String) =
        hash("$date:${user.userId}:${request.host()}:${refererHost()}")


fun ApplicationCall.refererHost() = request.header(HttpHeaders.Referrer)?.let { URI.create(it).host }

private val userIdRegexPattern = "[a-zA-Z0-9_\\.]+".toRegex()

internal fun validateUserName(userId: String) = userId.matches(userIdRegexPattern)
