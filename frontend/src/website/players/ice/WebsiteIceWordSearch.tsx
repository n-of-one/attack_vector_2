import React from "react";
import {Banner} from "../../../login/Banner";
import {SilentLink} from "../../../common/component/SilentLink";
import {WebsiteImage} from "../../WebsiteImage";


export const WebsiteIceWordSearch = () => {

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
                    <h3 className="muted">Word search ICE</h3>
                    <p className="text">
                        <WebsiteImage path="/img/website/players/ice-wordsearch.png" alt="A picture of a word search ICE puzzle"/><br/>
                        <br/>
                        Word search ICE is the classic word search puzzle.<br/>
                        <br/>
                        The words can be horizontal, vertical or diagonal. The words can be read in any direction.<br/>
                        <br/>
                    </p>
                    <h3 className="muted">ICE strength</h3>
                    <p className="text">
                        If the ICE strength is weak, then the grid of letters is small and fewer words need to be found.<br/>
                        <br/>
                        If the ICE strength is strong, then the grid of letters is large and more words need to be found.
                    </p>
                    <h3 className="muted">Multiplayer</h3>
                    <p className="text">
                        Multiple players can look for words together on the same screen. This is the ICE that benefits most from other players helping
                        that are not hackers. Anyone can try to find words.<br/>
                        <br/>
                        To maximize multiplayer potential, a hacker can hook their laptop to a large screen or projector and let other players help.
                    </p>
                </div>
            </div>
        </div>
    )
}
