const SERVER_SCAN_FULL = "SERVER_SCAN_FULL";
const SERVER_PROBE_LAUNCH = "SERVER_PROBE_LAUNCH";

const PROBE_SCAN_NODE = "PROBE_SCAN_NODE";
const SERVER_UPDATE_NODE_STATUS = "SERVER_UPDATE_NODE_STATUS";
const SERVER_DISCOVER_NODES = "SERVER_DISCOVER_NODES";
const AUTO_SCAN = "AUTO_SCAN";

/** Event to ignore while waiting for the scan full result */
const WAITING_FOR_SCAN_IGNORE_LIST = [ SERVER_PROBE_LAUNCH,
    PROBE_SCAN_NODE,
    SERVER_UPDATE_NODE_STATUS,
    SERVER_DISCOVER_NODES,
    AUTO_SCAN,];

export {
    SERVER_SCAN_FULL,
    SERVER_PROBE_LAUNCH,
    PROBE_SCAN_NODE,
    SERVER_UPDATE_NODE_STATUS,
    SERVER_DISCOVER_NODES,
    AUTO_SCAN,

    WAITING_FOR_SCAN_IGNORE_LIST,
}