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

        const root = "/img/" + theme + "/actors/patrollers/";
        const dirAndName = root + fileName;


        return (
            <img src={dirAndName} id={type} onLoad={onLoad}/>
        )
    });