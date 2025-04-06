import React from "react";
import {WebsitePlayers} from "./players/WebsitePlayers";
import {useParams} from "react-router-dom";
import {WebsiteTerminalCommands} from "./players/WebsiteTerminalCommands";
import {WebsiteIceWordSearch} from "./players/ice/WebsiteIceWordSearch";
import {WebsiteIceTangle} from "./players/ice/WebsiteIceTangle";
import {WebsiteIceNetwalk} from "./players/ice/WebsiteIceNetwalk";
import {WebsiteIcePassword} from "./players/ice/WebsiteIcePassword";
import {WebsiteIceTar} from "./players/ice/WebsiteIceTar";
import {WebsiteLayers} from "./players/WebsiteLayers";
import {WebsiteGms} from "./gms/WebsiteGms";
import {WebsiteOrganizers} from "./organizers/WebsiteOrganizers";
import {WebsiteLandingPage} from "./WebsiteLandingPage";
import {WebsiteIceMineSweeper} from "./players/ice/WebsiteIceMineSweeper";
import {WebsiteSkills} from "./players/skill/WebsiteSkills";
import {WebsitePlayerScripts} from "./players/WebsitePlayerScripts";

export const WebsiteRouting = () => {

    const {path} = useParams() // encodedParam example: 'aWJnLGpqYmIlOWg6biA6OnJwKH91bHNlNXoqfSQia2xFUx9WV0BUCkocExoBGUgXAQ=='

    switch (path) {
        case "players":
            return <WebsitePlayers/>
        case "players-commands":
            return <WebsiteTerminalCommands/>
        case "players-layers":
            return <WebsiteLayers/>
        case "players-ice-word-search":
            return <WebsiteIceWordSearch/>
        case "players-ice-tangle":
            return <WebsiteIceTangle/>
        case "players-ice-netwalk":
            return <WebsiteIceNetwalk/>
        case "players-ice-password":
            return <WebsiteIcePassword/>
        case "players-ice-tar":
            return <WebsiteIceTar/>
        case "players-ice-minesweeper":
            return <WebsiteIceMineSweeper/>
        case "players-skills":
            return <WebsiteSkills/>
        case "players-scripts":
            return <WebsitePlayerScripts/>
        case "gms":
            return <WebsiteGms/>
        case "organizers":
            return <WebsiteOrganizers/>
        default:
            return <WebsiteLandingPage/>
    }
}
