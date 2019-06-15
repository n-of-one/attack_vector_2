import React from 'react';
import {connect} from "react-redux";
import scanCanvas from "./ScanCanvas";
import {findElementById} from "../../../common/Immutable";
import ServiceInfo from "./scaninfo/ServiceInfo";

const mapDispatchToProps = (dispatch) => {
    return {
        dismiss: () => scanCanvas.unselect(),
    }
};

let mapStateToProps = (state) => {

    if (state.scan.infoNodeId) {
        const node = findElementById(state.scan.site.nodes, state.scan.infoNodeId);
        return {
            node: node,
        };
    }

    return {
        node: null,
    };
};

function findProtectedLayer(services) {
    for (let i = services.length-1; i >= 0 ; i--) {
        const service = services[i];
        if (service.ice && service.hacked === false) {
            return i;
        }
    }
    return -1;
}

const renderServices = (services) => {
    const rendered = [];
    const protectedLayer = findProtectedLayer(services);
    for (let i = 0; i < services.length; i++) {
        const service = services[i];
        if (i === protectedLayer) {
            rendered.push(<><br/>--- Services above are protected by ice --- <br/><br/></>)
        }
        rendered.push (<ServiceInfo service={service} key={i}/>)
    }
    return rendered;
};


export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, dismiss}) => {

        if (!node) {
            return <div className="row backgroundLight">&nbsp;</div>
        }

        return (
            <>
                <div className="row backgroundLight">&nbsp;</div>
                <div className="row nodeInfo text" onClick={() => dismiss()}>
                    <div className="col-lg-12">
                        <br />
                        Scan info on <span className="networkId">{node.networkId}</span><br />
                        <br />
                        {renderServices(node.services)}
                    </div>
                </div>
            </>
        );
    });
