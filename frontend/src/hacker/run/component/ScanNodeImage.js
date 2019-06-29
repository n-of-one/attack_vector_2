import React from 'react';
import {connect} from "react-redux";

/* eslint jsx-a11y/alt-text: 0*/

const mapDispatchToProps = () => {
    return {}
};
let mapStateToProps = (state) => {
    return {
        theme: state.theme
    };
};

function renderForPhase(theme, type, onLoad, phase, prefix) {
    const root = "/img/" + theme + "/nodes/" + phase + "/";
    const dirAndName = type.dir + "/" + type.name + ".png";

    const pathDiscovered = root + "empty.png";
    const pathTypeKnown = root + "type/" + dirAndName;
    const pathFree = root + "free/" + dirAndName;
    const pathProtected = root + "protected/" + dirAndName;
    const pathHacked = root + "hacked/" + dirAndName;

    return (
        <>
            <img src={pathDiscovered} height="80" width="80" id={prefix + type.name + "_DISCOVERED"} onLoad={onLoad}/>
            <img src={pathTypeKnown} height="80" width="80" id={prefix + type.name + "_TYPE"} onLoad={onLoad}/>
            <img src={pathFree} height="80" width="80" id={prefix + type.name + "_FREE"} onLoad={onLoad}/>
            <img src={pathProtected} height="80" width="80" id={prefix + type.name + "_PROTECTED"} onLoad={onLoad}/>
            <img src={pathHacked} height="80" width="80" id={prefix + type.name + "_HACKED"} onLoad={onLoad}/>
        </>
    )
}

export default connect(mapStateToProps, mapDispatchToProps)(
    ({theme, type, onLoad}) => {
        return <span>
            {renderForPhase(theme, type, onLoad, "scan", "SCAN_")}
            {renderForPhase(theme, type, onLoad, "hack", "HACK_")}
        </span>;
    }
);