package org.n1.av2.layer.other.uplink

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.hacker.hackerstate.UplinkActivity
import org.n1.av2.hacker.hackerstate.UplinkReturn
import org.n1.av2.layer.other.networkswitch.NetworkSwitchService
import org.n1.av2.layer.other.os.OsLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.RunService
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.runlink.RunLinkEntityService
import org.n1.av2.run.terminal.SyntaxHighlightingService
import org.n1.av2.run.terminal.inside.CommandMoveService
import org.n1.av2.run.timings.Timings
import org.n1.av2.run.timings.TimingsService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.NodeRepo
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.site.entity.enums.NodeType
import java.time.ZonedDateTime
import java.util.Optional

private const val USER_ID = "user-1"

/*
 Site A (run-a) has an uplink in node a03 to internal site B (run-b, start node b00).
 Site B has an uplink in node b02 to internal site C.
*/
@ExtendWith(MockKExtension::class)
class UplinkServiceTest {

    @MockK(relaxed = true) lateinit var sitePropertiesEntityService: SitePropertiesEntityService
    @MockK(relaxed = true) lateinit var runEntityService: RunEntityService
    @MockK(relaxed = true) lateinit var runService: RunService
    @MockK(relaxed = true) lateinit var runLinkEntityService: RunLinkEntityService
    @MockK(relaxed = true) lateinit var hackerStateEntityService: HackerStateEntityService
    @MockK(relaxed = true) lateinit var userEntityService: UserEntityService
    @MockK(relaxed = true) lateinit var nodeEntityService: NodeEntityService
    @MockK lateinit var nodeRepo: NodeRepo
    @MockK lateinit var networkSwitchService: NetworkSwitchService
    @MockK(relaxed = true) lateinit var commandMoveService: CommandMoveService
    @MockK(relaxed = true) lateinit var connectionService: ConnectionService
    @MockK(relaxed = true) lateinit var userTaskRunner: UserTaskRunner
    @MockK(relaxed = true) lateinit var timingsService: TimingsService
    @MockK(relaxed = true) lateinit var syntaxHighlightingService: SyntaxHighlightingService

    @InjectMockKs
    lateinit var uplinkService: UplinkService

    private val siteA = SiteProperties(siteId = "site-a", name = "a", ownerUserId = "gm", hackable = true)
    private var siteB = SiteProperties(siteId = "site-b", name = "b", ownerUserId = "gm", hackable = true, internal = true)
    private val siteC = SiteProperties(siteId = "site-c", name = "c", ownerUserId = "gm", hackable = true, internal = true)
    private val runA = Run("run-a", USER_ID, "site-a", mutableMapOf())
    private val runB = Run("run-b", USER_ID, "site-b", mutableMapOf(), parentRunId = "run-a")
    private val runC = Run("run-c", USER_ID, "site-c", mutableMapOf(), parentRunId = "run-b")
    private val uplinkToB = UplinkLayer("a03:layer-1", 1, "Uplink", "", "site-b", "b")
    private val nodes = mutableMapOf(
        "a03" to Node("a03", "site-a", NodeType.TRANSIT_2, 0, 0, mutableListOf(OsLayer("a03:layer-0000", "OS"), uplinkToB), "03"),
        "b00" to Node("b00", "site-b", NodeType.TRANSIT_2, 0, 0, mutableListOf(OsLayer("b00:layer-0000", "OS")), "00"),
        "b02" to Node("b02", "site-b", NodeType.TRANSIT_2, 0, 0, mutableListOf(OsLayer("b02:layer-0000", "OS"),
            UplinkLayer("b02:layer-1", 1, "Uplink", "", "site-c", "c")), "02"),
    )

    @BeforeEach
    fun setup() {
        every { sitePropertiesEntityService.findBySiteId(any()) } answers { listOf(siteA, siteB, siteC).find { it.siteId == firstArg() } }
        every { sitePropertiesEntityService.getBySiteId(any()) } answers { listOf(siteA, siteB, siteC).first { it.siteId == firstArg() } }
        every { runEntityService.findByRunId(any()) } answers { listOf(runA, runB, runC).find { it.runId == firstArg() } }
        every { runEntityService.getByRunId(any()) } answers { listOf(runA, runB, runC).first { it.runId == firstArg() } }
        every { nodeRepo.findById(any()) } answers { Optional.ofNullable(nodes[firstArg()]) }
        every { nodeEntityService.getById(any()) } answers { nodes[firstArg()]!! }
        every { nodeEntityService.findByNetworkId("site-b", "00") } returns nodes["b00"]
        every { networkSwitchService.isReachable(any(), any(), any()) } returns true
        every { timingsService.skillAndConfigAdjusted(any(), any()) } returns Timings("establish" to 1, "main" to 1)
        every { timingsService.UPLINK_RETURN } returns Timings("main" to 1)
        every { runLinkEntityService.hasUserRunLink(any(), any()) } returns false
    }

    private fun state(runId: String?, siteId: String?, nodeId: String?, uplinkReturns: List<UplinkReturn> = emptyList()) = HackerState(
        userId = USER_ID, connectionId = "con", runId = runId, siteId = siteId, currentNodeId = nodeId, previousNodeId = null,
        activity = HackerActivity.INSIDE, iceId = null, networkedConnectionId = null, iceConnectTimestamp = null,
        uplinkReturns = uplinkReturns,
    )

    private fun verifyTerminal(text: String) {
        verify { connectionService.replyTerminalReceive(match<String> { it.contains(text) }) }
    }

    @Test
    fun `hack refuses when the target is not hackable`() {
        siteB = siteB.copy(hackable = false)
        uplinkService.hack(uplinkToB, state("run-a", "site-a", "a03").toRunState())

        verifyTerminal("[info]b[/] is not accepting connections")
        verify(exactly = 0) { runService.leaveSite(any(), any(), any()) }
    }

    @Test
    fun `hack refuses when the target is shut down`() {
        siteB = siteB.copy(shutdownEnd = ZonedDateTime.now())
        uplinkService.hack(uplinkToB, state("run-a", "site-a", "a03").toRunState())

        verifyTerminal("[info]b[/] is shut down")
    }

    @Test
    fun `hack refuses when the uplink has no target`() {
        uplinkService.hack(UplinkLayer("a03:layer-1", 1, "Uplink"), state("run-a", "site-a", "a03").toRunState())

        verifyTerminal("No carrier")
    }

    @Test
    fun `hack fails when the target site does not exist`() {
        every { sitePropertiesEntityService.getBySiteId("site-removed") } throws IllegalStateException("No SiteProperties found for id: site-removed")

        assertThrows<IllegalStateException> {
            uplinkService.hack(UplinkLayer("a03:layer-1", 1, "Uplink", "", "site-removed", "removed"), state("run-a", "site-a", "a03").toRunState())
        }
    }

    @Test
    fun `first hack creates the run on the target, linked to the source run`() {
        every { runService.startNewRun(siteB, any(), "run-a") } returns runB

        uplinkService.hack(uplinkToB, state("run-a", "site-a", "a03").toRunState())

        assertThat(runA.uplinkRunIds).containsEntry("site-b", "run-b")
        verify { runEntityService.save(runA) }
    }

    @Test
    fun `later hacks reuse the run on the target`() {
        runA.uplinkRunIds["site-b"] = "run-b"

        uplinkService.hack(uplinkToB, state("run-a", "site-a", "a03").toRunState())

        verify(exactly = 0) { runService.startNewRun(any(), any(), any()) }
    }

    @Test
    fun `hack establishes the carrier before switching runs`() {
        runA.uplinkRunIds["site-b"] = "run-b"

        uplinkService.hack(uplinkToB, state("run-a", "site-a", "a03").toRunState())

        verify { connectionService.replyTerminalReceiveAndLocked(true, "Establishing uplink carrier...") }
        verify { userTaskRunner.queueInTicksForSite("uplink-establish", "site-a", 1, any()) }
        verify(exactly = 0) { runService.leaveSite(any(), any(), any()) }
    }

    @Test
    fun `switching is cancelled when the hacker was disconnected while establishing the carrier`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns state("run-a", "site-a", null)

        uplinkService.carrierEstablished(USER_ID, "run-a", "a03", "run-b")

        verify(exactly = 0) { runService.leaveSite(any(), any(), any()) }
    }

    @Test
    fun `once the carrier is established, the hacker leaves the source run and switches to the target run, connecting to its start node`() {
        val hackerState = state("run-a", "site-a", "a03")
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns hackerState

        uplinkService.carrierEstablished(USER_ID, "run-a", "a03", "run-b")

        verify { runService.leaveSite(hackerState, false, true) }
        verify { hackerStateEntityService.prepareUplinkArrival(hackerState, runB, "b00", listOf(UplinkReturn("run-a", "site-a", "a03")), UplinkActivity.CONNECTING) }
        verify {
            connectionService.toUser(USER_ID, ServerActions.SERVER_ENTERING_RUN, "runId" to "run-b", "siteId" to "site-b", "viaUplink" to true, "uplinkReturn" to false)
        }
    }

    @Test
    fun `entering the target run while connecting starts the connection sequence`() {
        val connecting = state("run-b", "site-b", null, listOf(UplinkReturn("run-a", "site-a", "a03"))).copy(uplinkActivity = UplinkActivity.CONNECTING)
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns connecting
        every { hackerStateEntityService.enterRunViaUplinkOutsideWhileConnecting(connecting, "con-2") } returns connecting

        uplinkService.enterSiteFromUplink(USER_ID, "run-b", "con-2")

        verify { runService.sendEnteredRun(USER_ID, runB, connecting) }
        verify { connectionService.toRun("run-b", ServerActions.SERVER_HACKER_START_UPLINK, any()) }
        verify { userTaskRunner.queueInTicksForSite("uplink-arrive", "site-b", 1, any()) }
        verify(exactly = 0) { commandMoveService.moveArrive(any(), any(), any()) }
    }

    @Test
    fun `entering the run when returning waits for the site to render`() {
        val returning = state("run-a", "site-a", null).copy(uplinkActivity = UplinkActivity.RETURNING, uplinkArrivalNodeId = "a03")
        val arrived = state("run-a", "site-a", "a03")
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns returning
        every { hackerStateEntityService.enterRunInsideBackFromUplinkedSite(returning, "con-2") } returns arrived

        uplinkService.enterSiteFromUplink(USER_ID, "run-a", "con-2")

        verify { runService.sendEnteredRun(USER_ID, runA, arrived) }
        verify { connectionService.toUser(USER_ID, ServerActions.SERVER_TERMINAL_RECEIVE, any()) }
        verify { userTaskRunner.queueInTicksForSite("uplink-return", "site-a", 1, any()) }
        verify(exactly = 0) { commandMoveService.moveArrive(any(), any(), any()) }
    }

    @Test
    fun `entering the run when returning outside enters it like a normal run`() {
        val returningOutside = state("run-a", "site-a", null).copy(uplinkActivity = UplinkActivity.RETURNING_OUTSIDE)
        val outside = state("run-a", "site-a", null).copy(activity = HackerActivity.OUTSIDE)
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns returningOutside
        every { hackerStateEntityService.enterRunOutside("site-a", USER_ID, "run-a", "con-2") } returns outside

        uplinkService.enterSiteFromUplink(USER_ID, "run-a", "con-2")

        verify { runService.sendEnteredRun(USER_ID, runA, outside) }
        verify(exactly = 0) { userTaskRunner.queueInTicksForSite(any(), any(), any(), any()) }
    }

    @Test
    fun `entering is aborted when the hacker left the run while the browser was waiting`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns state(null, null, null)

        uplinkService.enterSiteFromUplink(USER_ID, "run-b", "con-2")

        verify(exactly = 0) { runService.sendEnteredRun(any(), any(), any()) }
        verify(exactly = 0) { connectionService.toRun(any(), any(), *anyVararg()) }
    }

    @Test
    fun `after the site has rendered, the session is restored at the uplink node`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns state("run-a", "site-a", "a03")

        uplinkService.sessionRestored(USER_ID, "run-a", "a03")

        verify { commandMoveService.moveArrive("a03", USER_ID, "run-a") }
    }

    @Test
    fun `after the connection sequence the hacker arrives in the start node`() {
        val connecting = state("run-b", "site-b", null, listOf(UplinkReturn("run-a", "site-a", "a03"))).copy(uplinkActivity = UplinkActivity.CONNECTING)
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns connecting
        every { hackerStateEntityService.arriveViaUplink(connecting) } returns connecting.copy(currentNodeId = "b00", uplinkActivity = UplinkActivity.NONE)

        uplinkService.connectionSequenceFinished(USER_ID, "run-b")

        verify { commandMoveService.moveArrive("b00", USER_ID, "run-b") }
    }

    @Test
    fun `the end of the connection sequence is ignored when the hacker is no longer connecting`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns state("run-b", "site-b", "b00")

        uplinkService.connectionSequenceFinished(USER_ID, "run-b")

        verify(exactly = 0) { hackerStateEntityService.arriveViaUplink(any()) }
        verify(exactly = 0) { commandMoveService.moveArrive(any(), any(), any()) }
    }

    @Test
    fun `when the target shut down during the sequence, the hacker returns to the uplink node`() {
        siteB = siteB.copy(shutdownEnd = ZonedDateTime.now())
        val connecting = state("run-b", "site-b", null, listOf(UplinkReturn("run-a", "site-a", "a03"))).copy(uplinkActivity = UplinkActivity.CONNECTING)
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns connecting

        uplinkService.connectionSequenceFinished(USER_ID, "run-b")

        verify { hackerStateEntityService.prepareUplinkArrival(connecting, runA, "a03", emptyList(), UplinkActivity.RETURNING) }
        verify(exactly = 0) { commandMoveService.moveArrive(any(), any(), any()) }
    }

    @Test
    fun `leaving the target returns the hacker to the uplink node`() {
        val hackerState = state("run-b", "site-b", "b00", listOf(UplinkReturn("run-a", "site-a", "a03")))

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { runService.leaveSite(hackerState, false, true) }
        verify { hackerStateEntityService.prepareUplinkArrival(hackerState, runA, "a03", emptyList(), UplinkActivity.RETURNING) }
        verifyEnteringRunA()
    }

    private fun verifyEnteringRunA() {
        verify {
            connectionService.toUser(USER_ID, ServerActions.SERVER_ENTERING_RUN, "runId" to "run-a", "siteId" to "site-a", "viaUplink" to true, "uplinkReturn" to true)
        }
    }

    @Test
    fun `returns outside the source site when the uplink node is gone`() {
        nodes.remove("a03")
        val hackerState = state("run-b", "site-b", "b00", listOf(UplinkReturn("run-a", "site-a", "a03")))

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.prepareUplinkReturnOutside(hackerState, runA) }
        verify(exactly = 0) { hackerStateEntityService.prepareUplinkArrival(any(), any(), any(), any(), any()) }
        verifyEnteringRunA()
    }

    @Test
    fun `returns outside the source site when it is shut down`() {
        val shutDownA = siteA.copy(shutdownEnd = ZonedDateTime.now())
        every { sitePropertiesEntityService.findBySiteId("site-a") } returns shutDownA
        val hackerState = state("run-b", "site-b", "b00", listOf(UplinkReturn("run-a", "site-a", "a03")))

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.prepareUplinkReturnOutside(hackerState, runA) }
        verifyEnteringRunA()
    }

    @Test
    fun `in a chain, falls back past an internal site whose uplink node is gone`() {
        nodes.remove("b02")
        val returns = listOf(UplinkReturn("run-a", "site-a", "a03"), UplinkReturn("run-b", "site-b", "b02"))
        val hackerState = state("run-c", "site-c", "c00", returns)

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.prepareUplinkArrival(hackerState, runA, "a03", emptyList(), UplinkActivity.RETURNING) }
    }

    @Test
    fun `in a chain, returns to the uplink node in the previous internal site`() {
        val returns = listOf(UplinkReturn("run-a", "site-a", "a03"), UplinkReturn("run-b", "site-b", "b02"))
        val hackerState = state("run-c", "site-c", "c00", returns)

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.prepareUplinkArrival(hackerState, runB, "b02", listOf(UplinkReturn("run-a", "site-a", "a03")), UplinkActivity.RETURNING) }
    }

    @Test
    fun `goes home when there is nothing to return to`() {
        every { runEntityService.findByRunId("run-a") } returns null
        val hackerState = state("run-b", "site-b", "b00", listOf(UplinkReturn("run-a", "site-a", "a03")))

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.leaveSite(hackerState) }
        verify { connectionService.toUser(USER_ID, ServerActions.SERVER_RETURN_HOME) }
    }
}
