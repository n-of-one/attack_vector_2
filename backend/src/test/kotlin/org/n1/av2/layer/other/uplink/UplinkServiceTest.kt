package org.n1.av2.layer.other.uplink

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
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
import org.n1.av2.run.RunIngressService
import org.n1.av2.run.RunService
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.runlink.RunLinkEntityService
import org.n1.av2.run.runlink.RunLinkService
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
 Site A (run-a) has an uplink in node a03 to internal site B (run-b, start node b00). The hacker entered site A at node a01.
 Site B has an uplink in node b02 to internal site C.
*/
@ExtendWith(MockKExtension::class)
class UplinkServiceTest {

    @MockK(relaxed = true) lateinit var sitePropertiesEntityService: SitePropertiesEntityService
    @MockK(relaxed = true) lateinit var runEntityService: RunEntityService
    @MockK(relaxed = true) lateinit var runService: RunService
    @MockK(relaxed = true) lateinit var runIngressService: RunIngressService
    @MockK(relaxed = true) lateinit var runLinkEntityService: RunLinkEntityService
    @MockK(relaxed = true) lateinit var runLinkService: RunLinkService
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
    private val returnToA = UplinkReturn("run-a", "site-a", "a03", "a01")

    @BeforeEach
    fun setup() {
        every { sitePropertiesEntityService.findBySiteId(any()) } answers { listOf(siteA, siteB, siteC).find { it.siteId == firstArg() } }
        every { sitePropertiesEntityService.getBySiteId(any()) } answers { listOf(siteA, siteB, siteC).first { it.siteId == firstArg() } }
        every { runEntityService.findByRunId(any()) } answers { listOf(runA, runB, runC).find { it.runId == firstArg() } }
        every { runEntityService.getByRunId(any()) } answers { listOf(runA, runB, runC).first { it.runId == firstArg() } }
        every { runService.findRunOfUserForSite(any(), any()) } returns null
        every { nodeRepo.findById(any()) } answers { Optional.ofNullable(nodes[firstArg()]) }
        every { nodeEntityService.getById(any()) } answers { nodes[firstArg()]!! }
        every { nodeEntityService.findByNetworkId(any(), any()) } answers { nodes.values.find { it.siteId == firstArg() && it.networkId == secondArg() } }
        every { networkSwitchService.isReachable(any(), any(), any(), any()) } returns true
        every { timingsService.skillAndConfigAdjusted(any(), any()) } returns Timings("establish" to 1, "main" to 1)
        every { timingsService.UPLINK_RETURN } returns Timings("main" to 1)
        every { runLinkEntityService.hasUserRunLink(any(), any()) } returns false
    }

    private fun state(runId: String?, siteId: String?, nodeId: String?, uplinkReturns: List<UplinkReturn> = emptyList(), ingressNodeId: String? = null) = HackerState(
        userId = USER_ID, connectionId = "con", runId = runId, siteId = siteId, currentNodeId = nodeId, previousNodeId = null,
        activity = HackerActivity.INSIDE, iceId = null, networkedConnectionId = null, iceConnectTimestamp = null,
        ingressNodeId = ingressNodeId, uplinkReturns = uplinkReturns,
    )

    private fun inA() = state("run-a", "site-a", "a03", ingressNodeId = "a01")

    private fun verifyTerminal(text: String) {
        verify { connectionService.replyTerminalReceive(match<String> { it.contains(text) }) }
    }

    @Test
    fun `hack refuses when the target is not hackable`() {
        siteB = siteB.copy(hackable = false)
        uplinkService.hack(uplinkToB, inA().toRunState())

        verifyTerminal("[info]b[/] is not accepting connections")
        verify(exactly = 0) { runService.leaveSite(any(), any(), any()) }
    }

    @Test
    fun `hack refuses when the target is shut down`() {
        siteB = siteB.copy(shutdownEnd = ZonedDateTime.now())
        uplinkService.hack(uplinkToB, inA().toRunState())

        verifyTerminal("[info]b[/] is shut down")
    }

    @Test
    fun `hack refuses when the uplink has no target`() {
        uplinkService.hack(UplinkLayer("a03:layer-1", 1, "Uplink"), inA().toRunState())

        verifyTerminal("No carrier")
    }

    @Test
    fun `hack refuses when the target node does not exist`() {
        uplinkService.hack(UplinkLayer("a03:layer-1", 1, "Uplink", "", "site-b", "b", "09"), inA().toRunState())

        verifyTerminal("No carrier")
        verify(exactly = 0) { userTaskRunner.queueInTicksForSite(any(), any(), any(), any()) }
    }

    @Test
    fun `hack fails when the target site does not exist`() {
        every { sitePropertiesEntityService.getBySiteId("site-removed") } throws IllegalStateException("No SiteProperties found for id: site-removed")

        assertThrows<IllegalStateException> {
            uplinkService.hack(UplinkLayer("a03:layer-1", 1, "Uplink", "", "site-removed", "removed"), inA().toRunState())
        }
    }

    @Test
    fun `first hack creates the run on the target, linked to the source run`() {
        every { runService.startNewRun(siteB, any(), "run-a") } returns runB

        uplinkService.hack(uplinkToB, inA().toRunState())

        assertThat(runA.uplinkRunIds).containsEntry("site-b", "run-b")
        verify { runEntityService.save(runA) }
    }

    @Test
    fun `hack sends the hacker their runs, so the run on the target is known before its run info is updated`() {
        runA.uplinkRunIds["site-b"] = "run-b"

        uplinkService.hack(uplinkToB, inA().toRunState())

        verify { runLinkEntityService.createRunLink("run-b", any()) }
        verify { runLinkService.sendRunInfosToUser(USER_ID) }
    }

    @Test
    fun `first hack continues the run the hacker already has on the target`() {
        val ownRunOnB = Run("run-b-own", USER_ID, "site-b", mutableMapOf())
        every { runService.findRunOfUserForSite(USER_ID, "site-b") } returns ownRunOnB

        uplinkService.hack(uplinkToB, inA().toRunState())

        assertThat(runA.uplinkRunIds).containsEntry("site-b", "run-b-own")
        verify(exactly = 0) { runService.startNewRun(any(), any(), any()) }
    }

    @Test
    fun `later hacks reuse the run on the target`() {
        runA.uplinkRunIds["site-b"] = "run-b"

        uplinkService.hack(uplinkToB, inA().toRunState())

        verify(exactly = 0) { runService.findRunOfUserForSite(any(), any()) }
        verify(exactly = 0) { runService.startNewRun(any(), any(), any()) }
    }

    @Test
    fun `hack establishes the carrier before switching runs`() {
        runA.uplinkRunIds["site-b"] = "run-b"

        uplinkService.hack(uplinkToB, inA().toRunState())

        verify { connectionService.replyTerminalReceiveAndLocked(true, "Establishing uplink carrier...") }
        verify { userTaskRunner.queueInTicksForSite("uplink-establish", "site-a", 1, any()) }
        verify(exactly = 0) { runService.leaveSite(any(), any(), any()) }
    }

    @Test
    fun `switching is cancelled when the hacker was disconnected while establishing the carrier`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns state("run-a", "site-a", null)

        uplinkService.carrierEstablished(USER_ID, "run-a", "a03", "run-b", "b00")

        verify(exactly = 0) { runService.leaveSite(any(), any(), any()) }
    }

    @Test
    fun `once the carrier is established, the hacker leaves the source run and switches to the target run, connecting to its start node`() {
        val hackerState = inA()
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns hackerState

        uplinkService.carrierEstablished(USER_ID, "run-a", "a03", "run-b", "b00")

        verify { runService.revealNode(runB, "b00") }
        verify { runService.leaveSite(hackerState, false, true) }
        verify { hackerStateEntityService.prepareUplinkConnect(hackerState, runB, "b00", listOf(returnToA)) }
        verify {
            connectionService.toUser(USER_ID, ServerActions.SERVER_ENTERING_RUN, "runId" to "run-b", "siteId" to "site-b", "viaUplink" to true, "uplinkReturn" to false)
        }
    }

    @Test
    fun `an uplink with a target node connects the hacker to that node`() {
        runA.uplinkRunIds["site-b"] = "run-b"
        val hackerState = inA()
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns hackerState
        val establish = slot<() -> Unit>()
        every { userTaskRunner.queueInTicksForSite("uplink-establish", any(), any(), capture(establish)) } returns Unit

        uplinkService.hack(UplinkLayer("a03:layer-1", 1, "Uplink", "", "site-b", "b", "02"), hackerState.toRunState())
        establish.captured()

        verify { runService.revealNode(runB, "b02") }
        verify { hackerStateEntityService.prepareUplinkConnect(hackerState, runB, "b02", listOf(returnToA)) }
    }

    @Test
    fun `no carrier when the target node was removed while establishing the carrier`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns inA()

        uplinkService.carrierEstablished(USER_ID, "run-a", "a03", "run-b", "b05")

        verify { connectionService.replyTerminalReceiveAndLocked(false, match { it.contains("No carrier") }) }
        verify(exactly = 0) { runService.leaveSite(any(), any(), any()) }
    }

    @Test
    fun `entering the target run while connecting connects the hacker to the ingress node`() {
        val connecting = state("run-b", "site-b", null, listOf(returnToA), ingressNodeId = "b02").copy(uplinkActivity = UplinkActivity.CONNECTING)
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns connecting
        every { hackerStateEntityService.updateConnectionId(connecting, "con-2") } returns connecting

        uplinkService.enterSiteFromUplink(USER_ID, "run-b", "con-2")

        verify { runService.sendEnteredRun(USER_ID, runB, connecting) }
        verify { runIngressService.connect(connecting, "b02", false, match { it.sourceSiteName == "a" && it.targetSiteName == "b" }) }
        verify(exactly = 0) { commandMoveService.moveArrive(any(), any(), any()) }
    }

    @Test
    fun `entering the run when returning waits for the site to render`() {
        val returning = state("run-a", "site-a", null, ingressNodeId = "a01").copy(uplinkActivity = UplinkActivity.RETURNING, uplinkReturnNodeId = "a03")
        val arrived = inA()
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
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns returningOutside

        uplinkService.enterSiteFromUplink(USER_ID, "run-a", "con-2")

        verify { runService.enterRunOutside(USER_ID, runA, "con-2") }
        verify(exactly = 0) { userTaskRunner.queueInTicksForSite(any(), any(), any(), any()) }
    }

    @Test
    fun `entering is aborted when the hacker left the run while the browser was waiting`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns state(null, null, null)

        uplinkService.enterSiteFromUplink(USER_ID, "run-b", "con-2")

        verify(exactly = 0) { runService.sendEnteredRun(any(), any(), any()) }
        verify(exactly = 0) { runIngressService.connect(any(), any(), any(), any()) }
        verify(exactly = 0) { connectionService.toRun(any(), any(), *anyVararg()) }
    }

    @Test
    fun `after the site has rendered, the session is restored at the uplink node`() {
        every { hackerStateEntityService.retrieveForUserId(USER_ID) } returns inA()

        uplinkService.sessionRestored(USER_ID, "run-a", "a03")

        verify { commandMoveService.moveArrive("a03", USER_ID, "run-a") }
    }

    @Test
    fun `leaving the target returns the hacker to the uplink node, keeping the node where they entered the source site`() {
        val hackerState = state("run-b", "site-b", "b00", listOf(returnToA), ingressNodeId = "b00")

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { networkSwitchService.isReachable("site-a", "a01", "a03", true) }
        verify { runService.leaveSite(hackerState, false, true) }
        verify { hackerStateEntityService.prepareUplinkReturn(hackerState, runA, returnToA, emptyList()) }
        verifyEnteringRunA()
    }

    private fun verifyEnteringRunA() {
        verify {
            connectionService.toUser(USER_ID, ServerActions.SERVER_ENTERING_RUN, "runId" to "run-a", "siteId" to "site-a", "viaUplink" to true, "uplinkReturn" to true)
        }
    }

    @Test
    fun `returns outside the source site when a network switch cut off the uplink node from where the hacker entered`() {
        every { networkSwitchService.isReachable("site-a", "a01", "a03", true) } returns false
        val hackerState = state("run-b", "site-b", "b00", listOf(returnToA), ingressNodeId = "b00")

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.prepareUplinkReturnOutside(hackerState, runA) }
        verify(exactly = 0) { hackerStateEntityService.prepareUplinkReturn(any(), any(), any(), any()) }
        verifyEnteringRunA()
    }

    @Test
    fun `returns outside the source site when the uplink node is gone`() {
        nodes.remove("a03")
        val hackerState = state("run-b", "site-b", "b00", listOf(returnToA), ingressNodeId = "b00")

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.prepareUplinkReturnOutside(hackerState, runA) }
        verify(exactly = 0) { hackerStateEntityService.prepareUplinkReturn(any(), any(), any(), any()) }
        verifyEnteringRunA()
    }

    @Test
    fun `returns outside the source site when it is shut down`() {
        val shutDownA = siteA.copy(shutdownEnd = ZonedDateTime.now())
        every { sitePropertiesEntityService.findBySiteId("site-a") } returns shutDownA
        val hackerState = state("run-b", "site-b", "b00", listOf(returnToA), ingressNodeId = "b00")

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.prepareUplinkReturnOutside(hackerState, runA) }
        verifyEnteringRunA()
    }

    @Test
    fun `in a chain, falls back past an internal site whose uplink node is gone`() {
        nodes.remove("b02")
        val returns = listOf(returnToA, UplinkReturn("run-b", "site-b", "b02", "b00"))
        val hackerState = state("run-c", "site-c", "c00", returns, ingressNodeId = "c00")

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.prepareUplinkReturn(hackerState, runA, returnToA, emptyList()) }
    }

    @Test
    fun `in a chain, returns to the uplink node in the previous internal site`() {
        val returnToB = UplinkReturn("run-b", "site-b", "b02", "b00")
        val hackerState = state("run-c", "site-c", "c00", listOf(returnToA, returnToB), ingressNodeId = "c00")

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.prepareUplinkReturn(hackerState, runB, returnToB, listOf(returnToA)) }
    }

    @Test
    fun `goes home when there is nothing to return to`() {
        every { runEntityService.findByRunId("run-a") } returns null
        val hackerState = state("run-b", "site-b", "b00", listOf(returnToA), ingressNodeId = "b00")

        uplinkService.returnFromUplink(hackerState, "Disconnected")

        verify { hackerStateEntityService.leaveSite(hackerState) }
        verify { connectionService.toUser(USER_ID, ServerActions.SERVER_RETURN_HOME) }
    }
}
