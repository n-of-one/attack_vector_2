package org.n1.mainframe.backend.config

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
        val envUrl = System.getenv("MONGODB_URI")
        val url =
                if (envUrl != null && !envUrl.trim().isEmpty()) envUrl
                else "mongodb://av2:av2@localhost/admin?authMechanism=SCRAM-SHA-1"
        clientUri = MongoClientURI(url)
    }


    override fun getDatabaseName(): String {
        val envName: String? = System.getenv("MONGODB_NAME")

        return if (envName != null && !envName.trim().isEmpty()) envName
        else "av2"
    }

    override fun mongoClient(): MongoClient {
        return MongoClient(clientUri)
    }

    override fun getMappingBasePackage(): String {
        return "org.n_is_1._attack_vector"
    }
}