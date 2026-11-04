package org.n1.av2.layer.ice.sweeper

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.statistics.IceStatisticsService
import java.util.Optional

private const val ICE_ID = "sweeper-1"
private const val USER_ID = "user-1"

/*
 Board used in these tests (mine at 1,0):
 1*10
 1110
 0000
 */
private val CELLS = listOf("1*10", "1110", "0000")

@ExtendWith(MockKExtension::class)
class SweeperServiceTest {

    @MockK(relaxed = true)
    lateinit var connectionService: ConnectionService

    @MockK(relaxed = true)
    lateinit var hackedUtil: HackedUtil

    @MockK
    lateinit var sweeperIceStatusRepo: SweeperIceStatusRepo

    @MockK(relaxed = true)
    lateinit var currentUser: CurrentUserService

    @MockK
    lateinit var configService: ConfigService

    @MockK
    lateinit var runService: RunService

    @MockK(relaxed = true)
    lateinit var iceStatisticsService: IceStatisticsService

    @InjectMockKs
    lateinit var sweeperService: SweeperService

    private val saved = mutableListOf<SweeperIceStatus>()

    @BeforeEach
    fun setup() {
        every { currentUser.userId } returns USER_ID
        every { sweeperIceStatusRepo.save(capture(saved)) } answers { firstArg() }
    }

    private fun givenBoard(vararg modifiers: String) {
        val status = SweeperIceStatus(ICE_ID, "layer-1", IceStrength.VERY_WEAK, CELLS, modifiers.toMutableList(), false)
        every { sweeperIceStatusRepo.findById(ICE_ID) } returns Optional.of(status)
    }

    private fun chord(x: Int, y: Int) = sweeperService.interact(ICE_ID, x, y, SweeperModifyAction.CHORD)

    @Test
    fun `chord with matching flags reveals hidden neighbours and cascades`() {
        givenBoard("hfhh", "hrhh", "hhhh")

        chord(1, 1)

        assertThat(saved.last().modifiers).containsExactly("rfrr", "rrrr", "rrrr")
        verify { hackedUtil.iceHacked(ICE_ID, "layer-1", any(), any()) }
        verify(exactly = 0) { iceStatisticsService.sweeperLockout(any()) }
    }

    @Test
    fun `chord with a wrong flag explodes the mine and blocks the hacker`() {
        givenBoard("fhhh", "hrhh", "hhhh")

        chord(1, 1)

        val last = saved.last()
        assertThat(last.blockedUserIds).containsExactly(USER_ID)
        assertThat(last.modifiers).containsExactly("frhh", "hrhh", "hhhh")
        verify { iceStatisticsService.sweeperLockout(ICE_ID) }
        verify(exactly = 0) { hackedUtil.iceHacked(any(), any(), any(), any()) }
    }

    @Test
    fun `chord without enough flags does nothing`() {
        givenBoard("hhhh", "hrhh", "hhhh")

        chord(1, 1)

        assertThat(saved).isEmpty()
        verify(exactly = 0) { connectionService.toIce(any(), any(), *anyVararg()) }
    }

    @Test
    fun `chord on a hidden cell does nothing`() {
        givenBoard("hfhh", "hhhh", "hhhh")

        chord(1, 1)

        assertThat(saved).isEmpty()
    }

    @Test
    fun `exploded mine counts as a known mine for chording`() {
        givenBoard("hrhh", "hrhh", "hhhh")

        chord(1, 1)

        assertThat(saved.last().modifiers).containsExactly("rrrr", "rrrr", "rrrr")
        verify(exactly = 0) { iceStatisticsService.sweeperLockout(any()) }
    }

    @Test
    fun `reveal of an empty cell still cascades`() {
        givenBoard("hhhh", "hhhh", "hhhh")

        sweeperService.interact(ICE_ID, 3, 0, SweeperModifyAction.REVEAL)

        assertThat(saved.last().modifiers).containsExactly("hhrr", "rrrr", "rrrr")
    }
}
