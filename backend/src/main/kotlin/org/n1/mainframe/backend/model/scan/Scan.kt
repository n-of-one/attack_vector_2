package org.n1.mainframe.backend.model.scan

data class Scan(

    val id: String,
    val siteId: String,
    var complete: Boolean,
    val nodeStatusById:MutableMap<String, NodeStatus>
)