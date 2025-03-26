package org.n1.av2.script.effect.helper

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateRepo
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.run.scanning.BlockedPathException
import org.n1.av2.run.scanning.TraverseNode
import org.n1.av2.run.scanning.TraverseNodeService
import org.n1.av2.run.terminal.inside.CommandMoveService
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalState
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

enum class JumpBlockedType {
    IGNORE_ICE,
    BLOCKED_BY_ICE,
}

@Service
class JumpEffectHelper(
    private val nodeEntityService: NodeEntityService,
    private val traverseNodeService: TraverseNodeService,
    private val connectionService: ConnectionService,
    private val commandMoveService: CommandMoveService,
    private val currentUserService: CurrentUserService,
    private val hackerStateRepo: HackerStateRepo,
) {

    fun jump(effect: ScriptEffect, siteId: String, currentNodeId: String, targetNodeId: String, hackerState: HackerState, targetDescription: String): ScriptExecution {
        val blockedType = JumpBlockedType.valueOf(effect.value!!)
        return when (blockedType) {
            JumpBlockedType.IGNORE_ICE -> jumpIgnoringIce(currentNodeId, targetNodeId, hackerState, targetDescription)
            JumpBlockedType.BLOCKED_BY_ICE -> jumpBlockedByIce(siteId, currentNodeId, targetNodeId, hackerState, targetDescription)
        }
    }

    private fun jumpIgnoringIce(currentNodeId: String, targetNodeId: String, hackerState: HackerState, targetDescription: String): ScriptExecution {
        if (targetNodeId == currentNodeId) return ScriptExecution("Cannot jump to the current node.")

        val siteId = hackerState.siteId!!
        val nodes = nodeEntityService.findBySiteId(siteId)

        val traverseNodesById = traverseNodeService.createTraverseNodesWithDistance(siteId, currentNodeId, nodes)
        val startTraverseNode = traverseNodesById[currentNodeId] ?: error("currentNodeId node not found in traverse nodes.")
        val targetTraverseNode = traverseNodesById[targetNodeId] ?: error("targetNodeId not found in traverse nodes.")

        val path = TraverseNode.createPath(startTraverseNode, targetTraverseNode, true)

        return ScriptExecution(TerminalState.KEEP_LOCKED) {
            connectionService.replyTerminalReceive("Jumping to ${targetDescription}.")
            setPreviousNode(hackerState, path)
            commandMoveService.moveArrive(targetNodeId, currentUserService.userId, hackerState.runId!!)
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

    private fun jumpBlockedByIce(siteId: String, currentNodeId: String, targetNodeId: String, hackerState: HackerState, targetDescription: String): ScriptExecution {
        if (targetNodeId == currentNodeId) return ScriptExecution("Cannot jump to the current node.")

        val nodes: List<Node> = nodeEntityService.findBySiteId(siteId)


        val currentNode = nodeEntityService.findById(currentNodeId)
        if (currentNode.unhackedIce) return ScriptExecution("Cannot jump from a node with unhacked ICE.")

        val (start, traverseNodesById) = traverseNodeService.createTraverseNodesWithDistance(siteId, currentNodeId, nodes, targetNodeId)
        val targetTraverseNode = traverseNodesById[targetNodeId] ?: error("Target node not found in traverse nodes.")

        val path = try {
            TraverseNode.createPath(start, targetTraverseNode)
        }
        catch (_: BlockedPathException) {
            return ScriptExecution("Cannot jump to that node, ICE blocks the path.")
        }
        return ScriptExecution(TerminalState.KEEP_LOCKED) {
            connectionService.replyTerminalReceiveAndLocked(true, "Jumping to ${targetDescription}.")

            setPreviousNodeToPreviousInPath(path, hackerState)
            commandMoveService.moveArrive(targetNodeId, currentUserService.userId, hackerState.runId!!)
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

    companion object {
        fun validateJumpBlockedType(effect: ScriptEffect): String? {
            JumpBlockedType.entries.find { it.name == effect.value } ?: return "Invalid jump type."
            return null
        }
    }
}
