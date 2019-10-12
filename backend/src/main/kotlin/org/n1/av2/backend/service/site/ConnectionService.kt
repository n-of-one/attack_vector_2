package org.n1.av2.backend.service.site

import mu.KLogging
import org.n1.av2.backend.model.db.site.Connection
import org.n1.av2.backend.model.ui.AddConnection
import org.n1.av2.backend.repo.ConnectionRepo
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service

@Service
class ConnectionService(
        val connectionRepo: ConnectionRepo) {

    companion object: KLogging()

    fun findConnection(startId: String, endId: String): Connection? {
//        return logNanoTime("findConnection", logger) {
            val startConnections = findByNodeId(startId)
            return startConnections.find { it.fromId == endId || it.toId == endId }
//        }
    }

    fun createConnection(command: AddConnection): Connection {
        val connection = Connection(
                id = createId("con", connectionRepo::findById),
                siteId = command.siteId,
                fromId = command.fromId,
                toId = command.toId)
        connectionRepo.save(connection)

        return connection
    }

    fun getAll(siteId: String): List<Connection> {
        return connectionRepo.findBySiteId(siteId)
    }

    fun purgeAll() {
        connectionRepo.deleteAll()
    }

    fun findByNodeId(nodeId: String): Set<Connection> {
        return connectionRepo.findAllByFromIdOrToId(nodeId, nodeId).toSet()
    }

    fun deleteAll(connections: Collection<Connection>) {
        connectionRepo.deleteAll(connections)
    }


}