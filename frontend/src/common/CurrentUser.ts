
export const CONNECTION_TYPE_GENERAL = "WS_GENERAL"
export const CONNECTION_TYPE_ICE = "WS_ICE"
export const CONNECTION_TYPE_GM = "WS_GM"
export const WEB_PAGE = "WEB_PAGE"

export type ConnectionType = "WS_GENERAL" | "WS_ICE" | "WS_GM" | "WEB_PAGE"

class CurrentUser {
    _id: string | null = null
    _connectionId: string | null = null
    _type: ConnectionType | null = null

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

    set type(type: ConnectionType) {
        this._type = type
    }

    get type(): ConnectionType {
        if (this._type === null) { throw Error("currentUser.type has not been set yet")}
        return this._type
    }



}

export const currentUser = new CurrentUser()