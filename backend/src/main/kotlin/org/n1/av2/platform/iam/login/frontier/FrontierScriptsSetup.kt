package org.n1.av2.platform.iam.login.frontier

enum class FrontierScript(
    val scriptName: String,
    val price: Int)
{
    MASK("mask", 10),
    MASK_PLUS_PLUS("mask++", 18),
    SITESTAT("sitestat", 20),
    JMP("jmp", 5),
    PREDICT("predict", 8),
    UNCLUSTER("uncluster", 10),
    RESPAWN("respawn", 12),
    PHASE("phase", 40),
    DEEPSCAN("deepscan", 15),
//    PACKETSTORM("packetstorm", 10),
    ROTATE("rotate", 25),
    WORM("worm", 10),
    CRASH_GAA("crash_gaa", 40),
    CRASH_JAA("crash_jaa", 50),
    CRASH_SAN("crash_san", 60),
    CRASH_VIS("crash_vis", 70),
}

class FrontierScriptsAccessTranslator(
    val frontierInfo: FrontierUserAndCharacterInfo,
    val skills: FrontierV3HackingSkills,
) {

    fun determineAccessInfo(): Map<String, Int> = buildMap {
        fun addIfPresent( block: () -> FrontierScript?) {
            block()?.let { put(it.scriptName, it.price) }
        }

        addIfPresent { hackerLevel(3, FrontierScript.MASK) }
        addIfPresent { hackerLevel(3, FrontierScript.MASK_PLUS_PLUS) }
        addIfPresent { hackerLevel(3, FrontierScript.SITESTAT) }
        addIfPresent { hackerLevel(3, FrontierScript.JMP) }
        addIfPresent { hackerLevel(3, FrontierScript.PREDICT) }
        addIfPresent { hackerLevel(3, FrontierScript.UNCLUSTER) }
        addIfPresent { hackerLevel(3, FrontierScript.RESPAWN) }
        addIfPresent { hackerLevel(3, FrontierScript.PHASE) }

        addIfPresent { architectLevel(8, FrontierScript.DEEPSCAN) }
//        addIfPresent { architectLevel(6, FrontierScript.PACKEDSTORM) }

        addIfPresent { architectLevel(9, FrontierScript.ROTATE) }
        addIfPresent { architectLevel(9, FrontierScript.WORM) }

        addIfPresent { architectLevel(10, FrontierScript.CRASH_GAA) }
        addIfPresent { architectLevel(10, FrontierScript.CRASH_JAA) }
        addIfPresent { architectLevel(10, FrontierScript.CRASH_SAN) }
        addIfPresent { architectLevel(10, FrontierScript.CRASH_VIS) }
    }

    private fun hackerLevel(level: Int, script: FrontierScript) =
        if (skills.hacker >= level) script else null

    private fun architectLevel(level: Int, skillType: FrontierScript) =
        if (skills.architect >= level) skillType else null


}
