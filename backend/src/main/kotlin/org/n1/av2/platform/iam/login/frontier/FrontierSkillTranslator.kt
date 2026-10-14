package org.n1.av2.platform.iam.login.frontier

import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.hacker.skill.SkillType.*
import org.n1.av2.site.entity.enums.LayerType.NETWALK_ICE
import org.n1.av2.site.entity.enums.LayerType.SWEEPER_ICE
import org.n1.av2.site.entity.enums.LayerType.TANGLE_ICE
import org.n1.av2.site.entity.enums.LayerType.WORD_SEARCH_ICE

class FrontierSkillTranslator(
    val skills: FrontierV3HackingSkills,
) {

    private val WEAKEN_SIMPLE = "${WORD_SEARCH_ICE.name.substringBefore("_ICE").lowercase()}, ${TANGLE_ICE.name.substringBefore("_ICE").lowercase()}"
    private val WEAKEN_COMPLEX = "${NETWALK_ICE.name.substringBefore("_ICE").lowercase()}, ${SWEEPER_ICE.name.substringBefore("_ICE").lowercase()}"

    fun translateSkills(): Map<SkillType, String> = buildMap {
        fun addIfPresent( block: () -> Pair<SkillType, String>?) {
            block()?.let { put(it.first, it.second) }
        }

        addIfPresent { hackerLevel(1, SEARCH_SITE) }
        addIfPresent { hackerLevel(2, SCAN) }
        addIfPresent { hackerLevel(2, CREATE_SITE) }
        addIfPresent { weaken() }


        if (slicerSpecialisation()) {
            addIfPresent { eliteLevel(6, STEALTH, "25") }
            addIfPresent { eliteLevel(7, ADJUSTED_SPEED, "10") }
            addIfPresent { eliteLevel(8, UNDO_TRIPWIRE) }
            addIfPresent { eliteLevel(9, BYPASS) }
            addIfPresent { eliteLevel(10, JUMP_TO_HACKER) }
        }

        if (ghostSpecialisation()) {
            addIfPresent { architectLevel(7, ADJUSTED_SPEED, "10") }

            // Stealth is primarily for elites.
            // However, if you are an architect that also has elite levels then you get stealth as well
            addIfPresent { eliteLevel(6, STEALTH, "25") }
        }

        addIfPresent { scriptRam() }
        addIfPresent { scriptIncome() }
    }

    private fun hackerLevel(level: Int, skillType: SkillType, value: String = "") =
        if (skills.hacker >= level) skillType to value else null

    private fun eliteLevel(level: Int, skillType: SkillType, value: String = "") =
        if (skills.elite >= level) skillType to value else null

    private fun architectLevel(level: Int, skillType: SkillType, value: String = "") =
        if (skills.architect >= level) skillType to value else null

    private fun weaken(): Pair<SkillType, String>? {
        if (skills.hacker < 4) return null
        if (skills.hacker == 4) return WEAKEN to WEAKEN_SIMPLE
        return WEAKEN to "$WEAKEN_SIMPLE, $WEAKEN_COMPLEX"
    }

    fun slicerSpecialisation(): Boolean {
        if (skills.elite < 6) { return false }
        if (skills.elite > skills.architect) { return true }
        return false
    }

    fun ghostSpecialisation(): Boolean {
        if (skills.architect < 6) { return false }
        if (skills.architect > skills.elite) { return true }
        return skills.architect == skills.elite
    }

    private fun scriptRam(): Pair<SkillType, String>? {
        if (skills.hacker < 3) return null

        val ramFromScripts = 3
        val ramFromDataJack = if (skills.elite >= 7 || skills.architect >= 7) 3 else 0
        val ramFromSlicer1 = if (skills.architect >= 8) 3 else 0
        val ramFromSlicer2 = if (skills.architect >= 9) 3 else 0

        val totalRam = ramFromScripts + ramFromDataJack + ramFromSlicer1 + ramFromSlicer2
        return SCRIPT_RAM to totalRam.toString()
    }

    fun scriptIncome(): Pair<SkillType, String>? {
        if (skills.architect < 6 ) return null

        // Architect->Ghosts get botnet at level 6, and dual elite/architect also get it.
        val incomeFromBotNet = 100

        val incomeFromSlicer1 = if (ghostSpecialisation()) 100 else 0

        return SCRIPT_CREDITS to (incomeFromBotNet + incomeFromSlicer1).toString()
    }

}
