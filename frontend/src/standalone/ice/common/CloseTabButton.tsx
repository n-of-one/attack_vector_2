import React from "react";

export const CloseTabButton = () => {
    const close = () => window.close()
    return <span className="nodeInfoClose closeButtonLarge" onClick={close}>
        <span className="close-position glyphicon glyphicon-remove closeButtonLargeIcon"/>
    </span>
}
