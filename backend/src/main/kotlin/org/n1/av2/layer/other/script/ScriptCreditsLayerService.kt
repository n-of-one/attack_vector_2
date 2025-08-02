package org.n1.av2.layer.other.script

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class ScriptCreditsLayerService(
    private val connectionService: ConnectionService,
    private val nodeEntityService: NodeEntityService,
    private val hackerEntityService: HackerEntityService,
    private val skillService: SkillService,
    private val userAndHackerService: UserAndHackerService,
) {

    fun hack(layer: ScriptCreditsLayer, hackerState: HackerStateRunning) {
        if (layer.stolen) {
            connectionService.replyTerminalReceive("No data of value found.")
            connectionService.replyTerminalReceive("Logs indicate data has recently been deleted.")
            return
        }

        if (layer.amount == 0) {
            connectionService.replyTerminalReceive("No data of value found.")
            return
        }

        if (!skillService.currentUserHasSkill(SkillType.SCRIPT_CREDITS)) {
            connectionService.replyTerminalReceive("Valuable data found.")
            connectionService.replyTerminalReceive("Unknown encryption detected.[/] [mute](Missing skill)")
            return
        }

        connectionService.replyTerminalReceive("Valuable data found, worth ${layer.amount}⚡. Sending to data broker.")
        connectionService.replyTerminalReceive("Clearing data from layer...")
        connectionService.replyTerminalReceive("Done.")

        hackerEntityService.addScriptCredits(hackerState.userId, layer.amount)
        userAndHackerService.sendDetailsOfCurrentUser()

        val node = nodeEntityService.findByLayerId(layer.id)
        val layerToEdit = node.getLayerById(layer.id) as ScriptCreditsLayer
        layerToEdit.stolen = true
        nodeEntityService.save(node)
    }
}
