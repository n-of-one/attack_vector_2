package org.n1.av2.layer.other.daemon

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.layer.model.TriggerRecurrence
import org.n1.av2.layer.model.TriggerRecurrence.EACH_HACKER_ONCE
import org.n1.av2.layer.model.TriggerRecurrence.EVERY_ENTRY
import org.n1.av2.layer.model.TriggerRecurrence.FIRST_ENTRY_ONLY
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import java.util.Optional

private const val SITE_ID = "site-1"
private const val USER_ID = "user-1"
private const val OTHER_USER_ID = "user-2"

@ExtendWith(MockKExtension::class)
class DaemonServiceTest {

    @MockK
    lateinit var connectionService: ConnectionService

    @MockK
    lateinit var daemonStatusRepository: DaemonStatusRepository

    @MockK
    lateinit var currentUserService: CurrentUserService

    @MockK
    lateinit var skillService: SkillService

    @InjectMockKs
    lateinit var daemonService: DaemonService

    @BeforeEach
    fun setup() {
        every { currentUserService.userId } returns USER_ID
        every { connectionService.replyTerminalReceive(any<List<String>>()) } just runs
        every { daemonStatusRepository.save(any()) } answers { firstArg() }
        every { daemonStatusRepository.findById(any()) } returns Optional.empty()
    }

    private fun daemon(recurring: TriggerRecurrence, requiredTrait: String? = null) =
        DaemonLayer("node-1:layer-1", 1, "Daemon", "", "line 1\nline 2", requiredTrait, recurring)

    private fun status(vararg userIds: String) = DaemonStatusEntity("status-1", SITE_ID, "node-1:layer-1", userIds.toList())

    @Test
    fun `shows text on first entry and records the hacker`() {
        every { daemonStatusRepository.findByLayerId(any()) } returns null
        val saved = slot<DaemonStatusEntity>()
        every { daemonStatusRepository.save(capture(saved)) } answers { firstArg() }

        daemonService.hackerArrivesNode(SITE_ID, daemon(EACH_HACKER_ONCE))

        verify { connectionService.replyTerminalReceive(listOf("line 1", "line 2")) }
        assertThat(saved.captured.triggeredUserIds).containsExactly(USER_ID)
        assertThat(saved.captured.siteId).isEqualTo(SITE_ID)
    }

    @Test
    fun `first entry only does not trigger once any hacker has seen it`() {
        every { daemonStatusRepository.findByLayerId(any()) } returns status(OTHER_USER_ID)

        daemonService.hackerArrivesNode(SITE_ID, daemon(FIRST_ENTRY_ONLY))

        verify(exactly = 0) { connectionService.replyTerminalReceive(any<List<String>>()) }
    }

    @Test
    fun `each hacker once triggers for another hacker but not twice for the same hacker`() {
        every { daemonStatusRepository.findByLayerId(any()) } returns status(OTHER_USER_ID)
        daemonService.hackerArrivesNode(SITE_ID, daemon(EACH_HACKER_ONCE))
        verify(exactly = 1) { connectionService.replyTerminalReceive(any<List<String>>()) }

        every { daemonStatusRepository.findByLayerId(any()) } returns status(OTHER_USER_ID, USER_ID)
        daemonService.hackerArrivesNode(SITE_ID, daemon(EACH_HACKER_ONCE))
        verify(exactly = 1) { connectionService.replyTerminalReceive(any<List<String>>()) }
    }

    @Test
    fun `every entry triggers on repeated entries without saving again`() {
        every { daemonStatusRepository.findByLayerId(any()) } returns status(USER_ID)

        daemonService.hackerArrivesNode(SITE_ID, daemon(EVERY_ENTRY))

        verify(exactly = 1) { connectionService.replyTerminalReceive(any<List<String>>()) }
        verify(exactly = 0) { daemonStatusRepository.save(any()) }
    }

    @Test
    fun `required trait missing does not trigger and does not record the hacker`() {
        every { skillService.userHasTrait(USER_ID, "insider") } returns false

        daemonService.hackerArrivesNode(SITE_ID, daemon(FIRST_ENTRY_ONLY, "insider"))

        verify(exactly = 0) { connectionService.replyTerminalReceive(any<List<String>>()) }
        verify(exactly = 0) { daemonStatusRepository.save(any()) }
    }

    @Test
    fun `required trait present triggers`() {
        every { skillService.userHasTrait(USER_ID, "insider") } returns true
        every { daemonStatusRepository.findByLayerId(any()) } returns null

        daemonService.hackerArrivesNode(SITE_ID, daemon(EVERY_ENTRY, "insider"))

        verify(exactly = 1) { connectionService.replyTerminalReceive(any<List<String>>()) }
    }

    @Test
    fun `hack reveals nothing`() {
        every { connectionService.replyTerminalReceive(*anyVararg<String>()) } just runs

        daemonService.hack()

        verify { connectionService.replyTerminalReceive("Hacking this layer reveals nothing new.") }
    }

    @Test
    fun `site reset removes statuses`() {
        every { daemonStatusRepository.deleteBySiteId(SITE_ID) } just runs

        daemonService.siteReset(SITE_ID)

        verify { daemonStatusRepository.deleteBySiteId(SITE_ID) }
    }
}
