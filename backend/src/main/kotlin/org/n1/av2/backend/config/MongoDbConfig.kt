package org.n1.av2.backend.config

import com.github.mongobee.Mongobee
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.n1.av2.backend.AttackVector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.stereotype.Component


/**
 * Configuration class for Mongo Db
 */
@Component
class MongoConfig {
    val clientUri: MongoClientURI
    get() {
        val envUrl = System.getenv("MONGODB_URI")
        val url =
                if (envUrl != null && !envUrl.trim().isEmpty()) envUrl
                else "mongodb://av2:av2@localhost/admin?authMechanism=SCRAM-SHA-1"
        return MongoClientURI(url)
    }

    val databaseName: String
    get() {
        val envName: String? = System.getenv("MONGODB_NAME")

        return if (envName != null && !envName.trim().isEmpty()) envName
        else "av2"
    }

}


@Configuration
@EnableMongoRepositories(basePackageClasses = [(AttackVector::class)])
class MongoDbConfig(private val mongoConfig: MongoConfig) : AbstractMongoConfiguration() {

    override fun getDatabaseName(): String {
        return mongoConfig.databaseName
    }

    override fun mongoClient(): MongoClient {
        return MongoClient(mongoConfig.clientUri)
    }

    override fun getMappingBasePackages(): Collection<String> {
        return listOf("org.n1.av2.backend.model", "org.n1.av2.backend.model.scan")
    }
}

@Configuration
class MongobeeFactory(private val mongoConfig: MongoConfig ) {

    @Bean
    fun mongobee(): Mongobee {
        val runner = Mongobee(mongoConfig.clientUri)
        runner.setDbName(mongoConfig.databaseName)
        runner.setChangeLogsScanPackage("org.n1.av2.backend.db")

        return runner
    }
}
