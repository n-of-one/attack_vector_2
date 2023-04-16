package org.n1.av2.backend.entity.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


enum class Direction(val dx: Int, val dy: Int) {
    N(0, -1),
    E(1, 0),
    S(0, 1),
    W(-1, 0),
}

data class Point(val x: Int, val y: Int)

enum class NetwalkCellType(
    val n: Boolean,
    val e: Boolean,
    val s: Boolean,
    val w: Boolean,
    val textImage: Char,
) {
    ESW(n = false, e = true, s = true, w = true, textImage = '┳'),
    NSW(n = true, e = false, s = true, w = true, textImage = '┫'),
    NEW(n = true, e = true, s = false, w = true, textImage = '┻'),
    NES(n = true, e = true, s = true, w = false, textImage = '┣'),
    EW(n = false, e = true, s = false, w = true, textImage = '━'),
    NS(n = true, e = false, s = true, w = false, textImage = '┃'),
    N(n = true, e = false, s = false, w = false, textImage = '╵'),
    E(n = false, e = true, s = false, w = false, textImage = '╶'),
    S(n = false, e = false, s = true, w = false, textImage = '╷'),
    W(n = false, e = false, s = false, w = true, textImage = '╴'),
    SW(n = false, e = false, s = true, w = true, textImage = '┓'),
    NW(n = true, e = false, s = false, w = true, textImage = '┛'),
    NE(n = true, e = true, s = false, w = false, textImage = '┗'),
    SE(n = false, e = true, s = true, w = false, textImage = '┏'),
    NESW(n = true, e = true, s = true, w = true, textImage = '╋'),
}

val rotateRightMap = mapOf(
    NetwalkCellType.ESW to NetwalkCellType.NSW,
    NetwalkCellType.NSW to NetwalkCellType.NEW,
    NetwalkCellType.NEW to NetwalkCellType.NES,
    NetwalkCellType.NES to NetwalkCellType.ESW,

    NetwalkCellType.EW to NetwalkCellType.NS,
    NetwalkCellType.NS to NetwalkCellType.EW,

    NetwalkCellType.N to NetwalkCellType.E,
    NetwalkCellType.E to NetwalkCellType.S,
    NetwalkCellType.S to NetwalkCellType.W,
    NetwalkCellType.W to NetwalkCellType.N,

    NetwalkCellType.SW to NetwalkCellType.NW,
    NetwalkCellType.NW to NetwalkCellType.NE,
    NetwalkCellType.NE to NetwalkCellType.SE,
    NetwalkCellType.SE to NetwalkCellType.SW,

    NetwalkCellType.NESW to NetwalkCellType.NESW,
)

fun NetwalkCellType.rotateClockwise(): NetwalkCellType {
    return rotateRightMap[this]!!
}


data class NetwalkCellMinimal(
    val type: NetwalkCellType,
    val connected: Boolean
)

data class NetwalkEntity(
    val id: String,
    val runId: String,
    val nodeId: String,
    val layerId: String,
    val strength: IceStrength,
    val hacked: Boolean,

    val cellGrid: List<List<NetwalkCellMinimal>>,
)

@Repository
interface NetwalkStatusRepo : CrudRepository<NetwalkEntity, String> {
}
