import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {jigsawRootReducer, JigsawRootState} from "./reducer/JigsawRootReducer";
import {initGenericServerActions} from "../../../common/server/GenericServerActionProcessor";
import {terminalManager} from "../../../common/terminal/TerminalManager";
import {ice} from "../../StandaloneGlobals";
import {JigsawContainer} from "./component/JigsawContainer";
import {jigsawIceManager} from "./JigsawIceManager";
import {initJigsawServerActions} from "./JigsawServerActionProcessor";
import {Page} from "../../../common/menu/pageReducer";
import {initGenericAppActions} from "../../../common/server/GenericAppActionProcessor";
import {IceStrength} from "../../../common/model/IceStrength";
import {generatePieceConfigs} from "./component/JigsawShapes";
import {CANVAS_HEIGHT, CANVAS_WIDTH} from "./canvas/JigsawCanvas";


interface Props {
    iceId: string
    externalHack: boolean,
}

interface PuzzlePieces {
    columns: number,
    rows: number,
}


const ICE_DIFFICULTY: Record<IceStrength, PuzzlePieces> = {
    [IceStrength.UNKNOWN]: {columns: 5, rows: 3},
    [IceStrength.VERY_WEAK]: {columns: 5, rows: 3},
    [IceStrength.WEAK]: {columns: 7, rows: 4},
    [IceStrength.AVERAGE]: {columns: 10, rows: 6},
    [IceStrength.STRONG]: {columns: 13, rows: 8},
    [IceStrength.VERY_STRONG]: {columns: 17, rows: 10},
    [IceStrength.ONYX]: {columns: 21, rows: 12},
}

export class JigsawRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        document.body.style.backgroundColor = "#333"
        ice.id = props.iceId
        const preLoadedState = {currentPage: Page.ICE}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: jigsawRootReducer as Reducer<JigsawRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/ice/jigsaw/enter", {iceId: ice.id})
        });

        jigsawIceManager.init(this.store, props.externalHack);
        terminalManager.init(this.store)
        initGenericServerActions({fontSize: 12})
        initGenericAppActions()
        initJigsawServerActions()

        // DEV: simulate server enter response with hardcoded data

        const {columns, rows} = ICE_DIFFICULTY[IceStrength.ONYX]
        setTimeout(() => {
            jigsawIceManager.enter({
                hacked: false,
                strength: IceStrength.AVERAGE,
                // imageSrc: "/img/frontier/ice/jigsaw/tylijura-ai-generated-9396797_1920.png",
                // imageSrc: "/img/frontier/ice/jigsaw/chatgpt-angel-2.png",
                imageSrc: "/img/frontier/ice/jigsaw/anubis.png",
                // imageSrc: "/img/frontier/ice/jigsaw/pexels-marcin-jozwiak-199600-13835514.jpg",
                // imageSrc: "/img/frontier/ice/jigsaw/barbaraalane-fractal-2035686.jpg",
                columns: columns,
                rows: rows,
                pieces: generatePieceConfigs(columns, rows, CANVAS_WIDTH, CANVAS_HEIGHT),
                groups: [
                    // Array.from({length: devColumns * devRows}, (_, i) =>
                    //     [i % devColumns, Math.floor(i / devColumns)] as [number, number]
                    // ),
                ],
            })
        }, 200)
    }

    render() {
        return (
            <Provider store={this.store}>
                <JigsawContainer/>
            </Provider>
        )
    }
}
