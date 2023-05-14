import React from 'react'
import {CLOSE_USER_EDIT, User, USER_TYPE_ADMIN, USER_TYPE_GM, USER_TYPE_HACKER, USER_TYPE_HACKER_MANAGER} from "./UsersReducer";
import {TextSaveInput} from "../../common/component/TextSaveInput";
import {DropDownSaveInput} from "../../common/component/DropDownSaveInput";
import userAuthorizations, {ROLE_HACKER_MANAGER, ROLE_USER_MANAGER} from "../../common/UserAuthorizations";
import {CloseButton} from "../../common/component/CloseButton";
import {useDispatch} from "react-redux";
import {webSocketConnection} from "../../common/WebSocketConnection";

type SaveFunction = (field: string, value: string) => void

interface Props {
    user: User | null,
}

export const UserDetails = ({user}: Props) => {
    if (!user) {
        return <></>
    }
    const readOnlyType: boolean = !userAuthorizations.hasRole(ROLE_USER_MANAGER)

    return <UserDetailsInternal user={user} readOnlyType={readOnlyType}/>
}

interface PropsInternal {
    user: User,
    readOnlyType: boolean
}

const UserDetailsInternal = ({user, readOnlyType}: PropsInternal) => {
    const dispatch = useDispatch()
    const save = (field: string, value: string) => {
        const message = {userId: user.id, field, value}
        webSocketConnection.send("/av/user/edit", message)
    }

    const closeUserEdit = () => { dispatch({type: CLOSE_USER_EDIT}) }
    const deleteFun = () => {
        const confirmResult = window.confirm("Are you sure you want to delete user " + user.name + "?")
        if (!confirmResult) return
        webSocketConnection.send("/av/user/delete", user.id)
        closeUserEdit()
    }

    return <>
        <div className="d-flex flex-row justify-content-end">
            <CloseButton closeAction={closeUserEdit} />
        </div>

        <hr/>

        <div className="row form-group">
            <label htmlFor="placeholder" className="col-lg-4 control-label text-muted">Name</label>
            <div className="col-lg-3">
                <TextSaveInput id="name" className="form-control"
                               placeholder="Name" value={user.name}
                               save={(value: string) => save("name", value)}/>
            </div>
        </div>
        <div className="row form-group">
            <label htmlFor="email" className="col-lg-4 control-label text-muted">Email</label>
            <div className="col-lg-8">
                <TextSaveInput id="email" className="form-control"
                               placeholder="Email" value={user.email}
                               save={(value: string) => save("email", value)}/>
            </div>
        </div>

        <div className="row form-group">
            <label htmlFor="type" className="col-lg-4 control-label text-muted">Type</label>
            <div className="col-lg-3">
                <DropDownSaveInput id="type" className="form-control"
                                   selectedValue={user.type}
                                   save={(value: string) => save("type", value)}
                                   readonly={readOnlyType}>
                    <>
                        <option value={USER_TYPE_HACKER}>Hacker</option>
                        <option value={USER_TYPE_HACKER_MANAGER}>Hacker manager</option>
                        <option value={USER_TYPE_GM}>Game master</option>
                        <option value={USER_TYPE_ADMIN}>Admin</option>
                    </>
                </DropDownSaveInput>
            </div>
        </div>
        {(user.type === USER_TYPE_HACKER || user.type === USER_TYPE_HACKER_MANAGER) ? hackerFormPart(user, save) : <></>}
        <hr/>
        <div className="d-flex flex-row">
            <button className="btn btn-info btn-sm pull-right" onClick={deleteFun}>Delete</button>
        </div>

    </>
}

const hackerFormPart = (user: User, save: SaveFunction) => {

    const readonlySkills = !(userAuthorizations.hasRole(ROLE_USER_MANAGER) || userAuthorizations.hasRole(ROLE_HACKER_MANAGER))


    return <>
        <hr/>
        <div className="row form-group">
            <label htmlFor="characterName" className="col-lg-4 control-label text-muted">Character name</label>
            <div className="col-lg-8">
                <TextSaveInput id="characterName" className="form-control"
                               placeholder="In game name" value={user.hacker?.characterName}
                               save={(value: string) => save("characterName", value)}/>
            </div>
        </div>
        <div className="row form-group">
            <label htmlFor="playerName" className="col-lg-4 control-label text-muted">Player name</label>
            <div className="col-lg-8">
                <TextSaveInput id="playerName" className="form-control"
                               placeholder="Out of game name" value={user.hacker?.playerName}
                               save={(value: string) => save("playerName", value)}/>
            </div>
        </div>

        <div className="row form-group">
            <div className="col-lg-1"/>
            <label htmlFor="skillHacker" className="col-lg-3 control-label text-muted">Hacker level</label>
            <div className="col-lg-2">
                <TextSaveInput id="skillHacker" className="form-control"
                               placeholder="" value={user.hacker?.skill.hacker}
                               save={(value: string) => save("skillHacker", value)}
                               readonly={readonlySkills}/>
            </div>
        </div>

        <div className="row form-group">
            <div className="col-lg-1"/>
            <label htmlFor="skillElite" className="col-lg-3 control-label text-muted">Elite level</label>
            <div className="col-lg-2">
                <TextSaveInput id="skillElite" className="form-control"
                               placeholder="" value={user.hacker?.skill.elite}
                               save={(value: string) => save("skillElite", value)}
                               readonly={readonlySkills}/>
            </div>
        </div>

        <div className="row form-group">
            <div className="col-lg-1"/>
            <label htmlFor="skillArchitect" className="col-lg-3 control-label text-muted">Architect level</label>
            <div className="col-lg-2">
                <TextSaveInput id="skillArchitect" className="form-control"
                               placeholder="" value={user.hacker?.skill.architect}
                               save={(value: string) => save("skillArchitect", value)}
                               readonly={readonlySkills}/>
            </div>
        </div>
    </>

}