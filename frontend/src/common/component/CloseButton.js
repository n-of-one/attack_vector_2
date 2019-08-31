import React from "react";

export default ({closeAction}) => {
    return <div className="nodeInfoClose" onClick={ closeAction }><span className="glyphicon glyphicon-ok"/></div>
}