package org.n1.av2.site.entity

import org.n1.av2.editor.AddConnection
import org.n1.av2.platform.util.createId
import org.springframework.stereotype.Service

@Service
class ConnectionEntityService(
    private val connectionRepo: ConnectionRepo
) {

    private val logger = mu.KotlinLogging.logger {}

    fun findConnection(startId: String, endId: String): Connection? {
        val startConnections = findByNodeId(startId)
        return startConnections.find { it.fromId == endId || it.toId == endId }
    }

    fun createConnection(command: AddConnection): Connection {
        val connection = Connection(
            id = createId("con", connectionRepo::findById),
            siteId = command.siteId,
            fromId = command.fromId,
            toId = command.toId
        )
        connectionRepo.save(connection)

        return connection
    }

    fun getAll(siteId: String): List<Connection> {
        return connectionRepo.findBySiteId(siteId)
    }

    fun findByNodeId(nodeId: String): Set<Connection> {
        return connectionRepo.findAllByFromIdOrToId(nodeId, nodeId).toSet()
    }

    fun deleteAll(connections: Collection<Connection>) {
        connectionRepo.deleteAll(connections)
    }

    fun deleteAllForSite(siteId: String) {
        connectionRepo.findBySiteId(siteId).forEach { connectionRepo.delete(it) }
    }


}
