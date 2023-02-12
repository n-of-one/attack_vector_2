package org.n1.av2.backend.model.ui

import org.n1.av2.backend.model.Ticks

enum class NodeScanType(val ticks: Ticks) {
    SCAN_NODE_INITIAL(Ticks("in" to 50, "out" to 25)),
    SCAN_CONNECTIONS(Ticks("out" to 50, "in" to 25)),
    SCAN_NODE_DEEP(Ticks("in" to 50, "out" to 25)),

}