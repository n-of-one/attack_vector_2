import React from "react";
import {glyphiconFromType} from "../enums/LayerTypes";


interface Props {
    type?: string,
    name?: string,
    size?: string,
    display?: string,
    color?: string
}

export const Glyphicon = (props: Props) => {
    const className = determineClassName(props);
    const size = (props.size) ? props.size : "14px";
    const display = (props.display) ? props.display : "inherit";

    return (<span className={className} style={{"fontSize": size, display: display, zIndex: "100", color: props.color }}/>);
};

const determineClassName = ({type, name}: {type?: string, name?: string}) => {
    if (name) {
        return "glyphicon " + name;
    }
    return "glyphicon " + glyphiconFromType(type!);
};
