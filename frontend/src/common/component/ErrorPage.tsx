import React from "react";

export const ErrorPage = ({message}: { message: string }) => {
    return (
        <div className="text">
            <h1 className="text-danger">System error</h1>
            <p>There was a problem with Attack Vector. If you are currently in a Larp, please see if refreshing helps, or if you can work around it.</p>
            <p>If you have time (are not in a Larp), please let the organisers know about this problem.</p>
            <p>Error: <span className="text-info">{message}</span></p>
        </div>
    )
}
