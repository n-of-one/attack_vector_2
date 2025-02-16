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
import {ErrorPage} from "../../../common/component/ErrorPage";
import {ScriptLine, ScriptLineUseCase} from "../../../common/script/ScriptLine";
import {Script} from "../../../common/script/ScriptModel";
import {DataTable} from "../../../common/component/dataTable/DataTable";
import {Hr} from "../../../common/component/dataTable/Hr";

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
    const tableElement = user ? <ScriptsToAddTable user={user}/> :
        <UserOverviewTable users={hackers} selectUser={selectUser}/>

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
                <div className="rightPanel">
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
    const dispatch = useDispatch()
    const scriptsLoading = useSelector((state: GmRootState) => state.users.edit.scriptsLoading)

    const scripts = user.hacker?.scripts
    const close = () => {
        dispatch({type: CLOSE_USER_EDIT})
    }

    if (!scripts) return <ErrorPage message="No scripts information found for this user."/>

    const rowElements = scripts.map((script: Script) => {
        const loading = scriptsLoading.find((loading) => loading.scriptId === script.id)
        return <ScriptLine script={script}
                           loading={loading}
                           useCase={ScriptLineUseCase.GM}
                           key={script.id}/>
    })
    const rowTexts = scripts.map((script: Script) => `${script.code}~${script.name}~${script.state}`)

    const hr = <Hr color="#999"/>

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
            <DataTable rows={rowElements} rowTexts={rowTexts} pageSize={30} hr={hr}>
                <div className="row text strong">
                    <div className="col-lg-2 text-end">Code</div>
                    <div className="col-lg-2">Name</div>
                    <div className="col-lg-1">RAM</div>
                    <div className="col-lg-2">State</div>
                    <div className="col-lg-2">Action</div>
                    <div className="col-lg-2">Effects</div>
                </div>
            </DataTable>
        </div>
    </>)
}

