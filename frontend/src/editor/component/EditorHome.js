import React from 'react';
import {connect} from "react-redux";
import SiteData from "./SiteData";
import EditorMain from "./EditorMain";

const mapDispatchToProps = (dispatch) => {
    return {
    }
};
let mapStateToProps = (state) => {
    return {
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    () => {
        return (
            <div className="container">
                <br/>
                <SiteData />
                <br />
                <EditorMain />
                {/*<h2>Editor for { this.store.getState().siteLink }</h2>*/}
            </div>
        );
    });
