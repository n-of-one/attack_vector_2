package org.n1.mainframe.backend.service

enum class ReduxActions {

    SERVER_NOTIFICATION,
    SERVER_ERROR,

    SERVER_ADD_NODE,
    SERVER_ADD_CONNECTION,
    SERVER_FORCE_DISCONNECT,
    SERVER_MOVE_NODE,
    SERVER_SITE_FULL,
    SERVER_UPDATE_SITE_DATA,


    SERVER_SCAN_FULL,

    SERVER_TERMINAL_RECEIVE,
    SERVER_PROBE_LAUNCH,

    SERVER_UPDATE_NODE_STATUS,
    SERVER_DISCOVER_NODES,

    SERVER_USER_DC,


}