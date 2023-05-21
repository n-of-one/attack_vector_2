/**
 * Holds values for the current larp
 */

export class Larp {
    name: string = "generic"
    userEmailEnabled = true
    userEditSkills = true
    userEditCharacterName = true
    loginUrl = "/login"

    constructor() {
        const frontier = window.location.href.indexOf("eosfrontier.space") !== -1

        if (frontier) {
            this.name = "frontier"
            this.userEmailEnabled = false
            this.userEditSkills = false
            this.userEditCharacterName = false
            this.loginUrl = "/sso"
        }
    }

}

export const larp = new Larp()
