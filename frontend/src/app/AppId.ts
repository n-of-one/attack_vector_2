
export class AppId {
    _id: string | null = null

    set id(appId: string | undefined) {
        if (!appId) { throw Error("Missing app id, did you manually go to a URL?")}
        this._id = appId
    }

    get id(): string {
        if (this._id === null) { throw Error("app.id has not been set yet")}
        return this._id
    }
}

export const app = new AppId()