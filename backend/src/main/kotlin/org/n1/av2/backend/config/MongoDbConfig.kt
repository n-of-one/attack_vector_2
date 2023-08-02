package org.n1.av2.backend.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.n1.av2.backend.AttackVector
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.time.ZonedDateTime
import java.util.*


@Configuration
@EnableMongoRepositories(basePackageClasses = [(AttackVector::class)])
class MongoDbConfig(
    val config: ServerConfig

) : AbstractMongoClientConfiguration() {

    override fun mongoClient(): MongoClient {
        val connectionString = ConnectionString(config.mongoDbUrl)
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()
        return MongoClients.create(mongoClientSettings)
    }

    override fun getDatabaseName(): String {
        return config.mongoDbName
    }


    override fun getMappingBasePackages(): Collection<String> {
        return listOf("org.n1.av2.backend.model", "org.n1.av2.backend.model.scan")
    }


    override fun configureConverters(converterConfigurationAdapter: MongoConverterConfigurationAdapter) {
        converterConfigurationAdapter.registerConverter(DateToZonedDateTimeConverter())
        converterConfigurationAdapter.registerConverter(ZonedDateTimeToDateConverter())
    }


    inner class DateToZonedDateTimeConverter : Converter<Date, ZonedDateTime> {

        override fun convert(source: Date): ZonedDateTime? {
            return ZonedDateTime.ofInstant(source.toInstant(), config.timeZoneId)
        }
    }

    inner class ZonedDateTimeToDateConverter : Converter<ZonedDateTime, Date> {

        override fun convert(source: ZonedDateTime): Date? {
            return Date.from(source.toInstant())
        }
    }

}
