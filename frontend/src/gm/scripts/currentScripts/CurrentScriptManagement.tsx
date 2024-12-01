import React, {useEffect} from "react";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {CLOSE_USER_EDIT, UserOverview} from "../../../common/users/EditUserDataReducer";
import {useDispatch, useSelector} from "react-redux";
import {GmRootState} from "../../GmRootReducer";
import {ScriptType} from "../scriptType/ScriptTypeReducer";
import {ScriptTypesTable} from "../scriptType/ScriptTypeManagement";
import {UserOverviewTable} from "../../../common/users/UserManagement";
import {User} from "../../../common/users/CurrentUserReducer";
import {CloseButton} from "../../../common/component/CloseButton";
import {Script, ScriptState} from "../../../common/script/ScriptModel";
import {ErrorPage} from "../../../common/component/ErrorPage";

export const CurrentScriptManagement = () => {

    useEffect(() => {
        webSocketConnection.send("/user/overview", "")
    }, [])

    const userOverViewLines: UserOverview[] = useSelector((state: GmRootState) => state.users.overview)
    const hackers = userOverViewLines.filter(overViewLine => overViewLine.hacker)

    const user = useSelector((state: GmRootState) => state.users.edit.userData)
    const userScriptsElement = user ? <ScriptsOfUser user={user}/> :
        <div className="text">Click on a user in the table on the right to manage their current scripts.</div>


    const selectUser = (userOverview: UserOverview) => {
        webSocketConnection.send("/user/select", userOverview.id)
    }
    const tableElement = user ? <ScriptsToAddTable user={user}/> : <UserOverviewTable users={hackers} selectUser={selectUser}/>

    return (
        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">Manage current scripts of hackers</h3><br/>
                </div>
                {userScriptsElement}
            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="siteMap">
                    {tableElement}
                </div>
            </div>
        </div>
    )
}

const ScriptsToAddTable = ({user}: { user: User }) => {
    const giveScript = (type: ScriptType) => {
        webSocketConnection.send("/gm/script/add", {userId: user.id, typeId: type.id})
    }

    return <>
        <div className="text">Click on a script to give it to the hacker.</div>
        <br/>
        <ScriptTypesTable onSelect={giveScript}/>
    </>
}


export const ScriptsOfUser = ({user}: { user: User }) => {
    const scripts = user.hacker?.scripts
    const dispatch = useDispatch()
    const close = () => {
        dispatch({type: CLOSE_USER_EDIT})
    }

    if (!scripts) return <ErrorPage message="No scripts information found for this user."/>

    return (<>

        <div className="text">
            <div className="d-flex justify-content-between">
                <h5 className="text-muted">{user.name}'s current scripts</h5>
                <h5><CloseButton closeAction={close}/></h5>
            </div>
            <br/>
        </div>

        <div className="text">
            <br/>
            <div className="row">
                <div className="col-lg-2">Name</div>
                <div className="col-lg-1">RAM</div>
                <div className="col-lg-2">State</div>
                <div className="col-lg-1">Action</div>
            </div>
            <br/>
            <br/>
            <>{scripts.map(script => <ScriptElement script={script} key={script.id}/>)}</>
            <br/>
            <br/>
            <br/>
            <br/>


        </div>
    </>)
}

export const ScriptElement = ({script}: { script: Script }) => {


    return (<div className="row">
            <div className="col-lg-2">{script.name}</div>
            <div className="col-lg-1">{script.ram}</div>
            <div className="col-lg-2"><ScriptStateElement script={script}/></div>
            <div className="col-lg-1">-</div>
        </div>
    )
}

const ScriptStateElement = ({script}: { script: Script }) => {
    switch (script.state) {
        case ScriptState.NOT_LOADED:
            return <span className="badge bg-secondary">Available</span>
        case ScriptState.LOADING:
            return <span className="badge bg-secondary">Loading</span>
        case ScriptState.LOADED:
            return <span className="badge bg-secondary">Loaded</span>
        case ScriptState.USED:
            return <span className="badge bg-secondary">Used</span>
        case ScriptState.EXPIRED:
            return <span className="badge bg-secondary">Expired</span>
        case ScriptState.DELETED:
            return <span className="badge bg-secondary">Deleted</span>
        default:
            return <span>Unknown</span>
    }
}
