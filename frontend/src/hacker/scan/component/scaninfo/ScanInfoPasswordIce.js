import React from "react";

export default ({service}) => {
    return (
        <>
            &nbsp;ICE (static password)<br/>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;strength: <span className="text-info">unknown</span><br/>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hacked: <span className="text-danger">no</span>
        </>
    );
};
