/**
 * Holds values for the current larp
 */
import {IceType} from "../standalone/ice/common/IceModel";
import {NETWALK_ICE, PASSWORD_ICE, TANGLE_ICE, TAR_ICE, WORD_SEARCH_ICE} from "./enums/LayerTypes";

export class Larp {
    name: string = "generic"
    userEmailEnabled = true
    userEditSkills = true
    userEditCharacterName = true
    loginUrl = "/login"

    constructor() {
        const frontier = window.location.href.indexOf("eosfrontier.space") !== -1
        const local = window.location.href.indexOf("localhost") !== -1

        if (local) {
            this.name = "frontier"
            this.userEmailEnabled = false
            this.userEditSkills = true
            this.userEditCharacterName = true
            this.loginUrl = "/login"
        }

        if (frontier) {
            this.name = "frontier"
            this.userEmailEnabled = false
            this.userEditSkills = false
            this.userEditCharacterName = true
            this.loginUrl = "/login/frontier"
        }
    }

    iceName(type?: IceType): string {
        switch (type) {
            case PASSWORD_ICE:
                return "Rahasy"
            case TANGLE_ICE:
                return "Gaanth"
            case NETWALK_ICE:
                return "Sanrachana"
            case WORD_SEARCH_ICE:
                return "Jaal"
            case TAR_ICE:
                return "Tar"
            default:
                return ""

        }

    }

}

export const larp = new Larp()
