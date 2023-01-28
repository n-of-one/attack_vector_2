import React from 'react';
import { useSelector} from "react-redux";
import PasswordIceHome from "./password/PasswordIceHome";
import {ICE_PASSWORD, ICE_TANGLE} from "../../../common/enums/LayerTypes";
import TangleIceHome from "./tangle/TangleIceHome";
import {HackerState} from "../../HackerRootReducer";


export const IceGame = () => {
    const currentIce = useSelector((state: HackerState) => state.run.ice.currentIce)
    switch (currentIce.type) {
        case null:
            return <></>;
        case ICE_PASSWORD:
            return <PasswordIceHome/>
        case ICE_TANGLE:
            return <TangleIceHome/>
        default:
            return <h1 className="text">IceGame.js: Unsupported ICE: {currentIce.type}</h1>
    }
}
