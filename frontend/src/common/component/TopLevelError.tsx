import React from "react";

export const TopLevelError = ({error, description}: { error: string, description: string }) => {
    return <div className="topLevelError"
    style={{color: "whitesmoke", textAlign: "center", fontSize:"36px"}}>
        <h1>{error}</h1>
        <br/>
        <h3 style={{color: "cornsilk"}}>{description}</h3>
    </div>
}