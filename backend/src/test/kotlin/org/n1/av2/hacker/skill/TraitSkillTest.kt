package org.n1.av2.hacker.skill

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.platform.iam.user.UserEntityService
import org.springframework.context.ApplicationContext
import java.util.Optional

@ExtendWith(MockKExtension::class)
class TraitSkillTest {

    @MockK
    lateinit var currentUserService: CurrentUserService

    @MockK
    lateinit var skillRepo: SkillRepo

    @MockK
    lateinit var applicationContext: ApplicationContext

    @MockK
    lateinit var userEntityService: UserEntityService

    @MockK
    lateinit var userAndHackerService: UserAndHackerService

    @InjectMockKs
    lateinit var skillService: SkillService

    @Test
    fun `validate trait value`() {
        assertThat(validateTrait("insider: Former corporate insider")).isNull()
        assertThat(validateTrait("ex_corp-2:text: with colon")).isNull()
        assertThat(validateTrait("corp.mega.insider: dotted key")).isNull()
        assertThat(validateTrait("no separator")).isNotNull()
        assertThat(validateTrait(": no key")).isNotNull()
        assertThat(validateTrait("two words: text")).isNotNull()
        assertThat(validateTrait("insider:   ")).isNotNull()
    }

    @Test
    fun `normalize trait value`() {
        assertThat(traitToFunctional("  insider :Former insider ")).isEqualTo("insider: Former insider")
        assertThat(traitKeyOf("insider: text: more")).isEqualTo("insider")
    }

    @Test
    fun `user has trait matches case insensitive on key only`() {
        every { skillRepo.findByUserId("u1") } returns listOf(
            Skill("s1", "u1", SkillType.TRAIT, "insider: Former insider"),
            Skill("s2", "u1", SkillType.WEAKEN, "ghost"),
        )

        assertThat(skillService.userHasTrait("u1", "INSIDER")).isTrue()
        assertThat(skillService.userHasTrait("u1", "Former")).isFalse()
        assertThat(skillService.userHasTrait("u1", "ghost")).isFalse() // WEAKEN is not a trait
    }

    @Test
    fun `adding a new skill always creates a new instance`() {
        skillService.userAndHackerService = userAndHackerService
        every { skillRepo.findById(any()) } returns Optional.empty()
        every { skillRepo.save(any()) } answers { firstArg() }
        every { userAndHackerService.sendDetailsOfSpecificUser("u1") } just runs

        skillService.addNewSkillAndUpdateUi("u1", SkillType.TRAIT)

        verify(exactly = 0) { skillRepo.findFirstByTypeAndUserId(any(), any()) }
        verify { skillRepo.save(match { it.type == SkillType.TRAIT && it.userId == "u1" && it.value == SkillType.TRAIT.defaultValue }) }
    }
}
