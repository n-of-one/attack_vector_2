package org.n1.mainframe.backend.model.scan

data class NodeScan(
        var status: NodeStatus,
        var distance: Int? = null
)