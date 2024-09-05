package org.n1.av2.editor

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.n1.av2.integration.service.WebsocketSiteService
import org.n1.av2.integration.stomp.StompClientService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.connection.ServerActions.*
import org.n1.av2.site.entity.SitePropertiesRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

const val EXPECT_FAIL = false
const val EXPECT_SUCCESS = true

@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
internal class EditorControllerIT {

    @Autowired
    private lateinit var sitePropertiesRepo: SitePropertiesRepo

    @Autowired
    private lateinit var stompClientService: StompClientService

    @Autowired
    private lateinit var websocketSiteService: WebsocketSiteService


    @LocalServerPort
    private val port = 0

    private val logger = mu.KotlinLogging.logger {}

    @BeforeAll
    fun setup() {
        logger.info("Setting up")
        stompClientService.setPort(port)

        websocketSiteService.importTestSite("v1-dev-editor-test.json", "Stalker")
        websocketSiteService.makeHackable("editor-test")


        tutorialSiteId = sitePropertiesRepo.findByName("editor-test")?.siteId ?: error("No site found with name 'editor-test'")
    }

    data class TestCase(
        val description: String,
        val expectSuccess: Boolean,
        val userName: String,
        val path: String,
        val payload: String,
        val responseAction: ServerActions,
        val subscription: String? = null
    )

    @ParameterizedTest
    @MethodSource
    fun runTest(testCase: TestCase): Unit = runBlocking {
        val connection = stompClientService.connectUser(testCase.userName)

        if (testCase.subscription != null) {
            connection.subscribe(testCase.subscription)
        }

        try {
            connection.send(testCase.path, testCase.payload)
            if (testCase.expectSuccess) {
                connection.waitFor(testCase.responseAction, "")
            } else {
                connection.expectToNotReceive(testCase.responseAction, "")
            }
        }
        finally {
            logger.warn("Testcase ends")
        }
    }

    companion object {

        lateinit var tutorialSiteId: String

        @JvmStatic
        fun runTest(): List<TestCase> {
            return listOf(
                TestCase( "/editor/open for gm", EXPECT_SUCCESS,"gm", "/av/editor/open", "editor-test", SERVER_OPEN_EDITOR),
                TestCase( "/editor/open for hacker", EXPECT_FAIL,"hacker", "/av/editor/open", "editor-test", SERVER_OPEN_EDITOR),

                TestCase( "/editor/siteFull for gm", EXPECT_SUCCESS,"gm", "/av/editor/siteFull", tutorialSiteId, SERVER_SITE_FULL, "/topic/site/$tutorialSiteId"),
                TestCase( "/editor/siteFull for owning hacker", EXPECT_SUCCESS,"Stalker", "/av/editor/siteFull", tutorialSiteId, SERVER_SITE_FULL, "/topic/site/$tutorialSiteId"),
                TestCase( "/editor/siteFull for non owning hacker", EXPECT_FAIL,"hacker", "/av/editor/siteFull", tutorialSiteId, SERVER_SITE_FULL, "/topic/site/$tutorialSiteId"),
            )
        }
    }
}
