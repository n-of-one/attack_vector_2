import React from 'react';
import {connect} from "react-redux";
import EditorMain from "./EditorMain";
import EditorTop from "./EditorTop";
import EditorBottom from "./EditorBottom";

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
                <EditorTop />
                <br />
                <EditorMain />
                <br />
                <EditorBottom />
                {/*<h2>Editor for { this.store.getState().siteLink }</h2>*/}

            </div>
        );
    });
