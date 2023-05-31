import React from 'react'
import {CLOSE_USER_EDIT } from "./UsersReducer";
import {User, USER_TYPE_ADMIN, USER_TYPE_GM, USER_TYPE_HACKER, USER_TYPE_HACKER_MANAGER} from "./UserReducer";
import {TextSaveInput} from "../component/TextSaveInput";
import {DropDownSaveInput} from "../component/DropDownSaveInput";
import userAuthorizations, {ROLE_GM, ROLE_HACKER_MANAGER, ROLE_USER_MANAGER} from "../user/UserAuthorizations";
import {CloseButton} from "../component/CloseButton";
import {useDispatch} from "react-redux";
import {webSocketConnection} from "../server/WebSocketConnection";
import {currentUser} from "../user/CurrentUser";
import {larp} from "../Larp";

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
    const closeUserEdit = () => { dispatch({type: CLOSE_USER_EDIT}) }

    const save = (field: string, value: string) => {
        const message = {userId: user.id, field, value}
        webSocketConnection.send("/av/user/edit", message)
    }


    return <>
        <div className="d-flex flex-row justify-content-end">
            <CloseButton closeAction={closeUserEdit} />
        </div>

        <hr/>

        <UserAttribute
            label="Name" id="name" size={8}
            value={user.name}
            save={save} attributeSaveName="name"
            readonly={false}
        />

        <UserAttribute
            label="Email" id="email" size={8}
            value={user.email}
            save={save} attributeSaveName="email"
            readonly={false}
            show = {larp.userEmailEnabled}
        />

        <UserAttribute
            label="GM Note" id="gmNote" size={8}
            value={user.gmNote}
            save={save} attributeSaveName="gmNote"
            readonly={false}
            show={userAuthorizations.hasRole(ROLE_GM)}
        />


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
        { deleteUserButton(user, closeUserEdit) }
    </>
}

const hackerFormPart = (user: User, save: SaveFunction) => {

    const authorizationToEditSkills = userAuthorizations.hasRole(ROLE_USER_MANAGER) || userAuthorizations.hasRole(ROLE_HACKER_MANAGER)
    const readonlySkills = !(larp.userEditSkills && authorizationToEditSkills)


    return <>
        <hr/>
        <UserAttribute
            label="Character Name" id="characterName" size={8}
            value={user.hacker?.characterName}
            save={save} attributeSaveName="characterName"
            readonly={!larp.userEditCharacterName}
        />

        <UserAttribute
            label="Hacker level" id="skillHacker" size={2}
            value={user.hacker?.skill.hacker}
            save={save} attributeSaveName="skillHacker"
            readonly={readonlySkills}
            indent={1}
        />

        <UserAttribute
            label="Elite level" id="skillElite" size={2}
            value={user.hacker?.skill.elite}
            save={save} attributeSaveName="skillElite"
            readonly={readonlySkills}
            indent={1}
        />

        <UserAttribute
            label="Architect level" id="skillArchitect" size={2}
            value={user.hacker?.skill.architect}
            save={save} attributeSaveName="skillArchitect"
            readonly={readonlySkills}
            indent={1}
        />
    </>
}

interface UserAttributeProps {
    label: string,
    id: string,
    size: number,
    value?: string | number,
    save: SaveFunction,
    attributeSaveName: string
    readonly: boolean,
    indent?: number,
    show?: boolean
}

const UserAttribute = ({label, id, size, value, readonly, save, attributeSaveName, indent, show}: UserAttributeProps) => {
    if (show === false) return <></>

    const labelIndentElement = indent ? <div className={`col-lg-${indent}`}/> : <></>
    const labelSize = indent? 4 - indent : 4
    const labelClass = `col-lg-${labelSize} control-label text-muted`

    return <div className="row form-group">
        {labelIndentElement}
        <label htmlFor={id} className={labelClass}>{label}</label>
        <div className={`col-lg-${size}`}>
            <TextSaveInput id={id} className="form-control"
                           placeholder="" value={value}
                           save={(value: string) => save(attributeSaveName, value)}
                           readonly={readonly}/>
        </div>
    </div>


}

const deleteUserButton = (user: User, closeUserEdit: () => void) => {
    if (user.id === currentUser.id) return null

    const deleteFun = () => {
        const confirmResult = window.confirm("Are you sure you want to delete user " + user.name + "?")
        if (!confirmResult) return
        webSocketConnection.send("/av/user/delete", user.id)
        closeUserEdit()
    }

    return <>
        <hr/>
        <div className="d-flex flex-row">
            <button className="btn btn-info btn-sm pull-right" onClick={deleteFun}>Delete</button>
        </div>
    </>
}