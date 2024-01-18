import React from "react";
import {Banner} from "../../login/Banner";
import {SilentLink} from "../../common/component/SilentLink";
import {WebsiteImage} from "../WebsiteImage";


export const WebsiteIcePassword = () => {

    return (<div className="container" data-bs-theme="dark">
            <Banner image={true}/>
            <div className="row">
                <div className="col-12 text">
                    <SilentLink href="/website/players"><>&lt; back</>
                    </SilentLink><br/>
                </div>
            </div>

            <div className="row">
                <div className="col-12">
                    <h3 className="muted">Password ICE</h3>
                    <p className="text">
                        <WebsiteImage path="/img/website/players/ice-password.png" alt="A picture of a password ICE puzzle"/><br/>
                        <br/>
                        Password ICE cannot be hacked via a mini-game. Instead, the hacker needs to learn or guess the password.
                        The password can either be found inside the site or in the game-world.<br/>
                        <br/>
                        This ICE can have a password hint. This can point hackers in the right direction.<br/>
                        <br/>
                        For example, the password hint here is "The name of my daughter". Maybe this name can be found in the HR database. Or it can be found in the
                        non-digital game world.
                    </p>
                    <h3 className="muted">ICE strength</h3>
                    <p className="text">
                        This ICE does not have a strength setting.
                    </p>
                    <h3 className="muted">Multiplayer</h3>
                    <p className="text">
                        This ICE is not inherently multi-player.<br/>
                        <br/>
                        But complex sites can have multiple passwords. It will help if a group of hackers work together to hack multiple parts of the site,
                        gather passwords for other hackers to proceed.<br/>
                        <br/>
                        Another possibility it that this ICE requires non-hacker players to retrieve the password somewhere else in-game, to allow the hackers to proceed.
                    </p>
                </div>
            </div>
        </div>
    )
}
