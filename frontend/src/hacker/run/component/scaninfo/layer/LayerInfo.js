import React from 'react';
import ScanInfoOs from "./ScanInfoOs";
import {ICE_PASSWORD, ICE_TANGLE, OS, TEXT, TIMER_TRIGGER} from "../../../../../common/enums/LayerTypes";
import ScanInfoText from "./ScanInfoText";
import Pad from "../../../../../common/component/Pad";
import ScanInfoIce from "./ScanInfoIce";
import ScanInfoTimerTrigger from "./ScanInfoTimerTrigger";


const renderLayer = (layer) => {
    switch (layer.type) {
        case OS:
            return <ScanInfoOs layer={layer}/>;
        case ICE_PASSWORD:
            return <ScanInfoIce layer={layer} iceDescription="static password"/>;
        case ICE_TANGLE:
            return <ScanInfoIce layer={layer} iceDescription="tangle"/>;
        case TEXT:
            return <ScanInfoText layer={layer}/>;
        case TIMER_TRIGGER:
            return <ScanInfoTimerTrigger layer={layer}/>;
        default:
            return <><span className="text-danger">Unknown layer</span></>;
    }
};

const LayerInfo = ({layer}) => {
    return (
        <>
            <Pad p="3" n={layer.level}/>
            <span className="text-primary">{layer.level}</span>
            <Pad p="3"/>{layer.name}{renderLayer(layer)}<br/>
        </>
    );
};

export default LayerInfo;