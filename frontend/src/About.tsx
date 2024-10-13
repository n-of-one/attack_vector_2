import React from "react";
import {Banner} from "./login/Banner";

export const Link = ({href}: { href: string }) => {
    return (<a href={href} target="_blank">{href}</a>)
}

/* eslint react/jsx-no-target-blank  : 0*/

export const About = () => {
    return (<div className="container" data-bs-theme="dark">
        <Banner image={true}/>
        <div className="row">
            <div className="col-12">
                <h3 className="muted">Links to website</h3>
            </div>
        </div>
        <div className="row">
            <div className="col-12">
                <h3 className="muted">
                    <div className="btn btn-primary" onClick={() => window.location.href = "/website/players"}>Info for player</div>
                    &nbsp;
                    <div className="btn btn-primary" onClick={() => window.location.href = "/website/gms"}>Info for GM</div>
                    &nbsp;
                    <div className="btn btn-primary" onClick={() => window.location.href = "/website/organizers"}>Info for organizer</div>
                    &nbsp;

                </h3>
            </div>
        </div>

        <div className="row text">
            <div className="col-12">
                <h3>About</h3>
                <p>
                    Attack Vector 2 is a game that simulates hacking. It is designed to be used as part of a Live Action Role Playing (LARP) event.
                </p>

                <p>
                    The game aims to do the following:
                </p>
                <ul>
                    <li>Make players to feel like hackers, even if they know nothing about real-world hacking</li>
                    <li>Stimulate cooperative play between hackers</li>
                    <li>Provide ways to interact with the non-computer world</li>
                </ul>
                <p>
                    The setting is a nondescript science fiction setting. Hacker character hack 'sites' that are protected by 'ICE'. The hacking takes the form
                    of
                    solving
                    puzzles like Word-search, Network jigsaw and Untangle. The sites form meta-puzzles where there can be timers to beat, passwords to find or
                    other
                    higher-level puzzles to solve.</p>
                <p>Sites are created by Game masters, who have an visual editor and other tools to create and maintain sites for use in-game.</p>
                <br/>
                <h3>Privacy policy</h3>
                <p>
                    Link to the <a href="/privacy" target="_blank">privacy policy</a>.
                </p>
                <p>
                    Attack Vector 2 is very privacy conscious and aims to not collect or store any personal information. To that end, no usernames, email
                    addresses or
                    passwords are stored.
                    Login is done via an external system, such as your Larp's website or Google. When Google is used, only a one-way hash of the google-id is
                    stored, not
                    the email address, name or profile picture.
                </p>
                <br/>
                <h3>Open source</h3>
                <p>
                    Attack Vector 2 is open source. The source code is available at <a href="https://github.com/n-of-one/attack_vector_2"
                                                                                       target="_blank">https://github.com/n-of-one/attack_vector_2</a>.
                    If you want to know more, have found a <a href="https://github.com/n-of-one/attack_vector_2/issues" target="_blank">bug</a>, or want help to
                    use it in
                    your event, please visit the github page.
                </p>
                <br/>
                <h3>Attribution</h3>
                <p>
                    Attack Vector 2 is built using many other open source projects, and public domain images.<br/>
                    <br/>
                </p>
                <h6>Concepts</h6>
                <ul>
                    <li>Untangle - Simon Tatham's - <Link href="https://www.chiark.greenend.org.uk/~sgtatham/puzzles/js/untangle.html"/></li>
                    <li>Netwalk - Simon Tatham's - <Link href="https://www.chiark.greenend.org.uk/~sgtatham/puzzles/js/net.html"/></li>
                </ul>
                <h6> Graphics </h6>
                <ul>
                    <li>Background Untangle ICE - Image by Pete Linforth from Pixabay - <Link href="https://pixabay.com/users/thedigitalartist-202249/"/></li>
                    <li>Background Sanrachana (Netwalk) - Image by Piro from Pixabay - <Link href="https://pixabay.com/users/piro4d-2707530/"/></li>
                    <li>In game icons - My site my way & Icons etc. - links no longer work: http://icons.mysitemyway.com & http://iconsetc.com</li>
                    <li>Artwork for Netwalk ICE game - anonymous</li>
                    <li>Glyphicons Halflings from Bootstrap - Jan Kovařík - <License name="MIT" href="https://github.com/twbs/bootstrap/blob/main/LICENSE"/>
                    </li>
                    <li>Tabler Icons - ICONFINDER - <Link href="https://www.iconfinder.com/search/icons?family=tabler"/> <License name="CC BY 4.0"
                                                                                                                                  href="https://creativecommons.org/licenses/by/4.0/"/>
                    </li>
                    <li>Photo Attack Vector on Frontier 17 - Hephaestus Aperture <Link href="https://www.hephaestus-aperture.com"/></li>
                </ul>
                <h6> Javascript libraries </h6>
                <pre>
bootstrap                                          MIT           https://github.com/twbs/bootstrap.git                       The Bootstrap Authors (https://github.com/twbs/bootstrap/graphs/contributors)<br/>
fabric                                             MIT           https://github.com/fabricjs/fabric.js.git                   Juriy Zaytsev &lt;kangax@gmail.com&gt;
                    <br/>
js-cookie                                          MIT           https://github.com/js-cookie/js-cookie.git                  Klaus Hartl<br/>
license-checker                                    BSD-3-Clause  https://github.com/davglass/license-checker.git             Dav Glass &lt;davglass@gmail.com&gt;
                    <br/>
react                                              MIT           https://github.com/facebook/react.git                       Facebook<br/>
react-bootstrap                                    MIT           https://github.com/react-bootstrap/react-bootstrap.git      Stephen J. Collings stevoland@gmail.com<br/>
react-dom                                          MIT           https://github.com/facebook/react.git                       Facebook<br/>
react-hot-toast                                    MIT           https://github.com/timolins/react-hot-toast.git             Timo Lins<br/>
react-qr-code                                      MIT           https://github.com/rosskhanas/react-qr-code.git             Ross Khanas rtkhanas@gmail.com<br/>
react-redux                                        MIT           https://github.com/reduxjs/react-redux.git                  Dan Abramov &lt;dan.abramov@me.com&gt; (https://github.com/gaearon)<br/>
react-router-dom                                   MIT           https://github.com/remix-run/react-router.git               Remix Software &lt;hello@remix.run&gt;
                    <br/>
redux                                              MIT           https://github.com/reduxjs/redux.git                        Dan Abramov<br/>
typescript                                         Apache-2.0    https://github.com/Microsoft/TypeScript.git                 Microsoft Corp.<br/>
webstomp-client                                    Apache-2.0    https://github.com/JSteunou/webstomp-client.git             Jérôme Steunou<br/>
@babel/plugin-proposal-private-property-in-object  MIT           https://github.com/babel/babel.git                          The Babel Team (https://babel.dev/team)<br/>
@react-oauth/google                                MIT           https://github.com/MomenSherif/react-oauth.git              Mo'men Sherif momensherif.2019@gmail.com https://github.com/MomenSherif<br/>
@reduxjs/toolkit                                   MIT           https://github.com/reduxjs/redux-toolkit.git                Mark Erikson &lt;mark@isquaredsoftware.com&gt;
                    <br/>
@types/...                                         MIT           https://github.com/DefinitelyTyped/DefinitelyTyped.git      various<br/>
react-scripts                                      MIT           https://github.com/facebook/create-react-app.git            Facebook<br/>
redux-devtools-extension                           MIT           https://github.com/zalmoxisus/redux-devtools-extension.git  Mihail Diordiev &lt;zalmoxisus@gmail.com&gt; (https://github.com/zalmoxisus)<br/>
                </pre>
                <h6>Java libraries</h6>
                <pre>
kotlin                                             Apache-2.0    https://github.com/JetBrains/kotlin                          Jetbrains<br/>
springframework                                    Apache-2.0    https://github.com/spring-projects                           VMware<br/>
logback                                            LGPL 2.1      https://github.com/qos-ch/logback                            QOS.ch Sarl<br/>
io.jsonwebtoken                                    Apache-2.0    https://github.com/jwtk/jjwt                                 Les Hazlewood<br/>
kotlin-logging-jvm                                 Apache-2.0    https://github.com/oshai/kotlin-logging                      <br/>
                </pre>
            </div>
        </div>
    </div>)
}

export const License = ({name, href}: { name: string, href: string }) => {
    return (<span className="badge bg-primary"><a href={href} target="_blank">{name}</a></span>)
}
