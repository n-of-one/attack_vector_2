import React from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../../common/terminal/Terminal"
import {TangleRootState} from "../reducer/TangleRootReducer";
import {HIDDEN} from "../../common/IceModel";
import {IceTitle} from "../../common/IceTitle";
import {CloseTabButton} from "../../common/CloseTabButton";

export const TangleIceHome = () => {

    const ice = useSelector((state: TangleRootState) => state.tangle)
    const displayTerminal = useSelector((state: TangleRootState) => state.displayTerminal)

    const classHidden = ice.uiState === HIDDEN ? " hidden_alpha" : ""

    const showClusters = ice.clusters > 1

    return (
        <div className="row icePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-3">
                        <IceTitle name="Gaanth" strength={ice.strength}/>
                    </div>
                    <Clusters clusters={ice.clusters} classHidden={classHidden}/>
                    <div className="col-lg-6" style={{paddingTop: "4px"}}>
                        <Terminal terminalState={displayTerminal} height={84}/>
                    </div>
                    <div className="col-lg-1">
                        <div className="float-end">
                            <CloseTabButton/>
                        </div>
                    </div>
                </div>
                <hr style={{borderTopColor: "#300", marginTop: "5px", marginBottom: "5px"}}/>
                <div className={"row transition_alpha_fast" + classHidden}>
                    <div className="col-lg-12">
                        <div>
                            <canvas id="untangleCanvas" style={{
                                "borderRadius": "3px 3px 3px 3px",
                                "marginTop": "10px",
                                "marginBottom": "10px",
                            }}/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

const Clusters = ({clusters, classHidden}: { clusters: number, classHidden: string }) => {
    if (clusters === 1) {
        return <div className="col-lg-2">&nbsp;</div>
    }
    return (
        <div className="col-lg-2">
            <h4 className={"text-center text-success transition_alpha_fast" + classHidden}>Clusters<br/><span
                className="text-info">{clusters}</span></h4>
        </div>
    )

}
