package org.n1.av2.layer.ice.wordsearch

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.enums.IceStrength
import java.util.Optional

private const val ICE_ID = "wordSearch-1"
private const val USER_ID = "user-1"

@ExtendWith(MockKExtension::class)
class WordSearchServiceTest {

    @MockK
    lateinit var wordSearchIceStatusRepo: WordSearchIceStatusRepo

    @MockK
    lateinit var connectionService: ConnectionService

    @MockK
    lateinit var hackedUtil: HackedUtil

    @MockK
    lateinit var runService: RunService

    @MockK
    lateinit var configService: ConfigService

    @MockK
    lateinit var skillService: SkillService

    @MockK
    lateinit var currentUserService: CurrentUserService

    @InjectMockKs
    lateinit var wordSearchService: WordSearchService

    private var replied: Any? = null

    @BeforeEach
    fun setup() {
        val status = WordSearchIceStatus(ICE_ID, "node-1:layer-1", IceStrength.WEAK, listOf("AB"), listOf(listOf('A', 'B')), emptyList(),
            solutions = listOf(listOf("0:0", "1:0")))
        every { wordSearchIceStatusRepo.findById(ICE_ID) } returns Optional.of(status)
        every { configService.getAsBoolean(ConfigItem.DEV_QUICK_PLAYING) } returns false
        every { currentUserService.userId } returns USER_ID
        every { connectionService.reply(ServerActions.SERVER_WORD_SEARCH_ENTER, any()) } answers { replied = (args[1] as Array<*>).single() }
        every { runService.enterIce(ICE_ID) } just runs
    }

    private fun enteredPuzzle(): WordSearchService.EnterWordSearch {
        wordSearchService.enter(ICE_ID)
        return replied as WordSearchService.EnterWordSearch
    }

    @Test
    fun `display options shown for hacker with the trait`() {
        every { skillService.userHasTrait(USER_ID, WORD_SEARCH_DISPLAY_TRAIT) } returns true

        assertThat(enteredPuzzle().showDisplayOptions).isTrue()
    }

    @Test
    fun `display options hidden for hacker without the trait`() {
        every { skillService.userHasTrait(USER_ID, WORD_SEARCH_DISPLAY_TRAIT) } returns false

        assertThat(enteredPuzzle().showDisplayOptions).isFalse()
    }
}
