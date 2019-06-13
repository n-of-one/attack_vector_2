import React from 'react';
import ScanInfoOs from "./ScanInfoOs";
import {ICE_PASSWORD, OS, TEXT} from "../../../../editor/component/service/ServiceTypes";
import ScanInfoPasswordIce from "./ScanInfoPasswordIce";
import ScanInfoText from "./ScanInfoText";


const renderService = (service) =>{
    switch (service.type) {
        case OS:
            return <ScanInfoOs service={service}/>;
        case ICE_PASSWORD:
            return <ScanInfoPasswordIce service={service} />;
        case TEXT:
            return <ScanInfoText service={service} />
        default:
            return <><span className="text-danger">Unknown service</span></>;
    }
};

export default ({service}) => {
    return (
        <>
            {service.layer}&nbsp;&nbsp;{service.name}{renderService(service)}<br/>
        </>
    );
};
