import {AnyAction, Store} from "redux"
import {webSocketConnection} from "../../common/server/WebSocketConnection"
import {initGenericServerActions} from "./GenericServerActionProcessor"
import {NodeScanType, runCanvas} from "../run/component/RunCanvas"
import {
    SERVER_DISCOVER_NODES,
    SERVER_HACKER_ENTER_SITE,
    SERVER_HACKER_LEAVE_SITE,
    SERVER_PROBE_LAUNCH
} from "../run/model/ScanActions"
import {Scan, UpdateNodeStatusAction} from "../run/reducer/ScanReducer"
import {Site} from "../run/reducer/SiteReducer"
import {HackerPresence} from "../run/reducer/HackersReducer"
import {Timings} from "../../common/model/Ticks"
import {delayTicks, avEncodedUrl} from "../../common/util/Util"
import {
    SERVER_FLASH_PATROLLER,
    SERVER_PATROLLER_LOCKS_HACKER,
    SERVER_PATROLLER_MOVE, SERVER_PATROLLER_REMOVE,
    SERVER_START_TRACING_PATROLLER
} from "../run/coundown/CountdownReducer"
import {enterScan} from "../home/HackerHome"
import {SERVER_TERMINAL_RECEIVE, TERMINAL_LOCK, TERMINAL_RECEIVE, TERMINAL_UNLOCK} from "../../common/terminal/TerminalReducer"
import {Schedule} from "../../common/util/Schedule"
import {NodeScanStatus} from "../../common/enums/NodeStatus";
import {ICE_DISPLAY_TERMINAL_ID, MAIN_TERMINAL_ID} from "../../common/terminal/ActiveTerminalIdReducer";
import {currentUser} from "../../common/user/CurrentUser";

export const SERVER_HACKER_START_ATTACK = "SERVER_HACKER_START_ATTACK"
export const SERVER_HACKER_MOVE_START = "SERVER_HACKER_MOVE_START"
export const SERVER_HACKER_MOVE_ARRIVE = "SERVER_HACKER_MOVE_ARRIVE"
export const SERVER_HACKER_MOVE_ARRIVE_FAIL = "SERVER_HACKER_MOVE_ARRIVE_FAIL"
export const SERVER_HACKER_SCANS_NODE = "SERVER_HACKER_SCANS_NODE"
export const SERVER_LAYER_HACKED = "SERVER_LAYER_HACKED"
export const SERVER_NODE_HACKED = "SERVER_NODE_HACKED"

export const SERVER_HACKER_DC = "SERVER_HACKER_DC"
export const SERVER_ENTER_RUN = "SERVER_ENTER_RUN"
export const SERVER_UPDATE_NODE_STATUS = "SERVER_UPDATE_NODE_STATUS"
export const SERVER_SITE_DISCOVERED = "SERVER_SITE_DISCOVERED"

export const SERVER_REDIRECT_HACK_ICE = "SERVER_REDIRECT_HACK_ICE"
export const SERVER_REDIRECT_CONNECT_ICE = "SERVER_REDIRECT_CONNECT_ICE"
export const SERVER_REDIRECT_CONNECT_APP = "SERVER_REDIRECT_CONNECT_APP"

export const SERVER_SITE_RESET = "SERVER_SITE_RESET"


/** Event to ignore while waiting for the scan full result */
export const WAITING_FOR_SCAN_IGNORE_LIST =
    [
        SERVER_PROBE_LAUNCH,
        SERVER_UPDATE_NODE_STATUS,
        SERVER_DISCOVER_NODES,
    ]

export interface SiteAndScan {
    run: Scan,
    site: Site,
    hackers: HackerPresence[],
    patrollers: PatrollerData[],
}

export interface NodeStatusById {
    [key: string]: NodeScanStatus
}
export interface ProbeResultConnections {
    nodeStatusById: NodeStatusById,
    connectionIds: string[]
}

export interface ProbeAction {
    probeUserId: string,
    path: string[],
    scanType: NodeScanType,
    timings: Timings
}

export interface StartRun {
    userId: string,
    quick: boolean,
    timings: Timings
}

export interface MoveStartAction {
    userId: string,
    nodeId: string,
    timings: Timings
}

export interface MoveArriveAction {
    nodeId: string,
    userId: string,
    timings: Timings
}

export interface MoveArriveFailAction {
    userId: string,
}

export interface HackerScansNodeAction {
    userId: string,
    nodeId: string,
    timings: Timings
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
    timings: Timings
}

export interface PatrollerPathSegment {
    fromNodeId: string,
    toNodeId: string
}

export interface ActionPatrollerCatchesHacker {
    patrollerId: string,
    hackerId: string
}

export interface ActionPatrollerMove {
    patrollerId: string
    fromNodeId: string,
    toNodeId: string,
    timings: Timings
}

interface RemovePatrollerAction {
    patrollerId: string
}

interface RedirectHackIce {
    iceId: string
}

interface RedirectConnectIce {
    layerId: string
}

interface RedirectConnectApp {
    layerId: string,
    type: string
}

export const initRunServerActions = (store: Store) => {

    const dispatch = store.dispatch
    let hackerSchedule: Schedule | null = null

    initGenericServerActions()

    const echo = (time: number, message: string) => {
        hackerSchedule!.run(time, () => {
            dispatch({type: SERVER_TERMINAL_RECEIVE, data: {terminalId: MAIN_TERMINAL_ID, lines: [message]}})
        })
    }

    webSocketConnection.addAction(SERVER_SITE_DISCOVERED, ({runId, siteId}: { runId: string, siteId: string }) => {
        const currentPage = store.getState().currentPage
        enterScan(runId, siteId, dispatch, currentPage)
    })

    webSocketConnection.addAction(SERVER_ENTER_RUN, (data: SiteAndScan) => {
        runCanvas.enterSite(data)
    })

    webSocketConnection.addAction(SERVER_UPDATE_NODE_STATUS, (data: UpdateNodeStatusAction) => {
        runCanvas.updateNodeStatus(data.nodeId, data.newStatus)
    })

    webSocketConnection.addAction(SERVER_DISCOVER_NODES, ({nodeStatusById, connectionIds}: ProbeResultConnections) => {
        runCanvas.discoverNodes(nodeStatusById, connectionIds)
    })

    webSocketConnection.addAction(SERVER_PROBE_LAUNCH, (data: ProbeAction) => {
        runCanvas.launchProbe(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_ENTER_SITE, (data: HackerPresence) => {
        console.log("SERVER_HACKER_ENTER_SITE" + JSON.stringify(data))
        runCanvas.hackerEnter(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_LEAVE_SITE, (data: any) => {
        console.log("SERVER_HACKER_LEAVE_SITE" + JSON.stringify(data))
        runCanvas.hackerLeave(data.userId)
    })

    webSocketConnection.addAction(SERVER_HACKER_START_ATTACK, (data: StartRun) => {
        runCanvas.startAttack(data.userId, data.quick, data.timings)
        if (data.userId === currentUser.id) {
            // dispatch({type: TERMINAL_LOCK, terminalId: MAIN_TERMINAL_ID})
            const random = (max: number) => Math.floor(Math.random() * max)
            const personaId = "" + random(10) + random(10) + random(10) + random(10) + random(10) + random(10) + '-' +
                random(10) + random(10) + random(10) + random(10) + '/' + random(10)

            hackerSchedule = new Schedule(null)

            if (data.quick) {
                echo(0, "[info]Persona established, hack started.")
                echo(0, "")
            }
            else {
                echo(20, "")
                echo(20, "Persona v2.3 booting")
                echo(10, "- unique ID: " + personaId)
                echo(10, "- Matching fingerprint with OS deamon")
                echo(10, "  - [ok]ok[/] Suppressing persona signature")
                echo(10, "  - [ok]ok[/] Connection bandwidth adjusted")
                echo(10, "  - [ok]ok[/] Content masked.")
                echo(30, "  - [ok]ok[/] Operating speed reduced to mimic OS deamon")
                echo(30, "  - [ok]ok[/] Network origin obfuscated ")
                echo(20, "- Persona creation [info]complete")
                echo(0, "")
                echo(80, "Entering node")
                echo(0, "Persona accepted by node OS.")
                echo(0, "")
            }

        }
    })

    interface DisconnectAction { userId: string }
    webSocketConnection.addAction(SERVER_HACKER_DC, (action: DisconnectAction ) => {
        runCanvas.disconnect(action.userId)
    })

    webSocketConnection.addAction(SERVER_SITE_RESET, (action: AnyAction ) => {
        runCanvas.siteReset()
    })

    webSocketConnection.addAction(SERVER_HACKER_MOVE_START, (data: MoveStartAction) => {
        runCanvas.moveStart(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_MOVE_ARRIVE, (data: MoveArriveAction) => {
        runCanvas.moveArrive(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_MOVE_ARRIVE_FAIL, (data: MoveArriveAction) => {
        runCanvas.moveArriveFail(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_SCANS_NODE, (data: HackerScansNodeAction) => {
        runCanvas.hackerScansNode(data)
    })

    webSocketConnection.addAction(SERVER_REDIRECT_HACK_ICE, (data: RedirectHackIce) => {
        const url = avEncodedUrl(`ice/siteHack/${data.iceId}`)
        window.open(url)
    })

    webSocketConnection.addAction(SERVER_REDIRECT_CONNECT_ICE, (data: RedirectConnectIce) => {
        const url = avEncodedUrl(`app/auth/${data.layerId}`)
        window.open(url)
    })

    webSocketConnection.addAction(SERVER_NODE_HACKED, (data: NodeHacked) => {
        delayTicks(data.delay, () => {
            runCanvas.nodeHacked(data.nodeId)
        })
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

    webSocketConnection.addAction(SERVER_PATROLLER_LOCKS_HACKER, (data: ActionPatrollerCatchesHacker) => {
        runCanvas.patrollerLocksHacker(data)
    })

    webSocketConnection.addAction(SERVER_PATROLLER_REMOVE, (data: RemovePatrollerAction) => {
        runCanvas.removePatroller(data.patrollerId)
    })

    webSocketConnection.addAction(SERVER_REDIRECT_CONNECT_APP, (data: RedirectConnectApp) => {
        const url = avEncodedUrl(`app/${data.type}/${data.layerId}`)
        window.open(url)
    })
}