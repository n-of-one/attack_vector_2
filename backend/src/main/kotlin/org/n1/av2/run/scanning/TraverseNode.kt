package org.n1.av2.run.scanning

import jakarta.validation.constraints.NotEmpty
import java.util.*

class BlockedPathException(message: String) : RuntimeException(message)

data class TraverseNode(
    val nodeId: String,
    val networkId: String,
    val connections: MutableSet<TraverseNode> = HashSet(),
    var distance: Int? = null,
    var visited: Boolean = false,
    var unhackedIce: Boolean,
) {
    // for unit test
    constructor(name: String) : this(name, name, HashSet(), null, false, false)

    override fun equals(other: Any?): Boolean {
        return if (other is TraverseNode) {
            other.nodeId == this.nodeId
        } else false
    }

    override fun hashCode(): Int {
        return this.nodeId.hashCode()
    }

    override fun toString(): String {
        return "${networkId} (${nodeId}) Dist: ${distance} Connect count: ${connections.size}"
    }

    fun fillDistanceFromHere(distance: Int) {
        this.distance = distance
        val nextDistance = distance + 1
        this.connections
            .filter { it.distance == null || it.distance!! > nextDistance }
            .forEach { it.fillDistanceFromHere(nextDistance) }
    }

    companion object {

        fun removeIceBlockedNodes(target: TraverseNode, traverseNodes: Collection<TraverseNode>) {
            traverseNodes.forEach { node ->
                if (node == target) return@forEach
                if (node.unhackedIce) {
                    node.connections.forEach { iceBlockedNode -> iceBlockedNode.connections.remove(node) }
                    node.connections.clear()
                }
            }
        }

        @NotEmpty
        fun createPath(startNode: TraverseNode, targetNode: TraverseNode): List<String> {
            val path = LinkedList(listOf(targetNode.nodeId))
            var currentNode = targetNode
            while (currentNode != startNode) {
                if (currentNode.distance == null) throw BlockedPathException("ICE blocks path to ${currentNode.networkId}")
                val previousNode = currentNode
                    .connections
                    .find { it.distance == (currentNode.distance!! - 1) && !it.unhackedIce }
                    ?: throw BlockedPathException("ICE blocks path to ${currentNode.networkId}")
                currentNode = previousNode
                path.add(0, currentNode.nodeId)
            }

            return path
        }
    }

    fun unblockedNetwork(network: List<TraverseNode>): List<TraverseNode> {
        this.visited = true

        if (this.unhackedIce) {
            return network + this
        }

        val n = LinkedList<TraverseNode>()

        this.connections.forEach {
            if (it.visited) {
                return@forEach
            }
            n.addAll(it.unblockedNetwork(network))
        }

        return network + n + this
    }
}
