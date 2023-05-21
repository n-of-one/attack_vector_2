import React, {useEffect} from 'react'
import {SilentLink} from "../component/SilentLink";
import {UserOverview} from "./UsersReducer";
import {useSelector} from "react-redux";
import {GmState} from "../../gm/GmRootReducer";
import {webSocketConnection} from "../WebSocketConnection";
import {UserDetails} from "./UserDetails";
import {RequiresRole} from "../RequiresRole";
import {ROLE_HACKER_MANAGER, ROLE_USER_MANAGER} from "../UserAuthorizations";
import {TextInput} from "../component/TextInput";
import {User} from "./UserReducer";


export const UserManagement = () => {
    return <RequiresRole anyOf={[ROLE_USER_MANAGER, ROLE_HACKER_MANAGER]}>
        <UserManagementAuthorized/>
    </RequiresRole>

}

export const UserManagementAuthorized = () => {

    useEffect(() => {
        webSocketConnection.send("/av/user/overview", "")
    }, [])

    const users: UserOverview[] = useSelector((state: GmState) => state.users.overview)

    const selectUser = (userId: string) => {
        webSocketConnection.send("/av/user/select", userId)
    }

    const user = useSelector((state: GmState) => state.users.edit)

    return (
        <div className="row">
            <div className="col-lg-2">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">User management</h3><br/>
                    <br/>
                    <br/>
                    {CreateUser(user)}
                    <UserDetails user={user} />
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
                            users.map((user: UserOverview) => {
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

const CreateUser = (user: User | null) => {
    if (user) return null

    const createUser = (name: string) => {
        webSocketConnection.send("/av/user/create", name)
    }

    return <div className="row form-group">
        {/*<label htmlFor="newUser" className="col-lg-1 control-label text-muted">Email</label>*/}



        <div className="col-lg-8">
            <TextInput placeholder="User name"
                       buttonLabel="Create new user"
                       buttonClass="btn-info"
                       save={(name: string) => createUser(name)}
                       clearAfterSubmit={true}/>
        </div>
    </div>


}

