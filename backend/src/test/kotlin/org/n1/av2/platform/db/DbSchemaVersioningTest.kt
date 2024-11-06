package org.n1.av2.platform.db

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.n1.av2.platform.config.StaticConfig
import java.time.ZonedDateTime

@ExtendWith(MockKExtension::class)
class DbSchemaVersioningTest {

    @MockK
    lateinit var dbSchemaVersionRepository: DbSchemaVersionRepository

    @MockK
    lateinit var mongoClient: MongoClient

    @MockK
    lateinit var migrationSteps: List<MigrationStep>

    @MockK
    lateinit var staticConfig: StaticConfig


    @InjectMockKs
    lateinit var dbSchemaVersioning: DbSchemaVersioning


    @Test
    fun `test no upgrade when in sync`() {
        // arrange
        val v1Step = TestStep(1)

        every { dbSchemaVersionRepository.findAll() } returns listOf(DbSchemaVersion(1, "v1 description", ZonedDateTime.now()))
        every { migrationSteps.iterator() } returns listOf(v1Step).iterator()

        val savedRecords = mutableListOf<DbSchemaVersion>()
        every { dbSchemaVersionRepository.save(capture(savedRecords)) } returns mockk()

        // act
        dbSchemaVersioning.upgrade()

        // assert
        assertThat(savedRecords).isEmpty()
        assertThat(v1Step.called).isFalse
    }

    @Test
    fun `test upgrade from v1 to v3`() {
        // arrange
        val v1Step = TestStep(1)
        val v2Step = TestStep(2)
        val v3Step = TestStep(3)

        every { dbSchemaVersionRepository.findAll() } returns listOf(DbSchemaVersion(1, "v1 description", ZonedDateTime.now()))
        every { migrationSteps.iterator() } returns listOf(v1Step, v2Step, v3Step).iterator()
        every { staticConfig.mongoDbName } returns "db-name"
        every { mongoClient.getDatabase("db-name") } returns mockk()

        val savedRecords = mutableListOf<DbSchemaVersion>()
        every { dbSchemaVersionRepository.save(capture(savedRecords)) } returns mockk()

        // act
        dbSchemaVersioning.upgrade()

        // assert
        assertThat(v1Step.called).isFalse
        assertThat(v2Step.called).isTrue
        assertThat(v3Step.called).isTrue

        assertThat(savedRecords).hasSize(2)
        assertThat(savedRecords[0].version).isEqualTo(2)
        assertThat(savedRecords[1].version).isEqualTo(3)
    }
}

class TestStep(override val version: Int) : MigrationStep {

    var called: Boolean = false

    override fun migrate(db: MongoDatabase): String {
        called = true
        return "v$version description"
    }
}
