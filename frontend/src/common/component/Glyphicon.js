import React from "react";
import {glyphiconFromType} from "../../editor/component/service/ServiceTypes";

const determineClassName = ({type, name}) => {
    if (name) {
        return "glyphicon " + name;
    }
    return "glyphicon " + glyphiconFromType(type);
};

export default (props) => {
    const className = determineClassName(props);
    const size = (props.size) ? props.size : "14px";
    const display = (props.display) ? props.display : "block";

    return (<span className={className} style={{"fontSize": size, display: display }}/>);
};