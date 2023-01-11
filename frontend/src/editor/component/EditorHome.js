import React from 'react';
import {connect} from "react-redux";
import EditorMain from "./middle/EditorMain";
import EditorTop from "./top/EditorTop";
import EditorBottom from "./bottom/EditorBottom";

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
            <div className="container-fluid" data-bs-theme="dark">
                <br />
                <EditorTop />
                <br />
                <EditorMain />
                <br />
                <EditorBottom />

            </div>
        );
    });
