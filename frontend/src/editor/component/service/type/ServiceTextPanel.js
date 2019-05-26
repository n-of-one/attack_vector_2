import React from 'react';
import {connect} from "react-redux";
import ServiceText from "./ServiceText";
import ServiceField from "../ServiceField";
import ServicePanel from "./ServicePanel";

/* eslint jsx-a11y/alt-text: 0*/


const mapDispatchToProps = (dispatch) => {
    return { dispatch: dispatch }
};

let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, service, dispatch}) => {

        const text = new ServiceText(service, node, dispatch);

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => service.id + ":" + param;

        if (node) {
            return (
                <ServicePanel name="Text">
                    <ServiceField key={key("na")} size="small" name="Name" value={text.name} placeholder="00" save={value => text.saveName(value)}/>
                    <ServiceField key={key("ha")} size="large" name="Hacked text" value={text.text} placeholder="Data found: ..." save={value => text.saveText(value)} />
                    <ServiceField key={key("no")} size="large" name="Gm Note" value={text.note} placeholder="Players will never see this." save={value => text.saveNote(value)}/>
                </ServicePanel>
            );
        }
        else {
            return (<div />);
        }


    });
