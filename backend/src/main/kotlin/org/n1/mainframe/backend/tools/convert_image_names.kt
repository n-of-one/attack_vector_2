package org.n1.mainframe.backend.tools

import java.io.File
import java.util.*

val allNames = TreeSet<String>()

/**
 * Script that converts the image names as they are in the "Icons etc" set (from mysitemyway.com)
 * to the names used in Attack Vector 2.
 */

val map = hashMapOf(
        "box2.png" to "data_store.png",
        "cube.png" to "syscon.png",
        "puzzle3-ps.png" to "passcode_store.png",
        "shape-square-frame.png" to "transit_1.png",
        "shapes-circle-frame.png" to "transit_2.png",
        "shapes-diamond-frame.png" to "transit_3.png",
        "shapes-hexagon-frame.png" to "transit_4.png",
        "tile2.png" to "resource_store.png",
        "snowflake4-sc37.png" to "unhackable.png",
        "flower3.png" to "manual_1.png",
        "flower13-sc36.png" to "manual_2.png",
        "flower-cauliflower.png" to "manual_3.png",
        "spinner8-sc36.png" to "ice_1.png",
        "spinner2-sc30.png" to "ice_2.png",
        "spinner3-sc36.png" to "ice_3.png",
        "spinner9-sc36.png" to "ice_4.png"
)

//fun main(args: Array<String>) {
//    val dir = File("C:\\Projects\\Attack_Vector\\_attack_vector2\\frontend\\public\\img\\frontier\\nodes")
//    dir.walk().forEach { process(it) }
////    allNames.sorted().forEach { println(it) }
//
//}

fun process(file: File) {
    if (file.isFile) allNames.add(file.name)
    if (map.containsKey(file.name)) {
        val newFile = File(file.parentFile, map[file.name])
        println("renaming ${file} to ${newFile}")
        file.renameTo(newFile)
    }
    else {
        val newFile = File(file.parentFile, file.name.toLowerCase())
        println("renaming ${file} to ${newFile}")
        file.renameTo(newFile)
    }
}
