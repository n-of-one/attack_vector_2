import {Store} from "redux"
import {webSocketConnection} from "../../common/server/WebSocketConnection"
import {initGenericServerActions} from "./GenericServerActionProcessor"
import {HACKER_HOME, NAVIGATE_PAGE} from "../../common/menu/pageReducer"
import {terminalManager} from "../../common/terminal/TerminalManager"
import {NodeScanType, runCanvas} from "../run/component/RunCanvas"
import {
    SERVER_DISCOVER_NODES,
    SERVER_HACKER_ENTER_SCAN,
    SERVER_HACKER_LEAVE_SCAN,
    SERVER_PROBE_LAUNCH
} from "../run/model/ScanActions"
import {Scan, UpdateNodeStatusAction} from "../run/reducer/ScanReducer"
import {Site} from "../run/reducer/SiteReducer"
import {HackerPresence} from "../run/reducer/HackersReducer"
import {Timings} from "../../common/model/Ticks"
import {SERVER_ICE_PASSWORD_UPDATE, SERVER_ENTER_ICE_PASSWORD} from "../../ice/password/container/PasswordIceReducer"
import {passwordIceManager, PasswordIceStateUpdate} from "../../ice/password/container/PasswordIceManager"
import {delayTicks} from "../../common/util/Util"
import {
    SERVER_FLASH_PATROLLER,
    SERVER_PATROLLER_LOCKS_HACKER,
    SERVER_PATROLLER_MOVE, SERVER_PATROLLER_REMOVE,
    SERVER_START_TRACING_PATROLLER
} from "../run/coundown/CountdownReducer"
import {enterScan} from "../home/HackerHome"
import {SERVER_TERMINAL_RECEIVE, TERMINAL_LOCK} from "../../common/terminal/TerminalReducer"
import {Schedule} from "../../common/util/Schedule"
import {NodeScanStatus} from "../../common/enums/NodeStatus";

export const SERVER_HACKER_START_ATTACK = "SERVER_HACKER_START_ATTACK"
export const SERVER_HACKER_MOVE_START = "SERVER_HACKER_MOVE_START"
export const SERVER_HACKER_MOVE_ARRIVE = "SERVER_HACKER_MOVE_ARRIVE"
export const SERVER_HACKER_MOVE_ARRIVE_FAIL = "SERVER_HACKER_MOVE_ARRIVE_FAIL"
export const SERVER_HACKER_SCANS_NODE = "SERVER_HACKER_SCANS_NODE"
export const SERVER_LAYER_HACKED = "SERVER_LAYER_HACKED"
export const SERVER_NODE_HACKED = "SERVER_NODE_HACKED"

export const SERVER_HACKER_DC = "SERVER_HACKER_DC"
export const SERVER_SCAN_FULL = "SERVER_SCAN_FULL"
export const SERVER_UPDATE_NODE_STATUS = "SERVER_UPDATE_NODE_STATUS"
export const SERVER_SITE_DISCOVERED = "SERVER_SITE_DISCOVERED"

export const SERVER_REDIRECT_HACK_ICE = "SERVER_REDIRECT_HACK_ICE"

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
    redirectId: string
}

export const initRunServerActions = (store: Store) => {

    const dispatch = store.dispatch
    let hackerSchedule: Schedule | null = null

    initGenericServerActions()

    const echo = (time: number, message: string) => {
        hackerSchedule!.run(time, () => {
            dispatch({type: SERVER_TERMINAL_RECEIVE, data: {terminalId: "main", lines: [message]}})
        })
    }

    webSocketConnection.addAction(SERVER_HACKER_DC, (_: any) => {
        const currentPage = store.getState().currentPage

        webSocketConnection.unsubscribe()
        terminalManager.stop()
        runCanvas.stop()

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
        runCanvas.updateNodeStatus(data.nodeId, data.newStatus)
    })

    webSocketConnection.addAction(SERVER_DISCOVER_NODES, ({nodeStatusById, connectionIds}: ProbeResultConnections) => {
        runCanvas.discoverNodes(nodeStatusById, connectionIds)
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
        runCanvas.startAttack(data.userId, data.quick, data.timings)
        if (data.userId === store.getState().userId) {
            dispatch({type: TERMINAL_LOCK, terminalId: "main"})
            const random = (max: number) => Math.floor(Math.random() * max)
            const personaId = "" + random(10) + random(10) + random(10) + random(10) + random(10) + random(10) + '-' +
                random(10) + random(10) + random(10) + random(10) + '/' + random(10)

            hackerSchedule = new Schedule(null)

            if (data.quick) {
                echo(0, "[info]Persona established, hack started.")
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
            }

        }
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
        window.open(`/ice/${data.redirectId}`, data.redirectId)
    })

    webSocketConnection.addAction(SERVER_ENTER_ICE_PASSWORD, (_: any) => {
        passwordIceManager.enter()
    })

    webSocketConnection.addAction(SERVER_ICE_PASSWORD_UPDATE, (data: PasswordIceStateUpdate) => {
        passwordIceManager.serverPasswordIceUpdate(data)
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

}