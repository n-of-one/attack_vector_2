import React from "react";

export default ({closeAction}) => {
    return <div className="nodeInfoClose" onClick={ closeAction }><span className="close-x-position glyphicon glyphicon-remove" /></div>
}