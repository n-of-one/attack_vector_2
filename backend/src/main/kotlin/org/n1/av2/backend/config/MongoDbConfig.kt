package org.n1.av2.backend.config

import com.github.mongobee.Mongobee
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.n1.av2.backend.AttackVector
import org.n1.av2.backend.service.TimeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*


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
class MongoDbConfig(private val mongoConfig: MongoConfig, timeService: TimeService) : AbstractMongoConfiguration() {

    val zoneId = timeService.zoneId

    override fun getDatabaseName(): String {
        return mongoConfig.databaseName
    }

    override fun mongoClient(): MongoClient {
        return MongoClient(mongoConfig.clientUri)
    }

    override fun getMappingBasePackages(): Collection<String> {
        return listOf("org.n1.av2.backend.model", "org.n1.av2.backend.model.scan")
    }

    @Bean
    override fun customConversions(): org.springframework.data.convert.CustomConversions {
        val converters = ArrayList<Converter<*, *>>()
        converters.add(DateToZonedDateTimeConverter())
        converters.add(ZonedDateTimeToDateConverter())
        return MongoCustomConversions(converters)
    }


    inner class DateToZonedDateTimeConverter : Converter<Date, ZonedDateTime> {

        override fun convert(source: Date?): ZonedDateTime? {
            return if (source == null) null else ZonedDateTime.ofInstant(source.toInstant(), zoneId)
        }
    }

    inner class ZonedDateTimeToDateConverter : Converter<ZonedDateTime, Date> {

        override fun convert(source: ZonedDateTime?): Date? {
            return if (source == null) null else Date.from(source.toInstant())
        }
    }

}

@Configuration
class MongobeeConfiguration(private val mongoConfig: MongoConfig ) {

    @Bean
    fun mongobee(): Mongobee {
        val runner = Mongobee(mongoConfig.clientUri)
        runner.setDbName(mongoConfig.databaseName)
        runner.setChangeLogsScanPackage("org.n1.av2.backend.model.db.changelog")

        return runner
    }
}
