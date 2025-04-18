import React, {useEffect} from 'react'
import {SilentLink} from "../component/SilentLink";
import {UserOverview, UserTagLabels} from "./EditUserDataReducer";
import {useSelector} from "react-redux";
import {GmRootState} from "../../gm/GmRootReducer";
import {webSocketConnection} from "../server/WebSocketConnection";
import {UserDetails} from "./UserDetails";
import {RequiresRole} from "../user/RequiresRole";
import {ROLE_USER_MANAGER} from "../user/UserAuthorizations";
import {TextInput} from "../component/TextInput";
import {User, UserType, UserTypeLabels} from "./CurrentUserReducer";
import {DataTable} from "../component/dataTable/DataTable";
import {Hr} from "../component/dataTable/Hr";


export const UserManagement = () => {
    return <RequiresRole anyOf={[ROLE_USER_MANAGER]}>
        <UserManagementAuthorized/>
    </RequiresRole>

}

export const UserManagementAuthorized = () => {

    useEffect(() => {
        webSocketConnection.send("/user/overview", "")
    }, [])

    const users: UserOverview[] = useSelector((state: GmRootState) => state.users.overview)


    const selectUser = (userOverview: UserOverview) => {
        webSocketConnection.send("/user/select", userOverview.id)
    }

    const user = useSelector((state: GmRootState) => state.users.edit.userData)

    return (
        <div className="row">
            <div className="col-lg-2">
            </div>
            <div className="col-lg-4">
                <div className="text">
                    <h3 className="text-info">User management</h3><br/>
                    {CreateUser(user)}
                    {user !== null ? <UserDetails user={user}/> : <></>}
                </div>

            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="rightPanel">
                    <UserOverviewTable users={users} selectUser={selectUser}/>
                </div>
            </div>
        </div>
    )
}

export const UserOverviewTable = ({users, selectUser}: { users: UserOverview[], selectUser: (user: UserOverview) => void }) => {
    const sortedUsers = sortUsers(users).map((user: UserOverview) => {
        return {...user, name: user.name.toLowerCase()}
    })
    const sortedTexts = sortedUsers.map(user => `${user.name}~${user.characterName}~${UserTypeLabels[user.type]}~${UserTagLabels[user.tag]}`)

    const rows = sortedUsers.map((user: UserOverview) => {
        return (
            <div className="row text" key={user.id}>
                <div className="col-lg-4"><SilentLink onClick={() => {
                    selectUser(user)
                }}><>{user.name}</>
                </SilentLink>
                </div>
                <div className="col-lg-4">{user.characterName}</div>
                <div className="col-lg-2">{UserTypeLabels[user.type]}</div>
                <div className="col-lg-2">{UserTagLabels[user.tag]}</div>
            </div>)
    })

    const hr = <Hr height={4} marginTop={2}/>

    return (
        <DataTable rows={rows} rowTexts={sortedTexts} pageSize={35} hr={hr}>
            <div className="row text">
                <div className="col-lg-4 strong">Name</div>
                <div className="col-lg-4 strong">Character</div>
                <div className="col-lg-2 strong">Type</div>
                <div className="col-lg-2 strong">Tag</div>
            </div>
        </DataTable>
    )
}


const CreateUser = (user: User | null) => {
    if (user) return null

    const createUser = (name: string) => {
        webSocketConnection.send("/user/create", name)
    }

    return <div className="row form-group">
        {/*<label htmlFor="newUser" className="col-lg-1 control-label text-muted">Email</label>*/}


        <div className="col-lg-5">
            <TextInput placeholder="User name"
                       buttonLabel="Create"
                       buttonClass="btn-info"
                       save={(name: string) => createUser(name)}
                       clearAfterSubmit={true}/>
        </div>
    </div>


}

// Sort users by: Type, Tag, Name.
const sortUsers = (users: UserOverview[]): UserOverview[] => {
    const unsortedUsers = [...users]
    const alphabeticalUsers = unsortedUsers.sort((a, b) => {

        const nameA = a.name.toUpperCase()
        const nameB = b.name.toUpperCase()

        if (nameA < nameB) return -1
        if (nameA > nameB) return 1
        return 0
    })

    const tagSortedUsers = alphabeticalUsers.sort((a, b) => {
        const tagA = UserTagLabels[a.tag]
        const tagB = UserTagLabels[b.tag]
        if (tagA < tagB) return 1
        if (tagA > tagB) return -1
        return 0
    })

    const typeSortedUsers = tagSortedUsers.sort((a, b) => {
        if (a.type === UserType.HACKER && b.type !== UserType.HACKER) return -1
        if (a.type !== UserType.HACKER && b.type === UserType.HACKER) return 1

        if (a.type === UserType.GM && b.type !== UserType.GM) return -1
        if (a.type !== UserType.GM && b.type === UserType.GM) return 1

        return 0
    })

    return typeSortedUsers
}
