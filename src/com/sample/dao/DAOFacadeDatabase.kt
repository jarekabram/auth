package com.sample.dao

import com.sample.model.*
import org.jetbrains.exposed.sql.*
import org.joda.time.*
import java.io.*

interface DAOFacade : Closeable {

    fun init()
    fun user(userId: String, hash: String? = null): User?
    fun userByEmail(email: String): User?
    fun createUser(user: User)

}

class DAOFacadeDatabase(val db: Database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")): DAOFacade {
    constructor(dir: File) : this(Database.connect("jdbc:h2:file:${dir.canonicalFile.absolutePath}", driver = "org.h2.Driver"))

    override fun init() {
        // Create the used tables
        db.transaction {
            create(Users)
        }
    }

    override fun user(userId: String, hash: String?) = db.transaction {
        Users.select { Users.id.eq(userId) }
                .mapNotNull {
                    if (hash == null || it[Users.passwordHash] == hash) {
                        User(userId, it[Users.email], it[Users.displayName], it[Users.passwordHash])
                    } else {
                        null
                    }
                }
                .singleOrNull()
    }

    override fun userByEmail(email: String) = db.transaction {
        Users.select { Users.email.eq(email) }
                .map { User(it[Users.id], email, it[Users.displayName], it[Users.passwordHash]) }.singleOrNull()
    }

    override fun createUser(user: User) = db.transaction {
        Users.insert {
            it[id] = user.userId
            it[displayName] = user.displayName
            it[email] = user.email
            it[passwordHash] = user.passwordHash
        }
        Unit
    }

    override fun close() {
    }
}
