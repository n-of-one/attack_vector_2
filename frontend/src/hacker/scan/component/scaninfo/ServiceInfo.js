import React from 'react';
import ScanInfoOs from "./ScanInfoOs";
import {ICE_PASSWORD, OS} from "../../../../editor/component/service/ServiceTypes";
import ScanInfoPasswordIce from "./ScanInfoPasswordIce";


const renderService = (service) =>{
    switch (service.type) {
        case OS:
            return <ScanInfoOs service={service}/>;
        case ICE_PASSWORD:
            return <ScanInfoPasswordIce service={service} />;
        default:
            return <><span className="text-danger">Unknown service</span></>;
    }
};

export default ({service}) => {
    return (
        <>
            {service.layer}&nbsp;&nbsp;{service.data['name']}{renderService(service)}<br/>
        </>
    );
};
