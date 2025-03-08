package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateRepo
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.run.scanning.BlockedPathException
import org.n1.av2.run.scanning.TraverseNode
import org.n1.av2.run.scanning.TraverseNodeService
import org.n1.av2.run.terminal.inside.CommandMoveService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.effect.helper.NodeAccessHelper
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service
/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.JUMP_TO_NODE
 */
@Service
class JumpToNodeEffectService(
    private val nodeEntityService: NodeEntityService,
    private val connectionService: ConnectionService,
    private val traverseNodeService: TraverseNodeService,
    private val scriptEffectHelper: ScriptEffectHelper,
    private val nodeAccessHelper: NodeAccessHelper,
    private val commandMoveService: CommandMoveService,
    private val currentUserService: CurrentUserService,
    private val hackerStateRepo: HackerStateRepo,
) : ScriptEffectInterface {

    override val name = "Jump to node"
    override val defaultValue = null
    override val gmDescription = "Jump to another node, blocked by ICE along the way."

    override fun playerDescription(effect: ScriptEffect) = gmDescription

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        scriptEffectHelper.checkInNode(hackerState)?.let { return ScriptExecution(it) }
        val targetNetworkId = argumentTokens.firstOrNull() ?: return ScriptExecution("Provide the [ok]network id[/] of the node to jump to.")
        nodeAccessHelper.checkNodeRevealed(targetNetworkId, hackerState.runId!!)?.let { return ScriptExecution(it) }

        val siteId: String = hackerState.siteId!!
        val nodes: List<Node> = nodeEntityService.findBySiteId(siteId)
        val targetNodeId = nodes.find { it.networkId == targetNetworkId }?.id ?: error("Node not found for network ID: $targetNetworkId")
        val currentNodeId = hackerState.currentNodeId ?: error("Current node ID not found.")

        if (targetNodeId == currentNodeId) return ScriptExecution("Cannot jump to the current node.")

        val currentNode = nodes.find { it.id == currentNodeId } ?: error("Current node not found in nodes.")
        if (currentNode.unhackedIce) return ScriptExecution("Cannot jump from a node with unhacked ICE.")

        val (start, traverseNodesById) = traverseNodeService.createTraverseNodesWithDistance(siteId, currentNodeId, nodes, targetNodeId)
        val targetTraverseNode = traverseNodesById[targetNodeId] ?: error("Target node not found in traverse nodes.")

        val path = try {
            TraverseNode.createPath(start, targetTraverseNode)
        }
        catch (_: BlockedPathException) {
            return ScriptExecution("Cannot jump to that node, ICE blocks the path.")
        }
        return ScriptExecution {
            connectionService.replyTerminalReceive("Jumping to node...")

            setPreviousNodeToPreviousInPath(path, hackerState)
            commandMoveService.moveArrive(targetNodeId, currentUserService.userId, hackerState.runId)
            TerminalLockState.LOCK
        }
    }

    private fun setPreviousNodeToPreviousInPath(path: List<String>, hackerState: HackerState) {
        val lastIndex = path.lastIndex
        val previousNodeId: String = path[lastIndex - 1]
        val newPosition = hackerState.copy(
            currentNodeId = previousNodeId,
        )
        hackerStateRepo.save(newPosition)
    }

}
