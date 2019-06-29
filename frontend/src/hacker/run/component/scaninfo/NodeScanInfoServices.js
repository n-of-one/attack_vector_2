import React from 'react';
import ServiceInfo from "./service/ServiceInfo";


function findProtectedLayer(services) {
    for (let i = services.length - 1; i >= 0; i--) {
        const service = services[i];
        if (service.ice && service.hacked === false) {
            return i;
        }
    }
    return -1;
}

export default ({node}) => {

    const services = node.services;
    const rendered = [];
    const protectedLayer = findProtectedLayer(services);

    rendered.push(<span key="_0">Layer Service<br/></span>);

    for (let i = 0; i < services.length; i++) {
        const service = services[i];
        if (i === protectedLayer) {
            rendered.push(<span key="_1"><br/>--- Services above are protected by ice --- <br/><br/></span>)
        }
        rendered.push(<ServiceInfo service={service} key={i}/>)
    }
    return rendered;
};
