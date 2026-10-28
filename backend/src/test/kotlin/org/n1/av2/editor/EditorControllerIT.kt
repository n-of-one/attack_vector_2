package org.n1.av2.editor

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.n1.av2.integration.IntegrationTestBase
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.connection.ServerActions.SERVER_OPEN_EDITOR
import org.n1.av2.platform.connection.ServerActions.SERVER_SITE_FULL
import org.n1.av2.site.entity.SiteProperties

const val EXPECT_FAIL = false
const val EXPECT_SUCCESS = true

internal class EditorControllerIT : IntegrationTestBase() {

    private val logger = KotlinLogging.logger {}

    @BeforeEach
    fun stubSite() {
        val siteProperties = SiteProperties(siteId = SITE_ID, name = SITE_NAME, ownerUserId = stalkerUser.id, hackable = true)
        given(sitePropertiesRepo.findByName(SITE_NAME)).willReturn(siteProperties)
        given(sitePropertiesRepo.findBySiteId(SITE_ID)).willReturn(siteProperties)
        given(siteEditorStateRepo.findBySiteId(SITE_ID)).willReturn(SiteEditorState(SITE_ID))
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
            logger.warn { "Testcase ends" }
        }
    }

    companion object {

        const val SITE_ID = "site-editor-test"
        const val SITE_NAME = "editor-test"

        @JvmStatic
        fun runTest(): List<TestCase> {
            return listOf(
                TestCase( "/editor/open for gm", EXPECT_SUCCESS,"gm", "/av/editor/open", SITE_NAME, SERVER_OPEN_EDITOR),
                TestCase( "/editor/open for hacker", EXPECT_FAIL,"hacker", "/av/editor/open", SITE_NAME, SERVER_OPEN_EDITOR),

                TestCase( "/editor/enter for gm", EXPECT_SUCCESS,"gm", "/av/editor/enter", SITE_ID, SERVER_SITE_FULL, "/topic/site/$SITE_ID"),
                TestCase( "/editor/enter for owning hacker", EXPECT_SUCCESS,"Stalker", "/av/editor/enter", SITE_ID, SERVER_SITE_FULL, "/topic/site/$SITE_ID"),
                TestCase( "/editor/enter for non owning hacker", EXPECT_FAIL,"hacker", "/av/editor/enter", SITE_ID, SERVER_SITE_FULL, "/topic/site/$SITE_ID"),
            )
        }
    }
}
