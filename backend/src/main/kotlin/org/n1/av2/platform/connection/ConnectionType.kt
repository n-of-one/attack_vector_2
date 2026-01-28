package org.n1.av2.platform.connection

const val HACKER_ENDPOINT = "/ws_hacker"
const val NETWORKED_APP_ENDPOINT = "/ws_networked_app"
const val UNRESTRICTED_ENDPOINT = "/ws_unrestricted"

// See: /doc/WebsocketConnections.md
enum class ConnectionType {
    WS_UNRESTRICTED,
    WS_HACKER_MAIN,
    WS_NETWORK_APP,
    STATELESS,
    NONE // used for internal processes that don't  have a connection, but do have a UserPrincipal that has a connectionType property.
}


fun connectionTypeFromPath(path: String): ConnectionType {
    return when (path) {
        UNRESTRICTED_ENDPOINT -> ConnectionType.WS_UNRESTRICTED
        HACKER_ENDPOINT -> ConnectionType.WS_HACKER_MAIN
        NETWORKED_APP_ENDPOINT -> ConnectionType.WS_NETWORK_APP
        else -> ConnectionType.STATELESS
    }
}
