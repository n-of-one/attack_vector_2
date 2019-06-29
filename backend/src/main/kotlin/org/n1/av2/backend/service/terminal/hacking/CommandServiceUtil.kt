package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.site.Node


fun findBlockingIceLayer(node: Node): Int? {
    val blockingIce = node.services.reversed().find { it.type.ice && !it.hacked } ?: return null
    return blockingIce.layer
}