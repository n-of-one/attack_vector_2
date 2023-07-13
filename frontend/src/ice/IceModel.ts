import {Id} from "../common/model/Id";

export const HIDDEN = "HIDDEN";
export const VISIBLE = "VISIBLE";

export const ice = new Id("ice")

export type IceType = "PASSWORD_ICE" | "TAR_ICE" | "NETWALK_ICE" | "WORD_SEARCH_ICE" | "TANGLE_ICE"

