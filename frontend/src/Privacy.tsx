import React from "react";
import {Banner} from "./login/Banner";


export const Privacy = () => {
    return (<div className="container" data-bs-theme="dark">
        <Banner/>
        <div className="row text">
            <div className="col-12">
                <h1>Privacy policy</h1>
                <p>
                    Attack Vector 2 is very privacy conscious and does not inherently collect or store any personal information. It does not use cookies to track users,
                    it does not store any personal information other than the information stored in there by the game masters.
                </p>
                <p>
                    It uses external identity providers for logon. Ideally it is tied to the event's website, but it can also use Google.
                </p>
                <h5>Login with larp website</h5>
                <p>
                    When using the website of the organizers, the identifier for your account is stored. Additional information on your character may be retrieved,
                    depending on the event. But this does not include personal information.
                </p>
                <h5>Login with Google</h5>
                <p>
                    When using Google login, Google returns a unique account id, along with the user's email, name and profile picture. The account id and email address
                    are used to create a unique user-id via a secure one-way hash. The name and profile picture are not stored or used anywhere.
                </p>
                <h5>Account names</h5>
                <p>
                    For players, the username should be a fictional name related to their character.
                </p>
                <p>
                    For game masters, the username will need to include some off-game identifier, possibly including their first or last name. It is up to the game master
                    to decide how to identify themselves.
                </p>
                <p>
                    In addition, when a game master creates a site, they need to provide information on who created this site, this can also include personal information like their name.
                </p>
            </div>
        </div>
    </div>)
}
