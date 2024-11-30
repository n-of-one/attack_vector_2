import React, {useEffect} from "react";
import {navigateTo} from "../../common/menu/MenuItem";
import {Page} from "../../common/menu/pageReducer";
import {useDispatch} from "react-redux";
import {ActionButton} from "../../common/component/ActionButton";
import {Icon} from "../../common/component/icon/Icon";
import {GLYPHICON_EXPAND, GLYPHICON_FLASH, GLYPHICON_LOGIN} from "../../common/component/icon/Glyphicon";
import {ToolTip} from "../../common/component/ToolTip";
import {webSocketConnection} from "../../common/server/WebSocketConnection";


interface ScriptTypeOverview {
    id: string,
    name: string,
    description: string,
    ram: number,
    hackersThatCanBuy: number,
    hackersThatGetForFree: number,
    hackersThatOwn: number
}

export const GmScriptHome = () => {

    const dispatch = useDispatch()
    useEffect(() => {
            webSocketConnection.send("/gm/scriptType/getAll", null)
        },
        )

    const scriptTypeOverviews = [
        {
            id: "1",
            name: "mask",
            description: "description",
            ram: 1,
            hackersThatCanBuy: 1,
            hackersThatGetForFree: 0,
            hackersThatOwn: 0
        },
        {
            id: "2",
            name: "mask++",
            description: "description",
            ram: 2,
            hackersThatCanBuy: 1,
            hackersThatGetForFree: 0,
            hackersThatOwn: 0
        },
        {
            id: "3",
            name: "deepscan",
            description: "description",
            ram: 2,
            hackersThatCanBuy: 1,
            hackersThatGetForFree: 0,
            hackersThatOwn: 0
        },
    ]

    return (

        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">Script management</h3><br/>
                </div>
                <div className="text">
                        Scripts are single-use programs that hackers can use during a hack.<br/>
                        <br/>
                        <br/>
                        <br/>
                        <ActionButton onClick={() => {
                            navigateTo(Page.SCRIPT_MANAGEMENT, Page.SCRIPT_TYPE_MANAGEMENT, null, dispatch)
                        }}>
                            <><Icon type={GLYPHICON_FLASH} size="12px" height="12px"/> Script types</>
                        </ActionButton>
                        &nbsp;Manage the scripts that exist in your game.<br/>
                        <br/>
                        <br/>
                        <ActionButton onClick={() => {
                            navigateTo(Page.SCRIPT_MANAGEMENT, Page.SCRIPT_ACCESS_MANAGEMENT, null, dispatch)
                        }}>
                            <><Icon type={GLYPHICON_LOGIN} size="12px" height="12px"/> Script access</>
                        </ActionButton>
                        &nbsp;Manage the scripts that hackers have access to (without GM intervention)<br/>
                        <br/>
                        <br/>
                        <ActionButton onClick={() => {
                            navigateTo(Page.SCRIPT_MANAGEMENT, Page.SCRIPT_MANAGEMENT, null, dispatch)
                        }}>
                            <><Icon type={GLYPHICON_EXPAND} size="12px" height="12px"/> Current scripts</>
                        </ActionButton>
                        &nbsp; Directly manage the scripts hackers currently have.
                </div>


            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="siteMap">
                    <table className="table table-sm text-muted text" id="sitesTable">
                        <thead>
                        <tr>
                            <td className="strong">Script type</td>
                            <td className="strong">RAM</td>
                            <td className="strong">Owned</td>
                            <td className="strong">Can buy</td>
                            <td className="strong">Free receive</td>
                        </tr>
                        </thead>
                        <tbody>
                        {
                            scriptTypeOverviews.map((overview: ScriptTypeOverview) => {
                                return (
                                    <tr key={overview.id}>
                                        <td><ToolTip id={overview.id} text={overview.description}><span
                                            className="badge bg-secondary helpBadge">?</span></ToolTip> {overview.name}
                                        </td>
                                        <td>{overview.ram}</td>
                                        <td>{overview.hackersThatOwn}</td>
                                        <td>{overview.hackersThatCanBuy}</td>
                                        <td>{overview.hackersThatGetForFree}</td>
                                    </tr>)
                            })
                        }
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
)

}
