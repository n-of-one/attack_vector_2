import {SilentLink} from "../../../common/component/SilentLink";
import React, {useEffect} from "react";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {useDispatch, useSelector} from "react-redux";
import {GmRootState} from "../../GmRootReducer";
import {TextSaveInput} from "../../../common/component/TextSaveInput";
import {User} from "../../../common/users/CurrentUserReducer";
import {CloseButton} from "../../../common/component/CloseButton";
import {InfoBadge} from "../../../common/component/ToolTip";
import {CLOSE_USER_EDIT, UserOverview} from "../../../common/users/EditUserDataReducer";
import {UserOverviewTable} from "../../../common/users/UserManagement";
import {ScriptTypesTable} from "../scriptType/ScriptTypeManagement";
import {ScriptType} from "../scriptType/ScriptTypeReducer";
import {ScriptAccess} from "./ScriptAccessReducer";


export const ScriptAccessManagement = () => {

    useEffect(() => {
        webSocketConnection.send("/user/overview", "")
    }, [])

    const userOverViewLines: UserOverview[] = useSelector((state: GmRootState) => state.users.overview)
    const hackers = userOverViewLines.filter(overViewLine => overViewLine.hacker)


    const selectUser = (userOverview: UserOverview) => {
        webSocketConnection.send("/gm/scriptAccess/get", userOverview.id)
    }

    const user = useSelector((state: GmRootState) => state.users.edit.userData)
    const userAccessElement = user ? <AccessOfUser user={user}/> :
        <div className="text">Click on a user in the table on the right to manage their access to scripts.</div>

    const addScriptAccess = (type: ScriptType) => {
        webSocketConnection.send("/gm/scriptAccess/add", {userId: user!!.id, typeId: type.id})
    }

    const tableElement = user ? <ScriptTypesForAcess onSelect={addScriptAccess}/> : <UserOverviewTable users={hackers} selectUser={selectUser}/>

    return (
        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">Manage script access</h3><br/>
                </div>
                {userAccessElement}
            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="rightPanel">
                    {tableElement}
                </div>
            </div>
        </div>
    )
}

const ScriptTypesForAcess = ({onSelect}: { onSelect: (type: ScriptType) => void }) => {
    return (<>
            <div className="row">
                <div className="col-lg-12 text">
                    Click in a script type to give the hacker access to it to.<br/>
                    <br/>
                </div>
            </div>
            <ScriptTypesTable onSelect={onSelect}/>
        </>
    )
}


export const AccessOfUser = ({user}: { user: User }) => {
    const accesses = useSelector((state: GmRootState) => state.users.edit.scriptAccess)
    const dispatch = useDispatch()
    const close = () => {
        dispatch({type: CLOSE_USER_EDIT})
    }

    const header = (<div className="text">
        <div className="d-flex justify-content-between">
            <h5 className="text-muted">{user.name}'s access to scripts</h5>
            <h5><CloseButton closeAction={close}/></h5>
        </div>
        <br/>
    </div>)

    if (accesses.length === 0) {
        return (<>
            {header}å
            <div className="row">
                <div className="col-lg-12 text"><br/><br/>
                    {user.name} currently does not have access to any scripts.<br/>
                    <br/>
                    This means they will not receive them for free and cannot buy them on the dark web.<br/>
                    They can still receive them from fellow hackers if they have the skill to run scripts.<br/>
                    <br/>
                    <br/>
                    Click on a script type in the table on the right to give them access to it.<br/>
                </div>
            </div>
        </>)
    }


    return (<>
        {header}

        <div className="text">
            <br/>
            <div className="row form-group">
                <div className="col-lg-4">Name</div>
                <div className="col-lg-2">Receive&nbsp;<InfoBadge infoText="The number of scripts the hacker will receive for free." placement="top"/>
                </div>
                <div className="col-lg-2"/>
                {/*<div className="col-lg-2">Price&nbsp;<InfoBadge*/}
                {/*    infoText="The price this hacker can buy the script for. If empty, the hacker cannot buy this script." placement="top"/></div>*/}
                <div className="col-lg-1">Action</div>
            </div>
            <br/>
            <br/>
            <>{accesses.map(access => <ScriptAccessElement access={access} key={access.id}/>)}</>

        </div>
    </>)
}

const ScriptAccessElement = ({access}: { access: ScriptAccess }) => {

    const save = (toSave: ScriptAccess) => {
        webSocketConnection.send("/gm/scriptAccess/edit", toSave)
    }

    const deleteAccess = () => {
        webSocketConnection.send("/gm/scriptAccess/delete", access.id)
    }

    return <div className="row form-group">

        <div className="col-lg-4">
            {access.type.name}
        </div>
        <div className="col-lg-2">
            <TextSaveInput id={access.id} className="form-control"
                           placeholder="" value={`${access.receiveForFree}x`}
                           save={(usesInput) => {
                               const normalized = usesInput.replace("x", "")
                               const receiveForFree = isNaN(parseInt(normalized)) ? 0 : parseInt(normalized)
                               const accessCopy = {...access, receiveForFree}
                               save(accessCopy)
                           }}
                           readonly={false}/>
        </div>
        <div className="col-lg-2"/>
        {/*<div className="col-lg-2">*/}
        {/*    <TextSaveInput id={access.id} className="form-control"*/}
        {/*                   placeholder="" value={access.price === null ? "" : access.price}*/}
        {/*                   save={(priceInput) => {*/}
        {/*                       const price = isNaN(parseFloat(priceInput)) ? null : parseFloat(priceInput)*/}
        {/*                       const accessCopy = {...access, price}*/}
        {/*                       save(accessCopy)*/}
        {/*                   }}*/}
        {/*                   readonly={false}/>*/}
        {/*</div>*/}
        <div className="col-lg-1" style={{paddingTop: "9px"}}>
            <SilentLink onClick={deleteAccess} title="Remove script">
                <span className="glyphicon glyphicon-trash"/>
            </SilentLink>
        </div>
    </div>

}
