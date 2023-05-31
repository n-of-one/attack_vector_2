class CurrentUser {
    _id: string | null = null

    set id(userId: string) {
        this._id = userId
    }

    get id(): string {
        if (this._id === null) { throw Error("currentUser.id has not been set yet")}
        return this._id
    }



}

export const currentUser = new CurrentUser()