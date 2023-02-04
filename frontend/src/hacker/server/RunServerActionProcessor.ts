import {Store} from "redux";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {initGenericServerActions} from "./GenericServerActionProcessor";
import {HACKER_HOME, NAVIGATE_PAGE} from "../../common/menu/pageReducer";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {NodeScanType,runCanvas} from "../run/component/RunCanvas";
import {
    SERVER_DISCOVER_NODES,
    SERVER_HACKER_ENTER_SCAN,
    SERVER_HACKER_LEAVE_SCAN,
    SERVER_PROBE_LAUNCH
} from "../run/model/ScanActions";
import {Scan, UpdateNodeStatusAction} from "../run/reducer/ScanReducer";
import {Site} from "../run/reducer/SiteReducer";
import {HackerPresence} from "../run/reducer/HackersReducer";
import {SERVER_HACKER_MOVE_ARRIVE, SERVER_HACKER_MOVE_START, SERVER_HACKER_PROBE_LAYERS, SERVER_HACKER_START_ATTACK, SERVER_NODE_HACKED} from "../run/model/HackActions";
import {Ticks} from "../../common/model/Ticks";
import {SERVER_ICE_PASSWORD_UPDATE, SERVER_START_HACKING_ICE_PASSWORD} from "../run/ice/password/PasswordIceReducer";
import {passwordIceManager, PasswordIceState} from "../run/ice/password/PasswordIceManager";
import {delayTicks} from "../../common/Util";
import {
    SERVER_FLASH_PATROLLER,
    SERVER_PATROLLER_HOOKS_HACKER, SERVER_PATROLLER_LOCKS_HACKER,
    SERVER_PATROLLER_MOVE, SERVER_PATROLLER_REMOVE,
    SERVER_PATROLLER_SNAPS_BACK_HACKER,
    SERVER_START_TRACING_PATROLLER
} from "../run/coundown/CountdownReducer";
import {enterScan} from "../home/HackerHome";

export const SERVER_HACKER_DC = "SERVER_HACKER_DC"
export const SERVER_SCAN_FULL = "SERVER_SCAN_FULL"
export const SERVER_UPDATE_NODE_STATUS = "SERVER_UPDATE_NODE_STATUS"
export const SERVER_SITE_DISCOVERED = "SERVER_SITE_DISCOVERED"

/** Event to ignore while waiting for the scan full result */
export const WAITING_FOR_SCAN_IGNORE_LIST =
    [
        SERVER_PROBE_LAUNCH,
        SERVER_UPDATE_NODE_STATUS,
        SERVER_DISCOVER_NODES,
    ]

export interface SiteAndScan {
    scan: Scan,
    site: Site,
    hackers: HackerPresence[],
    patrollers: PatrollerData[],
}

export interface ProbeResultConnections {
    nodeIds: string[],
    connectionIds: string[]
}

export interface ProbeAction {
    probeUserId: string,
    path: string[],
    scanType: NodeScanType,
    autoScan: boolean
}

export interface StartRun {
    userId: string,
    quick: boolean
}

export interface MoveStartAction {
    userId: string,
    nodeId: string,
    ticks: Ticks
}

export interface MoveArriveAction {
    nodeId: string,
    userId: string
}

export interface HackerProbeLayersAction {
    userId: string,
    ticks: Ticks
}

export interface HackerProbeConnectionsAction {
    nodeId: string,
    userId: string
}

export interface NodeHacked {
    nodeId: string,
    delay: number
}

interface FlashPatrollerAction {
    nodeId: string
}

export interface PatrollerData {
    patrollerId: string | null,
    nodeId: string,
    path: PatrollerPathSegment[],
    ticks: Ticks
}

export interface PatrollerPathSegment {
    fromNodeId: string,
    toNodeId: string
}

export interface ActionSnapBack {
    hackerId: string,
    ticks: Ticks
}

export interface ActionPatrollerCatchesHacker {
    patrollerId: string,
    hackerId: string
}

export interface ActionPatrollerMove {
    patrollerId: string
    fromNodeId: string,
    toNodeId: string,
    ticks: Ticks
}

export interface PatrollerHooksHackerAction {
    hackerId: string
}

interface RemovePatrollerAction {
    patrollerId: string
}

export const initRunServerActions = (store: Store) => {

    const dispatch = store.dispatch

    initGenericServerActions()

    webSocketConnection.addAction(SERVER_HACKER_DC, (_: any) => {
        const currentPage = store.getState().currentPage

        webSocketConnection.unsubscribe();
        terminalManager.stop();
        runCanvas.stop();

        dispatch({type: NAVIGATE_PAGE, to: HACKER_HOME, from: currentPage})
    })

    webSocketConnection.addAction(SERVER_SITE_DISCOVERED, ({runId, siteId}: { runId: string, siteId: string }) => {
        const currentPage = store.getState().currentPage
        enterScan(runId, siteId, dispatch, currentPage)
    })

    webSocketConnection.addAction(SERVER_SCAN_FULL, (data: SiteAndScan) => {
        runCanvas.loadScan(data)
    })

    webSocketConnection.addAction(SERVER_UPDATE_NODE_STATUS, (data: UpdateNodeStatusAction) => {
        runCanvas.updateNodeStatus(data)
    })

    webSocketConnection.addAction(SERVER_DISCOVER_NODES, ({nodeIds, connectionIds}: ProbeResultConnections) => {
        runCanvas.discoverNodes(nodeIds, connectionIds);
    })

    webSocketConnection.addAction(SERVER_PROBE_LAUNCH, (data: ProbeAction) => {
        runCanvas.launchProbe(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_ENTER_SCAN, (data: HackerPresence) => {
        runCanvas.hackerEnter(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_LEAVE_SCAN, (data: any) => {
        runCanvas.hackerLeave(data.userId)
    })

    webSocketConnection.addAction(SERVER_HACKER_START_ATTACK, (data: StartRun) => {
        runCanvas.startAttack(data.userId, data.quick)
    })

    webSocketConnection.addAction(SERVER_HACKER_MOVE_START, (data: MoveStartAction) => {
        runCanvas.moveStart(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_MOVE_ARRIVE, (data: MoveArriveAction) => {
        runCanvas.moveArrive(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_PROBE_LAYERS, (data: HackerProbeLayersAction) => {
        runCanvas.hackerProbeLayersSaga(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_DC, (data: HackerProbeConnectionsAction) => {
        runCanvas.hackerProbeConnections(data)
    })

    webSocketConnection.addAction(SERVER_START_HACKING_ICE_PASSWORD, (_: any) => {
        passwordIceManager.passwordIceStartHack()
    })

    webSocketConnection.addAction(SERVER_ICE_PASSWORD_UPDATE, (data: PasswordIceState) => {
        passwordIceManager.serverPasswordIceUpdate(data)
    })

    webSocketConnection.addAction(SERVER_NODE_HACKED, (data: NodeHacked) => {
        delayTicks(data.delay, () => {runCanvas.nodeHacked(data.nodeId) } )
    })

    webSocketConnection.addAction(SERVER_FLASH_PATROLLER, (data: FlashPatrollerAction) => {
        runCanvas.flashTracingPatroller(data.nodeId)
    })

    webSocketConnection.addAction(SERVER_START_TRACING_PATROLLER, (data: PatrollerData) => {
        runCanvas.activateTracingPatroller(data)
    })

    webSocketConnection.addAction(SERVER_PATROLLER_MOVE, (data: ActionPatrollerMove) => {
        runCanvas.movePatroller(data)
    })

    webSocketConnection.addAction(SERVER_PATROLLER_HOOKS_HACKER, (data: PatrollerHooksHackerAction) => {
        runCanvas.patrollerHooksHacker(data.hackerId)
    })

    webSocketConnection.addAction(SERVER_PATROLLER_SNAPS_BACK_HACKER, (data: ActionSnapBack) => {
        runCanvas.patrollerSnacksBackHacker(data)
    })

    webSocketConnection.addAction(SERVER_PATROLLER_LOCKS_HACKER, (data: ActionPatrollerCatchesHacker) => {
        runCanvas.patrollerLocksHacker(data)
    })

    webSocketConnection.addAction(SERVER_PATROLLER_REMOVE, (data: RemovePatrollerAction) => {
        runCanvas.removePatroller(data.patrollerId)
    })

}