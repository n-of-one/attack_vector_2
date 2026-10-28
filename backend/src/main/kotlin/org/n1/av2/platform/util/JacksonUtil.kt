package org.n1.av2.platform.util

import tools.jackson.databind.JsonNode


fun JsonNode.asList(): List<JsonNode> {
    if (!this.isArray) error("Not an array")
    return this.values().toList()
}
