package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.Connection
import org.n1.mainframe.backend.model.site.Node
import org.n1.mainframe.backend.model.site.Site
import org.n1.mainframe.backend.model.ui.*
import org.n1.mainframe.backend.repo.SiteRepo
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service

@Service
class SiteService(
        val siteRepo: SiteRepo
) {

    fun getById(id: String): Site {
        return siteRepo.findOne(id)  ?: throw IllegalArgumentException("No site found for id: ${id}")
    }

    fun findByLink(link: String): Site? {
        return siteRepo.findByLink(link)
    }

    fun createSite(link: String): Site {
        val id = createId("site", siteRepo::findOne)
        val site = Site(id = id, link = link, name = link, hackTime = "15:00", startNode = "00")
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
        site.nodes.add(node.id)
        save(site)
    }

    fun addConnection(site: Site, connection: Connection) {
        site.connections.add(connection.id)
        save(site)
    }

    fun findAll(): MutableIterable<Site> {
        return siteRepo.findAll() ?: ArrayList()
    }

    fun deleteConnections(site: Site, connections: Collection<Connection>) {
        connections.forEach { site.connections.remove( it.id ) }
        siteRepo.save(site)
    }

    fun deleteNode(command: DeleteNodeCommand) {
        val site = getById(command.siteId)
        site.nodes.remove(command.nodeId)
        siteRepo.save(site)
    }



}