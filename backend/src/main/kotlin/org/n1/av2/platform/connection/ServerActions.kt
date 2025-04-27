package org.n1.av2.platform.connection

enum class ServerActions {

    SERVER_USER_CONNECTION,         // Tell the client that a new user-connection was established. Have the client check if this
                                    // is the connection it just made, or if a newer connection was made and the old one needs to shut down

    SERVER_TIME_SYNC,               // Send server time to client to make it use server time.

    SERVER_NOTIFICATION,
    SERVER_ERROR,

    SERVER_TASKS,                   // send all timed tasks to the server for debugging purpose

    SERVER_SITES_LIST,              // send list of sites

    SERVER_OPEN_EDITOR,             // Send to user to open the editor
    SERVER_ADD_NODE,
    SERVER_ADD_CONNECTION,
    SERVER_MOVE_NODE,
    SERVER_SITE_FULL,               // Send to editor
    SERVER_ALL_CORE_INFO,           // Send all core layers to editor for tripwire layers with remote core
    SERVER_UPDATE_SITE_DATA,        // Send update of SiteProperties

    SERVER_UPDATE_NETWORK_ID,
    SERVER_UPDATE_LAYER,

    SERVER_UPDATE_SITE_STATE,       // Report errors messages

    SERVER_ADD_LAYER,
    SERVER_NODE_UPDATED,

    SERVER_RECEIVE_CURRENT_USER,    // User details for current user
    SERVER_RECEIVE_USERS_OVERVIEW,  // List of users for user-overview
    SERVER_RECEIVE_EDIT_USER,       // User details for editing

    SERVER_RECEIVE_CONFIG,          // Send the config to the client

    SERVER_RECEIVE_SCRIPT_STATUS,   // Send the script status of a user to the client

    SERVER_SCRIPT_STATISTICS,       // Send the script statistics to the client
    SERVER_SCRIPT_TYPES,            // Send all script types to the client
    SERVER_EDIT_SCRIPT_TYPE,        // Send to the client that this script type is being edited

    SERVER_RECEIVE_SCRIPT_ACCESS,           // Send all script accesses of a user to the client


    SERVER_UPDATE_USER_RUNS,        // Sent at logon to hacker to update the runs shown on home page of that user
    SERVER_UPDATE_RUN_INFO,         // Scan info has changed (as displayed on home screen)
    SERVER_SITE_DISCOVERED,         // Result of scan site for name
    SERVER_ENTERING_RUN,            // Step 1 of entering a run: frontend can subscribe to topics and wait for SERVER_ENTERED_RUN
    SERVER_ENTERED_RUN,             // Step 2 of entering a run: frontend can now start processing messages for this run
    SERVER_RUN_TIMER,               // Optional resending of timer info for a site (at request of client)

    SERVER_RESET_ICE,               // Sent to ICE when site is reset and hacking stops. Also triggered by RotateIceEffectService

    SERVER_SITE_SHUTDOWN_START,     // All ice is reset and unhacked, all nodes are unhacked, threats reset, connections refused for shutdown time
    SERVER_SITE_SHUTDOWN_FINISH,    // All ice is reset and unhacked, all nodes are unhacked, threats reset, connections refused for shutdown time


    SERVER_TERMINAL_SYNTAX_HIGHLIGHTING,    // transfer syntax highlighting to terminal
    SERVER_TERMINAL_RECEIVE,                // Message to render in the terminal
    SERVER_TERMINAL_UPDATE_PROMPT,          // Update the prompt of the terminal

    SERVER_PROBE_LAUNCH,
    SERVER_UPDATE_NODE_STATUS,
    SERVER_DISCOVER_NODES,

    SERVER_HACKER_ENTER_SITE,           // A hacker enters a site, at scan range  (notify other hackers about this)
    SERVER_HACKER_LEAVE_SITE,           // A hacker leaves a site, for example when browser disconnects or moves to home screen.
    SERVER_HACKER_DC,                   // A hacker disconnects from the site but is still at the site

    SERVER_HACKER_START_ATTACK,         // A hacker starts the attack (connects to the entry site node)
    SERVER_HACKER_MOVE_START,           // step one of the move
    SERVER_HACKER_MOVE_ARRIVE,          // step two of the move

    SERVER_HACKER_SCANS_NODE,           // when arriving at a node that is state DISCOVERED or TYPE

    SERVER_START_TIMER,                 // The client can show the timer countdown
    SERVER_COMPLETE_TIMER,              // The timer countdown completes
    SERVER_CHANGE_TIMER,                // The timer countdown changes due to a script or skill

    SERVER_FLASH_PATROLLER,             // Clients show a patroller for a brief period of time (visual effect only)

    SERVER_REDIRECT_HACK_ICE,           // open a new window to start hacking ICE
    SERVER_ICE_HACKED,                  // sent to ice screen to indicate ICE has been hacked.
    SERVER_LAYER_HACKED,                // sent to run to update info, this is visible when clicking on icon
    SERVER_NODE_HACKED,                 // change icon of node in run
    SERVER_LAYER_CHANGED,               // sent to run to update info, this is visible when clicking on icon

    SERVER_ICE_HACKERS_UPDATED,         // sent to ice to inform all hackers of the current hackers on this ice

    SERVER_STATUS_LIGHT_UPDATE,         // sent to app and widget when loaded or status changed

    SERVER_AUTH_ENTER,                  // sent to hacker or user entering the Ice, first message of Ice window
    SERVER_AUTH_UPDATE,                 // sent to site to inform of hacked status update
    SERVER_AUTH_PASSWORD_CORRECT,       // sent to user to inform the Auth app that the password/passcode was correct

    SERVER_TANGLE_ENTER,                // sent to hacker that hacks this ice, first message of Ice window
    SERVER_TANGLE_POINT_MOVED,          // some hacker moved a tangle point
    SERVER_TANGLE_CLUSTERS_REVEALED,    // sent to hackers when clusters are revealed

    SERVER_WORD_SEARCH_ENTER,           // sent to hacker that hacks this ice, first message of Ice window
    SERVER_WORD_SEARCH_UPDATED,         // sent to run when a word was found

    SERVER_NETWALK_ENTER,               // sent to hacker that hacks this ice, first message of Ice window
    SERVER_NETWALK_NODE_ROTATED,        // sent to run when a node was rotated clockwise

    SERVER_TAR_ENTER,                   // sent to hacker that hacks this ice, first message of Ice window
    SERVER_TAR_UPDATE,                  // sent to run when a tar ice has change (units hacked)

    SERVER_SWEEPER_ENTER,               // sent to hacker that hacks this ice, first message of Ice window
    SERVER_SWEEPER_MODIFY,              // sent to everyone when a cell has been modified (revealed, flagged,... )
    SERVER_SWEEPER_BLOCK_USER,          // sent to everyone when a user is blocked because of exploding a cell
    SERVER_SWEEPER_UNBLOCK_USER,        // sent to everyone when a user is unblocked because of script or debug action
    SERVER_SWEEPER_RESET_START,         // sent to everyone when someone initiates reset
    SERVER_SWEEPER_RESET_STOP,          // sent to everyone when someone stops reset
    SERVER_SWEEPER_RESET_COMPLETE,      // sent to everyone when someone completes reset

    SERVER_REDIRECT_CONNECT_ICE,        // open a new window to connect to ICE auth UI
    SERVER_REDIRECT_CONNECT_APP,        // open a new window to connect to an app

    SERVER_REDIRECT_NEXT_LAYER,         // send by server to user when externally hacking ICE and moving player to next layer (ice or app).

    SERVER_SPEAK,                        // send to Lola to have them say something

}
