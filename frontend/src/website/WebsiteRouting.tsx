import React from "react";
import {WebsitePlayers} from "./players/WebsitePlayers";
import {useParams} from "react-router-dom";
import {WebsiteTerminalCommands} from "./players/WebsiteTerminalCommands";
import {WebsiteIceWordSearch} from "./players/WebsiteIceWordSearch";
import {WebsiteIceTangle} from "./players/WebsiteIceTangle";
import {WebsiteIceNetwalk} from "./players/WebsiteIceNetwalk";
import {WebsiteIcePassword} from "./players/WebsiteIcePassword";
import {WebsiteIceTar} from "./players/WebsiteIceTar";
import {WebsiteLayers} from "./players/WebsiteLayers";
import {WebsiteGms} from "./gms/WebsiteGms";
import {WebsiteOrganizers} from "./organizers/WebsiteOrganizers";
import {WebsiteLandingPage} from "./WebsiteLandingPage";

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
        case "gms":
            return <WebsiteGms/>
        case "organizers":
            return <WebsiteOrganizers/>
        default:
            return <WebsiteLandingPage/>
    }
}
