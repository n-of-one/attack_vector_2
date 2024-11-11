package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import org.n1.av2.platform.db.MigrationStep
import org.springframework.stereotype.Component

/**
 * V1
 *
 * Initial migration step.
 *
 * No operation, the collections (and thus the schema) will be created when the Spring Data repositories save objects.
 */
@Component
class V1_Initial: MigrationStep {

    override val version = 1

    override
    fun migrate(db: MongoDatabase): String {
        return "No operation. Schema will be created by adding objects through the program."
    }
}
