package org.n1.av2.backend.model.ui

import org.n1.av2.backend.model.Timings

enum class NodeScanType(val timings: Timings) {
    SCAN_NODE_INITIAL(Timings(
        "in" to 50, "out" to 25, "connection" to 20
    )),
    SCAN_CONNECTIONS(Timings(
        "out" to 50, "in" to 25, "connection" to 20
    )),
    SCAN_NODE_DEEP(Timings(
        "in" to 50, "out" to 25, "connection" to 20
    )),

}