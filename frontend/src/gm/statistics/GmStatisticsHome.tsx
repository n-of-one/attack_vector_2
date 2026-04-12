import React from "react";
import {Button} from "react-bootstrap";


const exportStatistics = () => {
    const developmentServer = (window.location.port === "3000")
    const url = (developmentServer) ? "http://localhost/gm/statistics" : "/gm/statistics"
    window.open(url, '_blank')
}


export const GmStatisticsHome = () => {

    return (

        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">ICE hack statistics</h3><br/>
                </div>
                <div className="text">
                    Attack Vector keeps track of how long it took to hack ICE. This data is useful if you want to figure out
                    how strong you must make your ICE to provide a fun challenge for your players.<br/>
                    <br/>
                    The export is a CSV file with a semicolon (;) as delimiter. Most programs like Excel and LibreOffice should be able to import these.
                    You can figure out the average hack time for each type of ICE for each strength in those programs.<br/>
                    <br/>
                    <br/>
                    <Button className="btn btn-info text-size" onClick={exportStatistics}>Export statistics</Button><br/>
                    <br/>
                </div>


            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="rightPanel">
                </div>
            </div>
        </div>
    )
}
