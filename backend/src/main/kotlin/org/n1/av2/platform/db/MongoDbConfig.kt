package org.n1.av2.platform.db

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.n1.av2.platform.util.TimeService
import org.springframework.beans.factory.annotation.Value
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

    @Value("\${MONGODB_URI:mongodb://attackvector2:attackvector2@localhost/admin?authMechanism=SCRAM-SHA-1}")
    private val mongoDbUrl: String,

    ) {

    @Bean
    fun createMongoClient(): MongoClient {
        val connectionString = ConnectionString(mongoDbUrl)
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()
        return MongoClients.create(mongoClientSettings)
    }

}

@Configuration
@EnableMongoRepositories(basePackages = ["org.n1.av2"])
class MongoDbConfig(
    private val mongoClient: MongoClient,
    private val timeService: TimeService,

    @Value("\${MONGODB_NAME:attackvector2}")
    private val mongoDbName: String,

    ) : AbstractMongoClientConfiguration() {

    override fun mongoClient(): MongoClient {
        return mongoClient
    }

    override fun getDatabaseName(): String {
        return mongoDbName
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
            return ZonedDateTime.ofInstant(source.toInstant(), timeService.timeZoneId)
        }
    }

    inner class ZonedDateTimeToDateConverter : Converter<ZonedDateTime, Date> {

        override fun convert(source: ZonedDateTime): Date? {
            return Date.from(source.toInstant())
        }
    }

}
