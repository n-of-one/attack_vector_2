import React from 'react';
import ScanInfoOs from "./ScanInfoOs";
import {ICE_PASSWORD, OS, TEXT} from "../../../../../editor/component/middle/right/service/ServiceTypes";
import ScanInfoPasswordIce from "./ScanInfoPasswordIce";
import ScanInfoText from "./ScanInfoText";
import Pad from "../../../../../common/component/Pad";


const renderService = (service) =>{
    switch (service.type) {
        case OS:
            return <ScanInfoOs service={service}/>;
        case ICE_PASSWORD:
            return <ScanInfoPasswordIce service={service} />;
        case TEXT:
            return <ScanInfoText service={service} />;
        default:
            return <><span className="text-danger">Unknown service</span></>;
    }
};

export default ({service}) => {
    return (
        <>
            <Pad p="3" n={service.layer}/>
            <span className="text-primary">{service.layer}</span>
            <Pad p="3" />{service.name}{renderService(service)}<br/>
        </>
    );
};
