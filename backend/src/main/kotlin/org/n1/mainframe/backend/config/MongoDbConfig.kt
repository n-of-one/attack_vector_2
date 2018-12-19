package org.n1.mainframe.backend.config

import com.mongodb.Mongo
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.n1.mainframe.backend.AttackVector
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
 * Configuration class for Mongo Db
 */
@Configuration
@EnableMongoRepositories(basePackageClasses = [(AttackVector::class)])
class MongoDbConfig : AbstractMongoConfiguration() {

    private val clientUri: MongoClientURI

    init {
        val url = System.getenv("MONGODB_URI")
        if (url == null || url.trim { it <= ' ' }.isEmpty()) {
            throw RuntimeException("No mongo db URL set, check env variable: MONGODB_URI")
        }
        clientUri = MongoClientURI(url)
    }


    override fun getDatabaseName(): String {
        var dbName: String? = System.getenv("MONGODB_NAME")
        if (dbName == null || dbName.trim { it <= ' ' }.isEmpty()) {
            dbName = clientUri.database
        }
        println("Using database: " + dbName!!)
        return dbName
    }

    override fun mongoClient(): MongoClient {
        return MongoClient(clientUri)
    }

    override fun getMappingBasePackage(): String {
        return "org.n_is_1._attack_vector"
    }
}