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
    LOCAL_DEV= "local development",
    FRONTIER = "Frontier",
    ATTACK_VECTOR_NL = "attackvector.nl",
    GENERIC = "generic",
}


export class Larp {

    type: LarpType = this.determineLarpType()

    name: string
    hackerShowSkills: boolean = false
    hackerEditSkills : boolean = false
    hackerEditUserName: boolean = true
    hackerEditCharacterName: boolean = true
    hackersResetSite: boolean = false
    hackersDeleteRunLinks: boolean = true
    hackersCreateSites: boolean = true
    loginUrl: string = "/login" // override in case there is no login page but an SSO redirect

    quickPlaying: boolean = false // set to true for quicker start of ice puzzles

    constructor() {
        console.log(`Configuration: ${this.type}`)

        if (this.type === LarpType.LOCAL_DEV) {
            this.name = "development"
            this.hackerEditSkills = true
            this.hackersResetSite = true
            this.hackerShowSkills = true

            this.quickPlaying = true
            return
        }

        if (this.type === LarpType.FRONTIER) {
            this.name = "frontier"
            this.loginUrl = "/login-frontier"
            this.hackerShowSkills = true
            return
        }

        if (this.type === LarpType.ATTACK_VECTOR_NL) {
            this.name = "attack_vector.nl"
            this.hackersResetSite = true
            this.hackersDeleteRunLinks = false
            this.hackerEditCharacterName = false
            this.hackerEditUserName = false
            this.hackersCreateSites = false
            return
        }

        // else

        this.name = "generic"
    }

    private determineLarpType(): LarpType {
        if (window.location.href.indexOf("eosfrontier.space") !== -1) return LarpType.FRONTIER
        if (window.location.href.indexOf("attackvector.nl") !== -1) return LarpType.ATTACK_VECTOR_NL
        if (window.location.href.indexOf("masm") !== -1) return LarpType.LOCAL_DEV // for ArkNoe02
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
