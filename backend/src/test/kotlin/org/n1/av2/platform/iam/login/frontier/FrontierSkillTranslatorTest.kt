package org.n1.av2.platform.iam.login.frontier

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.n1.av2.hacker.skill.SkillType.SCRIPT_RAM
import org.n1.av2.hacker.skill.SkillType.TRAIT

class FrontierSkillTranslatorTest {

    private val displayTrait = TRAIT to "word-search-display: Word search display options"

    @Test
    fun `every hacker gets the word search display trait, even without hacking skills`() {
        val skills = FrontierSkillTranslator(FrontierV3HackingSkills()).translateSkills()

        assertThat(skills).containsExactly(displayTrait)
    }

    @Test
    fun `word search display trait is added next to the datajack trait`() {
        val skills = FrontierSkillTranslator(FrontierV3HackingSkills(hacker = 5, elite = 7)).translateSkills()

        assertThat(skills).contains(TRAIT to "datajack: Datajack implant", displayTrait)
        assertThat(skills.filter { it.first == SCRIPT_RAM }).containsExactly(SCRIPT_RAM to "6")
    }
}
