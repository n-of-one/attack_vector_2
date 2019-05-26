import React from 'react';
import {connect} from "react-redux";
import ServiceText from "./ServiceText";
import {findElementById} from "../../../../common/Immutable";
import ServiceName from "../ServiceName";
import ServiceLayer from "../ServiceLayer";
import ServiceField from "../ServiceField";

/* eslint jsx-a11y/alt-text: 0*/


const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch
    }
};
let mapStateToProps = (state) => {

    return {
        nodes: state.nodes,
        currentNodeId: state.currentNodeId,
        currentServiceId: state.currentServiceId,

    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({nodes, currentNodeId, currentServiceId, dispatch}) => {
        if (!currentNodeId) {
            return <div/>
        }

        const currentNode = findElementById(nodes, currentNodeId);
        const currentService = findElementById(currentNode.services, currentServiceId);

        const text = new ServiceText(currentService, currentNode, dispatch);

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => currentServiceId + ":" + param;

        if (currentNode) {
            return (
                <div className="tab-content" id="node-services-tab-content ">
                    <div className="tab-pane active">
                        <ServiceName name="Text"/>
                        <ServiceLayer layer={0} layourCount={1}/>
                        <ServiceField key={key("id")} size="small" name="Node id" value={text.id} readOnly={true} />
                        <ServiceField key={key("na")} size="small" name="Name" value={text.name} placeholder="00" save={value => text.saveName(value)}/>
                        <ServiceField key={key("ha")} size="large" name="Hacked text" value={text.text} placeholder="Data found: ..." save={value => text.saveText(value)} />
                        <ServiceField key={key("no")} size="large" name="Gm Note" value={text.note} placeholder="Only GM can read" save={value => text.saveNote(value)}/>
                    </div>
                </div>
            );
        }
        else {
            return (<div />);
        }
    });
