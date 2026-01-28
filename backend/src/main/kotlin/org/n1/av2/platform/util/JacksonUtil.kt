package org.n1.av2.platform.util

import com.fasterxml.jackson.databind.JsonNode


fun JsonNode.asList(): List<JsonNode> {
    if (!this.isArray) error("Not an array")
    return this.elements().asSequence().toList()
}


fun JsonNode.asMap(): Map<String, Any> {
    if (!this.isObject) error("Not an object")
    val map = this.fields().asSequence().map { (key, value) ->
        key to value
    }.toMap()
    return map
}

fun mapJsonField(jsonNode: JsonNode): Any{
    return when {
        jsonNode.isTextual -> jsonNode.asText()
        jsonNode.isObject -> jsonNode.asMap()
        jsonNode.isArray -> jsonNode.asList()
        jsonNode.isBoolean -> jsonNode.asBoolean()
        jsonNode.isInt -> jsonNode.asInt()
        jsonNode.isLong -> jsonNode.asLong()
        jsonNode.isDouble -> jsonNode.asDouble()
        else -> error("Unsupported json node type: ${jsonNode.nodeType}")
    }
}
