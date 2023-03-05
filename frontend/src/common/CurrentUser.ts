

class CurrentUser {
    _id: string | null = null
    _connectionId: string | null = null

    set id(userId: string) {
        this._id = userId
    }

    get id(): string {
        if (this._id === null) { throw Error("currentUser.id has not been set yet")}
        return this._id
    }

    set connectionId(connectionId: string) {
        this._connectionId = connectionId
    }

    get connectionId(): string {
        if (this._connectionId === null) { throw Error("currentUser.connectionId has not been set yet")}
        return this._connectionId
    }

}

export const currentUser = new CurrentUser()