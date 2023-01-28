import React from 'react';
import {useSelector} from "react-redux";
import {HackerState} from "../../HackerRootReducer";

/* eslint jsx-a11y/alt-text: 0*/

export const ScanProbeImages = () => {

    const theme = useSelector((state: HackerState) => state.theme)
    const root = "/img/" + theme + "/actors/scan_probes/"
    const pathStart = root + "probe-"

    const renderImage = (nr: number) => {
        return <img src={pathStart + nr + ".png"} height="80" width="80" id={"PROBE_" + nr} key={nr}/>
    }

    return (
        <span>
            { [1,2,3,4,5,6,7,8,9,10].map( (nr) => renderImage(nr)) }
        </span>
    )
}
