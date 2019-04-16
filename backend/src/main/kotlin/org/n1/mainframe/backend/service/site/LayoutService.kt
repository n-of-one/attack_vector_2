package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.Connection
import org.n1.mainframe.backend.model.site.Layout
import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.repo.LayoutRepo
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

@Service
class LayoutService(
        val layoutRepo: LayoutRepo ) {


    fun getById(id: String): Layout {
        return layoutRepo.findById(id).orElseThrow { throw IllegalArgumentException("No site found for id: ${id}") }
    }
// TODO: implement
//    fun purgeAll() {
//        layoutRepo.deleteAll()
//    }

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
        val layout = getById(siteId)
        layout.nodeIds.remove(nodeId)
        layoutRepo.save(layout)
    }

    fun findById(id: String): Layout {
        return layoutRepo.findById(id).orElseThrow { IllegalStateException("Did not find expected site layout for id: ${id}") }
    }

    fun purgeAll() {
        layoutRepo.deleteAll()
    }

    fun create(id: String): Layout {
        val layout = Layout(id = id)
        layoutRepo.save(layout)
        return layout
    }

}