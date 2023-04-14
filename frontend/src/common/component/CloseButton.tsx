import React from "react";

export const CloseButton =  ({closeAction}: {closeAction: () => void}) => {
    return <span className="nodeInfoClose" onClick={ closeAction }><span className="close-position glyphicon glyphicon-remove" /></span>
}
