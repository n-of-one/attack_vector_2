import {AnyAction, Store} from "redux"
import {webSocketConnection} from "../common/server/WebSocketConnection"
import {SERVER_OPEN_EDITOR} from "../common/server/GenericServerActionProcessor"
import {runCanvas} from "./run/component/RunCanvas"
import {Scan, UpdateNodeStatusAction} from "./run/reducer/ScanReducer"
import {Site} from "./run/reducer/SiteReducer"
import {HackerPresence} from "./run/reducer/HackerPresencesReducer"
import {Timings} from "../common/model/Ticks"
import {delayTicks} from "../common/util/Util"
import {SERVER_FLASH_PATROLLER,} from "./run/timer/TimersReducer"
import {prepareToEnterRun} from "./home/HackerHome"
import {SERVER_TERMINAL_RECEIVE, TERMINAL_CLEAR, TERMINAL_RECEIVE} from "../common/terminal/TerminalReducer"
import {Schedule} from "../common/util/Schedule"
import {NodeScanStatus} from "../common/enums/NodeStatus";
import {MAIN_TERMINAL_ID} from "../common/terminal/ActiveTerminalIdReducer";
import {currentUser} from "../common/user/CurrentUser";
import {NAVIGATE_PAGE, Page} from "../common/menu/pageReducer";
import {terminalManager} from "../common/terminal/TerminalManager";
import {HIDE_NODE_INFO} from "./run/reducer/InfoNodeIdReducer";
import {avEncodedUrl} from "../common/util/PathEncodeUtils";
import {NodeScanType} from "./run/model/NodeScanTypes";
import {ConnectionStatusById} from "../editor/reducer/ConnectionsReducer";
import {updateTerminalState} from "../common/menu/MenuItem";

export const SERVER_HACKER_START_ATTACK = "SERVER_HACKER_START_ATTACK"
export const SERVER_RETURN_HOME = "SERVER_RETURN_HOME"
export const SERVER_HACKER_MOVE_START = "SERVER_HACKER_MOVE_START"
export const SERVER_HACKER_MOVE_ARRIVE = "SERVER_HACKER_MOVE_ARRIVE"
export const SERVER_HACKER_SCANS_NODE = "SERVER_HACKER_SCANS_NODE"
export const SERVER_LAYER_HACKED = "SERVER_LAYER_HACKED"
export const SERVER_LAYER_CHANGED = "SERVER_LAYER_CHANGED"
export const SERVER_HACKER_DC = "SERVER_HACKER_DC"
export const SERVER_ENTERING_RUN = "SERVER_ENTERING_RUN"
export const SERVER_ENTERED_RUN = "SERVER_ENTERED_RUN"
export const SERVER_UPDATE_NODE_STATUS = "SERVER_UPDATE_NODE_STATUS"
export const SERVER_SITE_DISCOVERED = "SERVER_SITE_DISCOVERED"
export const SERVER_REDIRECT_HACK_ICE = "SERVER_REDIRECT_HACK_ICE"
export const SERVER_HACKER_ENTER_SITE = "SERVER_HACKER_ENTER_SITE"
export const SERVER_HACKER_LEAVE_SITE = "SERVER_HACKER_LEAVE_SITE"
export const SERVER_HACKER_SWITCH_SITE = "SERVER_HACKER_SWITCH_SITE"

export const SERVER_PROBE_LAUNCH = "SERVER_PROBE_LAUNCH"

export const SERVER_REDIRECT_CONNECT_ICE = "SERVER_REDIRECT_CONNECT_ICE"
export const SERVER_REDIRECT_CONNECT_APP = "SERVER_REDIRECT_CONNECT_APP"

export const SERVER_SITE_SHUTDOWN_START = "SERVER_SITE_SHUTDOWN_START"
export const SERVER_SITE_SHUTDOWN_FINISH = "SERVER_SITE_SHUTDOWN_FINISH"

export const SERVER_DISCOVER_NODES = "SERVER_DISCOVER_NODES"
export const SERVER_CONNECTIONS_UPDATED = "SERVER_CONNECTIONS_UPDATED"


/** Event to ignore while waiting for the scan full result */
export const IGNORELIST_WHILE_ENTERING_RUN =
    [
        SERVER_HACKER_MOVE_ARRIVE,
        SERVER_HACKER_SCANS_NODE,
        SERVER_LAYER_HACKED,
        SERVER_LAYER_CHANGED,
        SERVER_HACKER_DC,
        SERVER_UPDATE_NODE_STATUS,
        SERVER_SITE_DISCOVERED,
        SERVER_SITE_SHUTDOWN_START,
        SERVER_SITE_SHUTDOWN_FINISH,

        SERVER_HACKER_ENTER_SITE,
        SERVER_HACKER_LEAVE_SITE,
        SERVER_HACKER_SWITCH_SITE,

        SERVER_PROBE_LAUNCH,
        SERVER_UPDATE_NODE_STATUS,
        SERVER_DISCOVER_NODES,
        SERVER_CONNECTIONS_UPDATED,
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

interface UplinkConnectInfo {
    sourceSiteName: string,
    targetSiteName: string,
}

interface StartAttack {
    userId: string,
    nodeId: string, // the ingress node
    quick: boolean,
    timings: Timings,
    uplink: UplinkConnectInfo | null,
}

export interface MoveStartAction {
    userId: string,
    nodeId: string,
    bypassingIce: boolean,
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

interface EnterRunData {
    runId: string,
    siteId: string,
    viaUplink?: boolean, // switching runs via an uplink: keep the terminal, enter via /run/enterSiteFromUplink
    uplinkReturn?: boolean, // returning from a site entered via an uplink
}

const UPLINK_SWITCH_PAUSE_TICKS = 30 // longer than UPLINK_SITE_FADE_OUT_TICKS in RunCanvas

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

    webSocketConnection.addAction(SERVER_ENTERING_RUN, (data: EnterRunData) => {

        runCanvas.leaveRunIfActive()
        webSocketConnection.waitFor(SERVER_ENTERED_RUN, IGNORELIST_WHILE_ENTERING_RUN)
        webSocketConnection.subscribeForRun(data.runId, data.siteId)
        dispatch({type: HIDE_NODE_INFO})
        if (!data.viaUplink) {
            dispatch({type: TERMINAL_CLEAR, terminalId: "main"})
            dispatch({type: TERMINAL_RECEIVE, terminalId: "main", data: "[b]🜁 Verdant OS 🜃"})
            dispatch({type: TERMINAL_RECEIVE, terminalId: "main", data: ""})
        }
        const currentPage = store.getState().currentPage
        dispatch({type: NAVIGATE_PAGE, to: Page.RUN, from: currentPage})
        terminalManager.start()

        if (data.viaUplink) {
            // the site that was left fades out, after a pause the next site appears
            runCanvas.hideYourHackerOnEnter = !!data.uplinkReturn // fades in at the uplink node when the session is restored
            delayTicks(UPLINK_SWITCH_PAUSE_TICKS, () => webSocketConnection.send("/run/enterSiteFromUplink", data.runId))
        } else {
            webSocketConnection.send("/run/enterRun", data.runId)
        }
    })

    webSocketConnection.addAction(SERVER_ENTERED_RUN, (data: SiteAndScan) => {
        runCanvas.enterSite(data)
    })

    webSocketConnection.addAction(SERVER_UPDATE_NODE_STATUS, (data: UpdateNodeStatusAction) => {
        runCanvas.updateNodeStatus(data.nodeId, data.newStatus, data.nodeHacked)
    })

    webSocketConnection.addAction(SERVER_DISCOVER_NODES, ({nodeStatusById}: ProbeResultConnections) => {
        runCanvas.discoverNodes(nodeStatusById)
    })

    webSocketConnection.addAction(SERVER_CONNECTIONS_UPDATED, ({connectionStatusById}: { connectionStatusById: ConnectionStatusById }) => {
        runCanvas.updateConnectionStatuses(connectionStatusById)
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

    webSocketConnection.addAction(SERVER_HACKER_SWITCH_SITE, (data: any) => {
        runCanvas.hackerSwitchSite(data.userId)
    })

    webSocketConnection.addAction(SERVER_HACKER_START_ATTACK, (data: StartAttack) => {
        if (data.uplink) {
            startUplink(data, data.uplink)
        } else {
            startAttack(data)
        }
    })

    const startAttack = (data: StartAttack) => {
        runCanvas.startAttack(data.userId, data.nodeId, data.quick, data.timings)
        if (data.userId === currentUser.id) {
            // dispatch({type: TERMINAL_LOCK, terminalId: MAIN_TERMINAL_ID})
            const random = (max: number) => Math.floor(Math.random() * max)
            const personaId = "" + random(10) + random(10) + random(10) + random(10) + random(10) + random(10) + '-' +
                random(10) + random(10) + random(10) + random(10) + '/' + random(10)

            hackerSchedule = new Schedule(null)

            // const site = (store.getState() as HackerRootState).run.site
            // const startNode = site.nodes.find(n => n.id === site.startNodeId)
            // const startNodeName = (startNode != null) ? startNode.layers[0].nodeName || "" : ""
            // const startNodeText = (startNodeName) ? `: ${startNodeName}` : ""

            const speedSlow = data.timings["slow"]
            const speedMedium = data.timings["medium"]
            const speedFast = data.timings["fast"]

            if (data.quick) {
                echo(10, "[info]Persona established, hack started.")
                echo(0, "")
            }
            else {
                echo(speedMedium, "")
                echo(speedMedium, "Persona v2.3 booting")
                echo(speedFast, "- unique ID: " + personaId)
                echo(speedFast, "- Matching fingerprint with OS deamon")
                echo(speedFast, "  - [ok]ok[/] Suppressing persona signature")
                echo(speedFast, "  - [ok]ok[/] Connection bandwidth adjusted")
                echo(speedFast, "  - [ok]ok[/] Content masked.")
                echo(speedSlow, "  - [ok]ok[/] Operating speed reduced to mimic OS deamon")
                echo(speedSlow, "  - [ok]ok[/] Network origin obfuscated ")
                echo(speedMedium, "- Persona creation [info]complete")
                echo(0, "")
                echo(speedFast, "Connection established.")
                echo(0, "")
            }

        }
    }

    const startUplink = (data: StartAttack, uplink: UplinkConnectInfo) => {
        const speedSlow = data.timings["slow"]
        const speedMedium = data.timings["medium"]
        const speedFast = data.timings["fast"]
        const carrierWait = data.timings["wait"] ?? 0

        // Let the site render and the hacker icon fade in before the connection line starts, at "Session request".
        // The line animation takes "move" ticks, the hacker display must not wait for the render wait again.
        const lineTimings = {...data.timings, main: data.timings["move"]}
        delayTicks(carrierWait, () => runCanvas.startAttack(data.userId, data.nodeId, false, lineTimings, true))
        if (data.userId !== currentUser.id) return

        hackerSchedule = new Schedule(null)

        const source = `[info]${uplink.sourceSiteName}[/]`
        const target = `[info]${uplink.targetSiteName}[/]`

        // "Establishing uplink carrier..." was sent by the server before switching to this site.
        // The lines after the wait take exactly as long as the connection line animation ("move"): 4 x medium + 2 x slow + fast.
        echo(carrierWait, "Carrier established, health [ok]100%")
        echo(speedMedium, `- Session request ${source} → ${target}`)
        echo(speedMedium, "  - [ok]ok[/] Handshake intercepted")
        echo(speedMedium, "  - [ok]ok[/] Persona v2.3 injected into session")
        echo(speedMedium, `  - [ok]ok[/] Origin rewritten to system daemon`)
        echo(speedSlow, `  - [ok]ok[/] Credentials relayed by uplink`)
        echo(speedSlow, `  - [ok]ok[/] Trust negotiation accepted by site`)
        echo(speedFast, "- Uplink session [info]established")
        echo(0, "")
        echo(0, `Routing through to ${target}.`)
        echo(0, "")
    }

    webSocketConnection.addAction(SERVER_RETURN_HOME, () => {
        runCanvas.leaveRunIfActive()
        updateTerminalState(Page.HACKER_HOME)
        dispatch({type: NAVIGATE_PAGE, to: Page.HACKER_HOME, from: store.getState().currentPage})
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

    webSocketConnection.addAction(SERVER_FLASH_PATROLLER, (data: FlashPatrollerAction) => {
        runCanvas.flashTracingPatroller(data.nodeId)
    })

    webSocketConnection.addAction(SERVER_REDIRECT_CONNECT_APP, (data: RedirectConnectApp) => {
        const url = avEncodedUrl(`app/${data.type}/${data.layerId}`)
        window.open(url, "app")
    })
}
