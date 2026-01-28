package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import org.n1.av2.platform.db.MigrationStep
import org.n1.av2.platform.iam.user.DefaultUserService
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.site.tutorial.TutorialService
import org.springframework.stereotype.Component

/**
 * V6
 *
 * Change how default data is imported. Rather than being created on application startup,
 * it is now created during the migration process. This way admins can remove default data
 * that they don't need, and it won't be recreated on the next startup.
 */
@Component
class V06_ImportDefaultData(
    private val tutorialService: TutorialService,
    private val defaultUserService: DefaultUserService,
) : MigrationStep {

    override val version = 6

    private val logger = mu.KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {
        createDefaultHackers()
        importDefaultTutorial()
        return "Imported default tutorial site and created default hackers."
    }

    fun importDefaultTutorial() {
        val created = tutorialService.importTutorialSite()
        if (created) {
            logger.info("Imported default tutorial site.")
        }
        else {
            logger.info("There is already a tutorial site, leaving it unchanged.")
        }

    }

    fun createDefaultHackers() {
        val users = mutableListOf<UserEntity?>()
        users.add(defaultUserService.createDefaultHacker("hacker", HackerIcon.CROCODILE))
        users.add(defaultUserService.createDefaultHacker("Stalker",HackerIcon.BEAR))
        users.add(defaultUserService.createDefaultHacker("Paradox",HackerIcon.BULL))
        users.add(defaultUserService.createDefaultHacker("Angler", HackerIcon.SHARK))

        val createdCount = users.filterNotNull().size
        logger.info("Created $createdCount new default hackers.")
    }
}
