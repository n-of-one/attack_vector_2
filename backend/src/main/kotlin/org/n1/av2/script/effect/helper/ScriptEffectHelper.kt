package org.n1.av2.script.effect.helper

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

@Service
class ScriptEffectHelper(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
) {

    fun checkHackerAtNonShutdownSite(hackerState: HackerState): String? {
        if (hackerState.activity == HackerActivity.OFFLINE || hackerState.siteId == null) {
            return "You must run this script when hacking a site."
        }

        val siteState = sitePropertiesEntityService.getBySiteId(hackerState.siteId)
        if (siteState.shutdownEnd != null) {
            return "Cannot run this script while the site is shut down."
        }
        return null
    }
}
