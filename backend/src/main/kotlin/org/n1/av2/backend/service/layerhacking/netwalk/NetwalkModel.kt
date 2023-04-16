package org.n1.av2.backend.service.layerhacking.netwalk

import org.n1.av2.backend.entity.ice.NetwalkCellType

class NetwalkCell(
    val x: Int,
    val y: Int,
    val type: NetwalkCellType,
    val neighbourNorth: NetwalkCell?,
    val neighbourEast: NetwalkCell?,
    val neighbourSouth: NetwalkCell?,
    val neighbourWest: NetwalkCell?,
)