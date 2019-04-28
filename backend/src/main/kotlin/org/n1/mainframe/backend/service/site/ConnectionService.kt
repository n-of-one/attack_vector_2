package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.Connection
import org.n1.mainframe.backend.model.ui.site.command.AddConnection
import org.n1.mainframe.backend.repo.ConnectionRepo
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service

@Service
class ConnectionService(
        val connectionRepo: ConnectionRepo) {

    fun findConnection(startId: String, endId: String): Connection? {
        val startConnections = findByNodeId(startId);
        return startConnections.find { it.fromId == endId || it.toId == endId }
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
        val froms = connectionRepo.findAllByFromId(nodeId)
        val tos = connectionRepo.findAllByToId(nodeId)
        return froms.union(tos)
    }

    fun deleteAll(connections: Collection<Connection>) {
        connectionRepo.deleteAll(connections)
    }


}