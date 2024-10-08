import {AnyAction, Store} from "redux"
import {webSocketConnection} from "../common/server/WebSocketConnection"
import {SERVER_OPEN_EDITOR} from "../common/server/GenericServerActionProcessor"
import {runCanvas} from "./run/component/RunCanvas"
import {Scan, UpdateNodeStatusAction} from "./run/reducer/ScanReducer"
import {Site} from "./run/reducer/SiteReducer"
import {HackerPresence} from "./run/reducer/HackersReducer"
import {Timings} from "../common/model/Ticks"
import {delayTicks} from "../common/util/Util"
import {SERVER_FLASH_PATROLLER,} from "./run/coundown/TimersReducer"
import {prepareToEnterRun} from "./home/HackerHome"
import {SERVER_TERMINAL_RECEIVE, TERMINAL_CLEAR, TERMINAL_RECEIVE} from "../common/terminal/TerminalReducer"
import {Schedule} from "../common/util/Schedule"
import {NodeScanStatus} from "../common/enums/NodeStatus";
import {MAIN_TERMINAL_ID} from "../common/terminal/ActiveTerminalIdReducer";
import {currentUser} from "../common/user/CurrentUser";
import {NAVIGATE_PAGE, RUN} from "../common/menu/pageReducer";
import {terminalManager} from "../common/terminal/TerminalManager";
import {HIDE_NODE_INFO} from "./run/reducer/InfoNodeIdReducer";
import {avEncodedUrl} from "../common/util/PathEncodeUtils";
import {NodeScanType} from "./run/model/NodeScanTypes";

export const SERVER_HACKER_START_ATTACK = "SERVER_HACKER_START_ATTACK"
export const SERVER_HACKER_MOVE_START = "SERVER_HACKER_MOVE_START"
export const SERVER_HACKER_MOVE_ARRIVE = "SERVER_HACKER_MOVE_ARRIVE"
export const SERVER_HACKER_SCANS_NODE = "SERVER_HACKER_SCANS_NODE"
export const SERVER_LAYER_HACKED = "SERVER_LAYER_HACKED"
export const SERVER_NODE_HACKED = "SERVER_NODE_HACKED"
export const SERVER_HACKER_DC = "SERVER_HACKER_DC"
export const SERVER_ENTERING_RUN = "SERVER_ENTERING_RUN"
export const SERVER_ENTERED_RUN = "SERVER_ENTERED_RUN"
export const SERVER_UPDATE_NODE_STATUS = "SERVER_UPDATE_NODE_STATUS"
export const SERVER_SITE_DISCOVERED = "SERVER_SITE_DISCOVERED"
export const SERVER_REDIRECT_HACK_ICE = "SERVER_REDIRECT_HACK_ICE"
export const SERVER_HACKER_ENTER_SITE = "SERVER_HACKER_ENTER_SITE"
export const SERVER_HACKER_LEAVE_SITE = "SERVER_HACKER_LEAVE_SITE"

export const SERVER_PROBE_LAUNCH = "SERVER_PROBE_LAUNCH"

export const SERVER_REDIRECT_CONNECT_ICE = "SERVER_REDIRECT_CONNECT_ICE"
export const SERVER_REDIRECT_CONNECT_APP = "SERVER_REDIRECT_CONNECT_APP"

export const SERVER_SITE_SHUTDOWN_START = "SERVER_SITE_SHUTDOWN_START"
export const SERVER_SITE_SHUTDOWN_FINISH = "SERVER_SITE_SHUTDOWN_FINISH"

export const SERVER_DISCOVER_NODES = "SERVER_DISCOVER_NODES"


/** Event to ignore while waiting for the scan full result */
export const IGNORELIST_WHILE_ENTERING_RUN =
    [
        SERVER_HACKER_MOVE_ARRIVE,
        SERVER_HACKER_SCANS_NODE,
        SERVER_LAYER_HACKED,
        SERVER_NODE_HACKED,
        SERVER_HACKER_DC,
        SERVER_UPDATE_NODE_STATUS,
        SERVER_SITE_DISCOVERED,
        SERVER_SITE_SHUTDOWN_START,
        SERVER_SITE_SHUTDOWN_FINISH,

        SERVER_HACKER_ENTER_SITE,
        SERVER_HACKER_LEAVE_SITE,

        SERVER_PROBE_LAUNCH,
        SERVER_UPDATE_NODE_STATUS,
        SERVER_DISCOVER_NODES,
    ]

export interface SiteAndScan {
    run: Scan,
    site: Site,
    hackers: HackerPresence[],
}

export interface NodeStatusById {
    [key: string]: NodeScanStatus
}
export interface ProbeResultConnections {
    nodeStatusById: NodeStatusById,
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

export interface HackerScansNodeAction {
    userId: string,
    nodeId: string,
    timings: Timings
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
    timings: Timings
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

interface RunAndSite {
    runId: string,
    siteId: string
}

export const initRunServerActions = (store: Store) => {

    const dispatch = store.dispatch
    let hackerSchedule: Schedule | null = null

    const echo = (time: number, message: string) => {
        hackerSchedule!.run(time, () => {
            dispatch({type: SERVER_TERMINAL_RECEIVE, data: {terminalId: MAIN_TERMINAL_ID, lines: [message]}})
        })
    }

    webSocketConnection.addAction(SERVER_OPEN_EDITOR, (data: { id: string }) => {
        window.open("/edit/" + data.id, data.id)
    })

    webSocketConnection.addAction(SERVER_SITE_DISCOVERED, ({runId, siteId}: { runId: string, siteId: string }) => {
        prepareToEnterRun(runId)
    })

    webSocketConnection.addAction(SERVER_ENTERING_RUN, (data: RunAndSite) => {

        webSocketConnection.waitFor(SERVER_ENTERED_RUN, IGNORELIST_WHILE_ENTERING_RUN)
        webSocketConnection.subscribeForRun(data.runId, data.siteId)
        dispatch({type: HIDE_NODE_INFO})
        dispatch({type: TERMINAL_CLEAR, terminalId: "main"})
        dispatch({type: TERMINAL_RECEIVE, terminalId: "main", data: "[b]🜁 Verdant OS 🜃"})
        dispatch({type: TERMINAL_RECEIVE, terminalId: "main", data: ""})
        const currentPage = store.getState().currentPage
        dispatch({type: NAVIGATE_PAGE, to: RUN, from: currentPage})
        terminalManager.start()

        webSocketConnection.send("/run/enterRun", data.runId)
    })

    webSocketConnection.addAction(SERVER_ENTERED_RUN, (data: SiteAndScan) => {
        runCanvas.enterSite(data)
    })

    webSocketConnection.addAction(SERVER_UPDATE_NODE_STATUS, (data: UpdateNodeStatusAction) => {
        runCanvas.updateNodeStatus(data.nodeId, data.newStatus)
    })

    webSocketConnection.addAction(SERVER_DISCOVER_NODES, ({nodeStatusById}: ProbeResultConnections) => {
        runCanvas.discoverNodes(nodeStatusById)
    })

    webSocketConnection.addAction(SERVER_PROBE_LAUNCH, (data: ProbeAction) => {
        runCanvas.launchProbe(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_ENTER_SITE, (data: HackerPresence) => {
        runCanvas.hackerEnter(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_LEAVE_SITE, (data: any) => {
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
                echo(20, "Entering node")
                echo(0, "Connection established.")
                echo(0, "")
            }

        }
    })

    interface DisconnectAction { userId: string }
    webSocketConnection.addAction(SERVER_HACKER_DC, (action: DisconnectAction ) => {
        runCanvas.disconnect(action.userId)
    })

    webSocketConnection.addAction(SERVER_SITE_SHUTDOWN_START, (action: AnyAction ) => {
        runCanvas.siteShutdownStart()
    })

    webSocketConnection.addAction(SERVER_SITE_SHUTDOWN_FINISH, (action: AnyAction ) => {
        runCanvas.siteShutdownFinish()
    })

    webSocketConnection.addAction(SERVER_HACKER_MOVE_START, (data: MoveStartAction) => {
        runCanvas.moveStart(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_MOVE_ARRIVE, (data: MoveArriveAction) => {
        runCanvas.moveArrive(data)
    })

    webSocketConnection.addAction(SERVER_HACKER_SCANS_NODE, (data: HackerScansNodeAction) => {
        runCanvas.hackerScansNode(data)
    })

    webSocketConnection.addAction(SERVER_REDIRECT_HACK_ICE, (data: RedirectHackIce) => {
        const url = avEncodedUrl(`ice/siteHack/${data.iceId}`)
        window.open(url, "app")
    })

    webSocketConnection.addAction(SERVER_REDIRECT_CONNECT_ICE, (data: RedirectConnectIce) => {
        const url = avEncodedUrl(`app/auth/${data.layerId}`)
        window.open(url, "app")
    })

    webSocketConnection.addAction(SERVER_NODE_HACKED, (data: NodeHacked) => {
        delayTicks(data.delay, () => {
            runCanvas.nodeHacked(data.nodeId)
        })
    })

    webSocketConnection.addAction(SERVER_FLASH_PATROLLER, (data: FlashPatrollerAction) => {
        runCanvas.flashTracingPatroller(data.nodeId)
    })

    webSocketConnection.addAction(SERVER_REDIRECT_CONNECT_APP, (data: RedirectConnectApp) => {
        const url = avEncodedUrl(`app/${data.type}/${data.layerId}`)
        window.open(url, "app")
    })
}
