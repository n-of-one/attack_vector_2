package org.n1.av2.backend.model.ui

enum class ReduxActions {

    SERVER_TIME_SYNC,               // Send server time to client to make it use server time.

    SERVER_NOTIFICATION,
    SERVER_ERROR,

    SERVER_ADD_NODE,
    SERVER_ADD_CONNECTION,
    SERVER_FORCE_DISCONNECT,
    SERVER_MOVE_NODE,
    SERVER_SITE_FULL,
    SERVER_UPDATE_SITE_DATA,

    SERVER_UPDATE_NETWORK_ID,
    SERVER_UPDATE_LAYER,

    SERVER_UPDATE_SITE_STATE,       // Report errors or OK state

    SERVER_ADD_LAYER,
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

    SERVER_HACKER_PROBE_LAYERS,      // when arriving at a node that is state DISCOVERED or TYPE
    SERVER_HACKER_PROBE_CONNECTIONS, // when hacking OS


    SERVER_START_HACKING_ICE_PASSWORD, // sent to hacker that hacks this ice
    SERVER_ICE_PASSWORD_UPDATE,        // sent to run to inform of hacked status update
    SERVER_LAYER_HACKED,               // sent to run to update info, this is visible when clicking on icon
    SERVER_NODE_HACKED,                // change icon of node in run

    SERVER_START_HACKING_ICE_TANGLE,    // sent to hacker that hacks this ice
    SERVER_TANGLE_POINT_MOVED,          // some hacker moved a tangle point

    SERVER_START_COUNTDOWN,             // The client can show the countdown clock
    SERVER_COMPLETE_COUNTDOWN,          // The countdown completes

    SERVER_START_PATROLLER,             // Client shows a patroller (for example as a result of an alarm countdown)


    SERVER_LEASH_LOCKS_HACKER,          // Client can show leash locking hacker, and start back tracing.
    SERVER_PATROLLER_LOCKS_HACKER,      // Client can show this


}