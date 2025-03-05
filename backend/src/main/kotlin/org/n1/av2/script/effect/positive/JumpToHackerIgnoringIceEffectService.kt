package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateRepo
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.scanning.TraverseNode
import org.n1.av2.run.scanning.TraverseNodeService
import org.n1.av2.run.terminal.SocialTerminalService
import org.n1.av2.run.terminal.inside.CommandMoveService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class JumpToHackerIgnoringIceEffectService(
    private val connectionService: ConnectionService,
    private val scriptEffectHelper: ScriptEffectHelper,
    private val commandMoveService: CommandMoveService,
    private val currentUserService: CurrentUserService,
    private val hackerStateRepo: HackerStateRepo,
    private val userEntityService: UserEntityService,
    private val traverseNodeService: TraverseNodeService,
    private val nodeEntityService: NodeEntityService,
    ) : ScriptEffectInterface {

    override val name = "Jump to hacker, ignoring ICE"
    override val defaultValue = null
    override val gmDescription = "Jump to another hacker, ignore ICE along the way."

    override fun playerDescription(effect: ScriptEffect) = gmDescription

    override fun validate(effect: ScriptEffect): String? {
        return null
    }

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        scriptEffectHelper.checkInNode(hackerState)?.let { return ScriptExecution(it) }
        val userName = argumentTokens.firstOrNull() ?: return ScriptExecution("Provide the [info]<username>[/] of the hacker to phase to.")

        val user = userEntityService.findByNameIgnoreCase(userName)
        if (user == null) {
            return ScriptExecution(SocialTerminalService.hackerNotFound(userName))
        }

        val targetHackerRunState: HackerState = hackerStateRepo.findById(user.id).orElse(null) ?: return ScriptExecution("${userName} is not in this run.")
        if (targetHackerRunState.runId != hackerState.runId) return ScriptExecution("${userName} is not in this run.")
        if (targetHackerRunState.activity != HackerActivity.INSIDE) return ScriptExecution("${userName} is not in the site.")

        val currentNodeId = hackerState.currentNodeId ?: error("Current node ID not found.")
        val targetNodeId = targetHackerRunState.currentNodeId ?: error("Target node ID not found.")
        if (targetNodeId == currentNodeId) return ScriptExecution("Cannot jump to the current node.")

        val siteId = hackerState.siteId!!
        val nodes = nodeEntityService.findBySiteId(siteId)

        val traverseNodesById = traverseNodeService.createTraverseNodesWithDistance(siteId, currentNodeId, nodes)
        val startTraverseNode = traverseNodesById[currentNodeId] ?: error("currentNodeId node not found in traverse nodes.")
        val targetTraverseNode = traverseNodesById[targetNodeId] ?: error("targetNodeId not found in traverse nodes.")

        val path = TraverseNode.createPath(startTraverseNode, targetTraverseNode, true)

        return ScriptExecution {
            connectionService.replyTerminalReceive("Jumping to ${userName}.")

            setPreviousNode(hackerState, path)
            commandMoveService.moveArrive(targetNodeId, currentUserService.userId, hackerState.runId!!)
            TerminalLockState.LOCK
        }
    }

    private fun setPreviousNode(hackerState: HackerState, path: List<String>) {
        val newPosition = hackerState.copy(
            currentNodeId = determinePreviousNode(path),
        )
        hackerStateRepo.save(newPosition)
    }

    private fun determinePreviousNode(path: List<String>): String {
        if (path.size <= 2) {
            return path.first() // you were adjacent to target, take your starting positions
        }
        return path[path.size - 2] // take the previous node in the path
    }

}
