package com.sample.dao

import com.sample.model.*
import org.jetbrains.exposed.sql.*
import org.joda.time.*
import java.io.*

/**
 * A DAO Facade interface for the Database. This allows to provide several implementations.
 *
 * In this case this is used to provide a Database-based implementation using Exposed,
 * and a cache implementation composing another another DAOFacade.
 */
interface DAOFacade : Closeable {
    /**
     * Initializes all the required data.
     * In this case this should initialize the Users table.
     */
    fun init()

    /**
     * Tries to get an user from its [userId] and optionally its password [hash].
     * If the [hash] is specified, the password [hash] must match, or the function will return null.
     * If no [hash] is specified, it will return the [User] if exists, or null otherwise.
     */
    fun user(userId: String, hash: String? = null): User?

    /**
     * Tries to get an user from its [email].
     *
     * Returns null if no user has this [email] associated.
     */
    fun userByEmail(email: String): User?

    /**
     * Creates a new [user] in the database from its object [User] representation.
     */
    fun createUser(user: User)
}

/**
 * Database implementation of the facade.
 * Uses Exposed, and either an in-memory H2 database or a file-based H2 database by default.
 * But can be configured.
 */
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
