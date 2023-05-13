import React, {useEffect} from 'react'
import {SilentLink} from "../../common/component/SilentLink";
import {UserOverview} from "./UsersReducer";
import {useSelector} from "react-redux";
import {GmState} from "../GmRootReducer";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {UserDetails} from "./UserDetails";
import {RequiresRole} from "../../common/RequiresRole";
import {ROLE_HACKER_MANAGER, ROLE_USER_MANAGER} from "../../common/UserAuthorizations";





export const UserManagement = () => {
    return <RequiresRole anyOf={[ROLE_USER_MANAGER,ROLE_HACKER_MANAGER]}>
        <UserManagementAuthorized/>
    </RequiresRole>

}

export const UserManagementAuthorized = () => {

    useEffect( () => {
        webSocketConnection.send("/av/user/overview", "")
    }, [])

    const users: UserOverview[] = useSelector((state: GmState) => state.users.overview)

    const selectUser = (userId: string) => {
        webSocketConnection.send("/av/user/select", userId)
    }

    const user = useSelector((state: GmState) => state.users.edit)
    const save = (field: string, value: string) => {
        if (!user) throw Error("No user to save")
        const message = {userId: user.id, field, value}
        webSocketConnection.send("/av/user/edit", message)
    }

    return (
        <div className="row">
            <div className="col-lg-2">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <strong>User management</strong><br/>
                    <br/>
                    <br/>
                    <UserDetails user={user} save={save} />
                </div>
                <div className="text">[create user]
                </div>
            </div>
            <div className="col-lg-5 rightPane rightPane">
                <div className="siteMap">
                    <table className="table table-sm text-muted text" id="sitesTable">
                        <thead>
                        <tr>
                            <td className="strong">Name</td>
                            <td className="strong">Character</td>
                            <td className="text-strong">Player</td>
                        </tr>
                        </thead>
                        <tbody>
                        {
                            users.map((user: UserOverview ) => {
                                return (
                                    <tr key={user.id}>
                                        <td className="table-very-condensed"><SilentLink onClick={() => {
                                            selectUser(user.id)
                                        }}><>{user.name}</>
                                        </SilentLink>
                                        </td>
                                        <td>{user.characterName}</td>
                                        <td>{user.playerName}</td>
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


