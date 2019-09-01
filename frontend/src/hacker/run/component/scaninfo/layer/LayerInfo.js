import React from 'react';
import ScanInfoOs from "./ScanInfoOs";
import {ICE_PASSWORD, OS, TEXT} from "../../../../../common/enums/LayerTypes";
import ScanInfoPasswordIce from "./ScanInfoPasswordIce";
import ScanInfoText from "./ScanInfoText";
import Pad from "../../../../../common/component/Pad";


const renderLayer = (layer) =>{
    switch (layer.type) {
        case OS:
            return <ScanInfoOs layer={layer}/>;
        case ICE_PASSWORD:
            return <ScanInfoPasswordIce layer={layer} />;
        case TEXT:
            return <ScanInfoText layer={layer} />;
        default:
            return <><span className="text-danger">Unknown layer</span></>;
    }
};

export default ({layer}) => {
    return (
        <>
            <Pad p="3" n={layer.layer}/>
            <span className="text-primary">{layer.layer}</span>
            <Pad p="3" />{layer.name}{renderLayer(layer)}<br/>
        </>
    );
};
