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

interface Props {
    iceId: string
    externalHack: boolean,
}

export class JigsawRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
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
        // const devColumns = 5
        // const devRows = 3
        const devColumns = 7
        const devRows = 4
        setTimeout(() => {
            jigsawIceManager.enter({
                hacked: false,
                strength: IceStrength.AVERAGE,
                // imageSrc: "/img/frontier/ice/jigsaw/tylijura-ai-generated-9396797_1920.png",
                imageSrc: "/img/frontier/ice/jigsaw/barbaraalane-fractal-2035686.jpg",
                columns: devColumns,
                rows: devRows,
                pieces: generatePieceConfigs(devColumns, devRows, 1576, 828),
                groups: [
                    [
                        [0, 0], [1, 0], [2, 0], [3, 0], [4, 0], [5, 0], [6, 0],
                        [0, 1], [1, 1], [2, 1], [3, 1], [4, 1], [5, 1], [6, 1],
                        // [0, 2], [1, 2], [2, 2], [3, 2], [4, 2], [5, 2], [6, 2],
                        // [0, 3], [1, 3], [2, 3], [3, 3], [4, 3], [5, 3], [6, 3]
                    ],
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
