package org.n1.av2.platform.db

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.n1.av2.platform.config.ServerConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.time.ZonedDateTime
import java.util.*


@Configuration
class MongoClientFactory(
    private val config: ServerConfig
) {

    @Bean
    fun createMongoClient(): MongoClient {
        val connectionString = ConnectionString(config.mongoDbUrl)
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()
        return MongoClients.create(mongoClientSettings)
    }
}

@Configuration
@EnableMongoRepositories(basePackages = ["org.n1.av2"])
class MongoDbConfig(
    private val config: ServerConfig,
    private val mongoClient: MongoClient,

    ) : AbstractMongoClientConfiguration() {

    override fun mongoClient(): MongoClient {
        return mongoClient
    }

    override fun getDatabaseName(): String {
        return config.mongoDbName
    }

    override fun getMappingBasePackages(): Collection<String> {
        return listOf("org.n1.av2")
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
