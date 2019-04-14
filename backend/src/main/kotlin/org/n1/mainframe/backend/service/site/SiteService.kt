package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.Connection
import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.site.Site
import org.n1.mainframe.backend.model.ui.DeleteNodeCommand
import org.n1.mainframe.backend.repo.SiteRepo
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service

@Service
class SiteService(
        val siteRepo: SiteRepo
) {

    fun getById(id: String): Site {
        return siteRepo.findById(id).orElseThrow { throw IllegalArgumentException("No site found for id: ${id}") }
    }

    fun findByName(name: String): Site? {
        val all = siteRepo.findAllByName(name)
        return if (all.size == 1) all.first()
        else null
    }

    fun createSite(name: String): Site {
        val id = createId("site", siteRepo::findById)
        val site = Site(id = id, name = name, hackTime = "15:00", startNodeId = "00")
        siteRepo.save(site)
        return site
    }

    fun purgeAll() {
        siteRepo.deleteAll()
    }

    fun save(site: Site) {
        siteRepo.save(site)
    }

    fun addNode(site: Site, node: Node) {
        site.nodeIds.add(node.id)
        save(site)
    }

    fun addConnection(site: Site, connection: Connection) {
        site.connectionIds.add(connection.id)
        save(site)
    }

    fun findAll(): MutableIterable<Site> {
        return siteRepo.findAll() ?: ArrayList()
    }

    fun deleteConnections(site: Site, connections: Collection<Connection>) {
        connections.forEach { site.connectionIds.remove(it.id) }
        siteRepo.save(site)
    }

    fun deleteNode(command: DeleteNodeCommand) {
        val site = getById(command.siteId)
        site.nodeIds.remove(command.nodeId)
        siteRepo.save(site)
    }


}