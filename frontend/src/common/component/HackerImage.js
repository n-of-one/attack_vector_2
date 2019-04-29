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
    ({theme, fileName, type, onLoad}) => {

        const root = "/img/" + theme + "/actors/hackers/";
        const dirAndName = root + fileName;


        return (
            <img src={dirAndName} height="80" width="80" id={type} onLoad={onLoad}/>
        )
    });