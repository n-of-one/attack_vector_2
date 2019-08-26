package org.n1.av2.backend.model.ui

enum class ReduxActions {

    SERVER_NOTIFICATION,
    SERVER_ERROR,

    SERVER_ADD_NODE,
    SERVER_ADD_CONNECTION,
    SERVER_FORCE_DISCONNECT,
    SERVER_MOVE_NODE,
    SERVER_SITE_FULL,
    SERVER_UPDATE_SITE_DATA,

    SERVER_UPDATE_NETWORK_ID,
    SERVER_UPDATE_SERVICE,

    SERVER_UPDATE_SITE_STATE,       // Report errors or OK state

    SERVER_ADD_SERVICE,
    SERVER_NODE_UPDATED,


    SERVER_RECEIVE_USER_SCANS,      // Scans shown on home page of user
    SERVER_UPDATE_SCAN_INFO,        // Scan info has changed (as displayed on home screen)
    SERVER_SITE_DISCOVERED,         // Result of scan site for name
    SERVER_SCAN_FULL,               // Result of user enter scan (for the user itself)

    SERVER_TERMINAL_SYNTAX_HIGHLIGHTING, // transfer syntax highlighting to terminal
    SERVER_TERMINAL_RECEIVE,
    SERVER_PROBE_LAUNCH,
    SERVER_UPDATE_NODE_STATUS,
    SERVER_DISCOVER_NODES,

    SERVER_USER_DC,                 // Specific command that tells browser to move to another page.

    SERVER_HACKER_ENTER_SCAN,       // A hacker enters a scan (notify other hackers about this)
    SERVER_HACKER_LEAVE_SCAN,       // A hacker leaves a scan

    SERVER_HACKER_START_ATTACK,
    SERVER_HACKER_MOVE_START,       // step one of the move
    SERVER_HACKER_MOVE_ARRIVE,      // step two of the move

    SERVER_HACKER_PROBE_SERVICES,    // when arriving at a node that is state DISCOVERED or TYPE
    SERVER_HACKER_PROBE_CONNECTIONS, // when hacking OS


    SERVER_START_HACKING_ICE_PASSWORD, // sent to hacker that hacks this ice
    SERVER_ICE_PASSWORD_UPDATE,        // sent to run to inform of hacked status update
    SERVER_ICE_HACKED,                 // change icon

}