package org.n1.av2.backend.service.site

import org.n1.av2.backend.model.db.site.Connection
import org.n1.av2.backend.model.db.site.Layout
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.repo.LayoutRepo
import org.springframework.stereotype.Service

@Service
class LayoutService(
        val layoutRepo: LayoutRepo) {


    /* get by Site Id */
    fun getBySiteId(siteId: String): Layout {
        return layoutRepo.findBySiteId(siteId) ?: error("No site found for id: ${siteId}")
    }

    fun save(layout: Layout) {
        layoutRepo.save(layout)
    }

    fun addNode(layout: Layout, node: Node) {
        layout.nodeIds.add(node.id)
        save(layout)
    }

    fun addConnection(layout: Layout, connection: Connection) {
        layout.connectionIds.add(connection.id)
        save(layout)
    }

    fun deleteConnections(layout: Layout, connections: Collection<Connection>) {
        connections.forEach { layout.connectionIds.remove(it.id) }
        layoutRepo.save(layout)
    }

    fun deleteNode(siteId: String, nodeId: String) {
        val layout = getBySiteId(siteId)
        layout.nodeIds.remove(nodeId)
        layoutRepo.save(layout)
    }

    fun purgeAll() {
        layoutRepo.deleteAll()
    }

    fun create(id: String): Layout {
        val layout = Layout(siteId = id)
        layoutRepo.save(layout)
        return layout
    }

}