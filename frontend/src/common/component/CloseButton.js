import React from "react";

const CloseButton =  ({closeAction}) => {
    return <span className="nodeInfoClose" onClick={ closeAction }><span className="close-x-position glyphicon glyphicon-remove" /></span>
}

export default CloseButton