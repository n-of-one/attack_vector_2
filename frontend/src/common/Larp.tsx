/**
 * Holds values for the current larp
 */
import React from "react";
import {IceType} from "../standalone/ice/common/IceModel";
import {NETWALK_ICE, PASSWORD_ICE, TANGLE_ICE, TAR_ICE, WORD_SEARCH_ICE} from "./enums/LayerTypes";
import {DevLogin} from "../login/DevLogin";
import {LoggedOut} from "../login/LoggedOut";
import {GoogleAuth} from "../login/GoogleAuth";

export enum LarpType {
    LOCAL_DEV,
    FRONTIER,
    GENERIC
}


export class Larp {

    // type: LarpType = this.determineLarpType()
    type: LarpType = LarpType.GENERIC

    name: string
    userEditSkills : boolean
    userEditCharacterName: boolean
    loginUrl: string = "/login" // override in case there is no login page but an SSO redirect


    constructor() {
        if (this.type === LarpType.LOCAL_DEV) {
            this.name = "frontier"
            this.userEditSkills = true
            this.userEditCharacterName = true
            return
        }

        if (this.type === LarpType.FRONTIER) {
            this.name = "frontier"
            this.userEditSkills = false
            this.userEditCharacterName = true
            this.loginUrl = "/login/frontier"
            return
        }

        this.name = "generic"
        this.userEditSkills = false
        this.userEditCharacterName = true
    }

    private determineLarpType(): LarpType {
        if (window.location.href.indexOf("eosfrontier.space") !== -1) return LarpType.FRONTIER
        if (window.location.href.indexOf("localhost") !== -1) return LarpType.LOCAL_DEV
        return LarpType.GENERIC
    }

    loginElement(): React.JSX.Element {
        if (this.type === LarpType.FRONTIER) {
            return <LoggedOut/>
        }
        if (this.type === LarpType.LOCAL_DEV) {
            return <DevLogin/>
        }
        return <GoogleAuth/>
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
