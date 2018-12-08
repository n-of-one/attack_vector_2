package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.ui.AddNode
import org.n1.mainframe.backend.model.ui.MoveNode
import org.n1.mainframe.backend.repo.NodeRepo
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service

@Service
class NodeService(
        val nodeRepo: NodeRepo
) {

    fun createNode(command: AddNode): Node {
        val id = createId("node", nodeRepo::findOne)
        val node = Node(id, command.type, command.x, command.y, command.type.ice )
        nodeRepo.save(node)
        return node
    }

    fun getAll(nodeIds: MutableList<String>): List<Node> {
        return nodeRepo.findByIdIn(nodeIds)
    }

    fun getById(nodeId: String) : Node {
        return nodeRepo.findOne(nodeId) ?: throw IllegalStateException("Node not found with id: ${nodeId}")
    }

    fun moveNode(command: MoveNode) {
        val node = getById(command.nodeId)
        val movedNode = node.copy(x = command.x, y = command.y)
        nodeRepo.save(movedNode)
    }

    fun purgeAll() {
        nodeRepo.deleteAll()
    }

    fun deleteNode(nodeId: String) {
        val node = getById(nodeId)
        nodeRepo.delete(node)
    }

    fun snap(nodeIds: List<String>) {
        nodeIds.forEach { nodeId ->
            val node = getById(nodeId)
            node.x = 40 * ((node.x + 20) / 40)
            node.y = 40 * ((node.y + 20) / 40)
            nodeRepo.save(node)
        }
    }


}