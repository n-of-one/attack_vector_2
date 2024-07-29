export class Id {
    _id: string | null = null

    errorIfNotSet: string

    constructor(name: string) {
        this.errorIfNotSet = `${name}.id has not been set yet`
    }

    set id(userId: string) {
        this._id = userId
    }

    get id(): string {
        if (this._id === null) {
            throw Error(this.errorIfNotSet)
        }
        return this._id
    }

    isSet(): boolean {
        return this._id !== null
    }

    idOrEmptyString(): string {
        return this._id !== null ? this._id : ""
    }
}
