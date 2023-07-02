class CurrentUser {
    _id: string | null = null
    _mandatedId : string | null = null

    set id(userId: string) {
        if (this._mandatedId && this._mandatedId !== userId) {
            throw Error(`currentUser.id is ${userId} but mandatedId is ${this._mandatedId}`)
        }
        this._id = userId
    }

    get id(): string {
        if (this._id === null) { throw Error("currentUser.id has not been set yet")}
        return this._id
    }

    set mandatedId(mandatedId: string) {
        this._mandatedId = mandatedId
    }

    mandatedIdOk(userId: string): boolean {
        if (this._mandatedId === null) { return true }
        return this._mandatedId === userId
    }


}

export const currentUser = new CurrentUser()