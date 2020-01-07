package com.sample.dao

import com.sample.model.User
import org.ehcache.CacheManagerBuilder
import org.ehcache.config.CacheConfigurationBuilder
import org.ehcache.config.ResourcePoolsBuilder
import org.ehcache.config.persistence.CacheManagerPersistenceConfiguration
import org.ehcache.config.units.EntryUnit
import org.ehcache.config.units.MemoryUnit
import java.io.File


class DAOFacadeCache(val delegate: DAOFacade, val storagePath: File) : DAOFacade {

    val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerPersistenceConfiguration(storagePath))
            .withCache("usersCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder<String, User>()
                            .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                    .heap(1000, EntryUnit.ENTRIES)
                                    .offheap(10, MemoryUnit.MB)
                                    .disk(100, MemoryUnit.MB, true)
                            )
                            .buildConfig(String::class.java, User::class.java))
            .build(true)

    val usersCache = cacheManager.getCache("usersCache", String::class.java, User::class.java)

    override fun init() {
        delegate.init()
    }

    override fun user(userId: String, hash: String?): User? {
        // Returns a cached User when available in the cache.
        val cached = usersCache.get(userId)
        val user = if (cached == null) {
            val dbUser = delegate.user(userId)
            if (dbUser != null) {
                usersCache.put(userId, dbUser)
            }
            dbUser
        } else {
            cached
        }

        // Verifies that, if specified, the hash matches to return the user.
        return when {
            user == null -> null
            hash == null -> user
            user.passwordHash == hash -> user
            else -> null
        }
    }

    override fun userByEmail(email: String): User? {
        return delegate.userByEmail(email)
    }

    override fun createUser(user: User) {
        if (usersCache.get(user.userId) != null) {
            throw IllegalStateException("User already exist")
        }

        delegate.createUser(user)
        usersCache.put(user.userId, user)
    }

    override fun close() {
        try {
            delegate.close()
        } finally {
            cacheManager.close()
        }
    }
}
