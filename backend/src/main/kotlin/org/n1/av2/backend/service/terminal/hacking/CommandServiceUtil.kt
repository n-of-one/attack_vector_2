package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.site.Node


fun findBlockingIceLayer(node: Node): Int? {

    // FIXME: val blockingIce = node.services.reversed().find { it.type.ice && !it.hacked } ?: return null
    val blockingIce = node.services.reversed().find { it.type.ice } ?: return null
    return blockingIce.layer
}