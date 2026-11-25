package org.n1.av2.run

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.terminal.SyntaxHighlightingService
import org.n1.av2.run.terminal.inside.CommandMoveService
import org.n1.av2.run.timings.Timings
import org.n1.av2.run.timings.TimingsService
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesEntityService
import java.time.ZonedDateTime

private const val USER_ID = "user-1"

@ExtendWith(MockKExtension::class)
class RunIngressServiceTest {

    @MockK(relaxed = true) lateinit var runEntityService: RunEntityService
    @MockK(relaxed = true) lateinit var runService: RunService
    @MockK(relaxed = true) lateinit var hackerStateEntityService: HackerStateEntityService
    @MockK(relaxed = true) lateinit var sitePropertiesEntityService: SitePropertiesEntityService
    @MockK(relaxed = true) lateinit var connectionService: ConnectionService
    @MockK(relaxed = true) lateinit var userTaskRunner: UserTaskRunner
    @MockK(relaxed = true) lateinit var timingsService: TimingsService
    @MockK(relaxed = true) lateinit var syntaxHighlightingService: SyntaxHighlightingService
    @MockK(relaxed = true) lateinit var commandMoveService: CommandMoveService

    @InjectMockKs
    lateinit var runIngressService: RunIngressService

    private val run = Run("run-1", USER_ID, "site-1", mutableMapOf())
    private var site = SiteProperties(siteId = "site-1", name = "site", ownerUserId = "gm", hackable = true)

    @BeforeEach
    fun setup() {
        every { runEntityService.getByRunId("run-1") } returns run
        every { sitePropertiesEntityService.getBySiteId("site-1") } answers { site }
        every { timingsService.skillAndConfigAdjusted(any(), any()) } returns Timings("main" to 5)
    }

    private fun connecting(ingressNodeId: String? = "n05") = HackerState(
        userId = USER_ID, connectionId = "con", runId = "run-1", siteId = "site-1", currentNodeId = null, previousNodeId = null,
        activity = HackerActivity.OUTSIDE, iceId = null, networkedConnectionId = null, iceConnectTimestamp = null,
        ingressNodeId = ingressNodeId,
    )

    @Test
    fun `connecting reveals the ingress node and announces the connection to the run`() {
        val outside = connecting(ingressNodeId = null)

        runIngressService.connect(outside, "n05", quick = false, uplink = null)

        verify { runService.revealNode(run, "n05") }
        verify { hackerStateEntityService.startConnecting(outside, "n05") }
        verify { connectionService.toRun("run-1", ServerActions.SERVER_HACKER_START_ATTACK, any()) }
        verify { userTaskRunner.queueInTicksForSite("ingress-arrive", "site-1", 5, any()) }
        verify(exactly = 0) { commandMoveService.moveArrive(any(), any(), any()) }
    }

    @Test
    fun `connecting via an uplink uses the uplink timings`() {
        val uplinkTimings = Timings("main" to 7)
        every { timingsService.START_UPLINK } returns uplinkTimings

        runIngressService.connect(connecting(), "n05", quick = false, UplinkConnectInfo("a", "b"))

        verify { timingsService.skillAndConfigAdjusted(uplinkTimings, USER_ID) }
    }

    @Test
    fun `at the end of the connection sequence the hacker arrives at the ingress node`() {
        val hackerState = connecting()
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns hackerState

        runIngressService.ingressArrive(USER_ID, "run-1", "n05")

        verify { hackerStateEntityService.arriveAtIngress(hackerState) }
        verify { commandMoveService.moveArrive("n05", USER_ID, "run-1") }
    }

    @Test
    fun `arrival is ignored when the hacker left the run during the connection sequence`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns connecting().copy(runId = null, siteId = null)

        runIngressService.ingressArrive(USER_ID, "run-1", "n05")

        verify(exactly = 0) { hackerStateEntityService.arriveAtIngress(any()) }
        verify(exactly = 0) { commandMoveService.moveArrive(any(), any(), any()) }
    }

    @Test
    fun `arrival is ignored when the hacker is connecting to another ingress node`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns connecting(ingressNodeId = "n00")

        runIngressService.ingressArrive(USER_ID, "run-1", "n05")

        verify(exactly = 0) { commandMoveService.moveArrive(any(), any(), any()) }
    }

    @Test
    fun `when the site shut down during the connection sequence, the hacker is disconnected`() {
        site = site.copy(shutdownEnd = ZonedDateTime.now())
        val hackerState = connecting()
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns hackerState

        runIngressService.ingressArrive(USER_ID, "run-1", "n05")

        verify { runService.hackerDisconnect(hackerState, match { it.contains("is shut down") }) }
        verify(exactly = 0) { commandMoveService.moveArrive(any(), any(), any()) }
    }
}
