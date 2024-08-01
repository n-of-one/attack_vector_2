package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import org.n1.av2.platform.db.MigrationStep
import org.springframework.stereotype.Component

@Component
class V1_Initial: MigrationStep {

    override
    fun version() = 1

    override
    fun migrate(db: MongoDatabase): String {
        return "No operation. Schema will be created by adding objects through the program"
    }
}
