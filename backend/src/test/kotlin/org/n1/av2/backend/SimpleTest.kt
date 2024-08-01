package org.n1.av2.backend


import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.n1.av2.AttackVector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles


@ActiveProfiles("test")
@SpringBootTest(classes = [AttackVector::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ExampleIT {
    @Test
    fun example(@Autowired mongoTemplate: MongoTemplate) {
        Assertions.assertNotNull(mongoTemplate.db)
        val collectionNames = mongoTemplate.db
            .listCollectionNames()
            .into(ArrayList())
        org.assertj.core.api.Assertions.assertThat(collectionNames).isNotEmpty()
    }
}
