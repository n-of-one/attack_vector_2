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

export default connect(mapStateToProps, mapDispatchToProps)(
    ({theme, type, onLoad}) => {

        const root = "/img/" + theme + "/nodes/scan/";
        const dirAndName = type.dir + "/" + type.name + ".png";

        const pathProbed = root + "probed/" + dirAndName;
        const pathFree = root + "free/" + dirAndName;
        const pathProtected = root + "protected/" + dirAndName;
        const pathHacked = root + "hacked/" + dirAndName;

        return (
            <span>
                <img src={pathProbed} height="80" width="80" id={type.name + "_PROBED"} onLoad={onLoad}/>
                <img src={pathFree} height="80" width="80" id={type.name + "_FREE"} onLoad={onLoad}/>
                <img src={pathProtected} height="80" width="80" id={type.name + "_PROTECTED"} onLoad={onLoad}/>
                <img src={pathHacked} height="80" width="80" id={type.name + "_HACKED"} onLoad={onLoad}/>
            </span>
        )
    });