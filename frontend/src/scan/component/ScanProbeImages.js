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
    ({theme}) => {

        const root = "/img/" + theme + "/actors/scan_probes/";
        const pathStart = root + "probe-";

        const renderImage = (nr) => {
            return <img src={pathStart + nr + ".png"} height="80" width="80" id={"PROBE_" + nr} key={nr}/>
        };

        const nrs = [1,2,3,4,5,6,7,8,9,10];


        return (
            <span>
                { nrs.map( (nr) => renderImage(nr)) }
            </span>
        )
    });