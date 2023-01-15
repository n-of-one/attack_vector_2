import React from "react";

const CloseButton =  ({closeAction}) => {
    return <div className="nodeInfoClose" onClick={ closeAction }><span className="close-x-position glyphicon glyphicon-remove" /></div>
}

export default CloseButton