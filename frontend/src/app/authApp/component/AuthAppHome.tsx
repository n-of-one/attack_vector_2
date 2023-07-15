import React, {useState} from 'react'
import {useDispatch, useSelector} from "react-redux"
import {AuthAppRootState} from "../reducer/AuthAppRootReducer";
import {Glyphicon} from "../../../common/component/icon/Glyphicon";
import {larp} from "../../../common/Larp";
import {PASSWORD_ICE} from "../../../common/enums/LayerTypes";
import {SUBMIT_PASSWORD, UI_STATE_LOCKED, UI_STATE_SUBMITTING, UI_STATE_UNLOCKED} from "../reducer/AuthAppUiReducer";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {ice} from "../../../ice/IceModel";
import {avEncodedUrl} from "../../../common/util/Util";
import {app} from "../../AppId";

export const AuthAppHome = () => {

    const type = useSelector((root: AuthAppRootState) => root.info.type)
    const infoType = type === PASSWORD_ICE ? "Password" : "Passcode"

    return (
        <>
            <div className="row">
                <div className="col-lg-12">
                    <br/>
                    <br/>
                    <div className="d-flex justify-content-center">
                        <h2 className="text-info">
                            <strong>Authorization required</strong>
                        </h2>
                    </div>
                    <div className="d-flex justify-content-center text">
                        Automatic authorization failed
                    </div>
                    <br/>
                    <div className="d-flex justify-content-center text">
                        Please enter {infoType} to continue
                    </div>
                    <br/>
                </div>
            </div>
            <div className="row" style={{background: "#373c3c"}}>
                <div className="col-lg-12">
                    <hr style={{borderTopColor: "#300", marginTop: "5px", marginBottom: "5px"}}/>
                    <br/>
                    <div className="d-flex justify-content-center text-primary" style={{height: "42px"}}>
                        <PasswordSection/>
                    </div>
                    <div className="d-flex justify-content-center text-primary" style={{height: "42px"}}>
                        <HintSection />
                    </div>
                    <br/>
                    <hr style={{borderTopColor: "#300", marginTop: "5px", marginBottom: "5px"}}/>
                </div>
            </div>
            <div className="row">
                <div className="col-lg-12">
                    <br/>
                    <br/>
                    <div className="d-flex justify-content-center">
                        <h4 className="text-secondary">
                            <IceBanner />
                        </h4>
                    </div>
                </div>
            </div>
        </>
    )
}

const IceBanner = () => {
    const type = useSelector((root: AuthAppRootState) => root.info.type)
    const [clickCount, setClickCount] = useState(0)

    if (!type) return <></>

    const iceName = larp.iceName(type)
    const clickIceName = () => {
        const newClickCount = clickCount + 1
        setClickCount(newClickCount)
        if (newClickCount >= 3) {
            const url = avEncodedUrl(`app/${app.id}?hacking=true`)
            document.location.href = url
        }
    }
    return <>
        <strong>Protected by {iceName} </strong><span className="text-success unselectable">I<span onClick={clickIceName}>C</span>E</span>
    </>
}


const PasswordSection = () => {
    const state = useSelector((root: AuthAppRootState) => root.ui.state)

    switch (state) {
        case UI_STATE_UNLOCKED:
            return <PasswordInput/>
        case UI_STATE_SUBMITTING:
            return <PasswordDisabled/>
        case UI_STATE_LOCKED:
            return <PasswordLocked/>
    }
}

const PasswordInput = () => {
    const [password, setPassword] = React.useState("")
    const [showPassword, setShowPassword] = React.useState(false)
    const type = useSelector((root: AuthAppRootState) => root.info.type)
    const placeHolder = type === PASSWORD_ICE ? "password" : "passcode-xxxx-xxxx"
    const dispatch = useDispatch()

    const submit = () => {
        const payload = {iceId: ice.id, password: password, userType: "USER"}
        webSocketConnection.sendObject("/av/ice/password/submit", payload)
        dispatch({type: SUBMIT_PASSWORD})
    }

    return (
        <div style={{width: "300px"}}>
            <div className="input-group">
                <input type={showPassword ? "text" : "password"}
                       className="form-control" placeholder={placeHolder}
                       aria-label="Recipient's username"
                       aria-describedby="basic-addon2"
                       maxLength={19}
                       onChange={(e) => setPassword(e.target.value)}
                />
                <div className="input-group-append">

                    <button className="btn btn-outline-secondary ice-input-eye"
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}>
                        {showPassword ? <Glyphicon name="glyphicon glyphicon-eye-close"/> : <Glyphicon name="glyphicon glyphicon-eye-open"/>}

                    </button>
                    <button className="btn btn-outline-secondary ice-input-submit"
                            type="button"
                            onClick={() => {
                                submit()
                            }}>
                        <Glyphicon name="glyphicon glyphicon-arrow-right"/>
                    </button>
                </div>
            </div>
        </div>
    )
}

const PasswordDisabled = () => {
    return (
        <div style={{width: "300px"}}>

            <div className="input-group">
                <input type="password"
                       className="form-control" placeholder=""
                       aria-label="Recipient's username"
                       aria-describedby="basic-addon2"
                       maxLength={19}
                       disabled={true}
                />
                <div className="input-group-append">
                    <button className="btn btn-outline-secondary ice-input-eye" type="button" disabled={true}>
                        <Glyphicon name="glyphicon glyphicon-eye-close"/>
                    </button>
                    <button className="btn btn-outline-secondary ice-input-submit" type="button" disabled={true}>
                        <Glyphicon name="glyphicon glyphicon-arrow-right"/>
                    </button>
                </div>
            </div>
        </div>

    )
}

const PasswordLocked = () => {
    const waitSeconds = useSelector((root: AuthAppRootState) => root.ui.waitSeconds)

    return (
        <div className="text-muted">Invalid input. Please wait {waitSeconds} seconds before retrying</div>
    )
}

const HintSection = () => {
    const showHint = useSelector((root: AuthAppRootState) => root.ui.showHint)
    const hint = useSelector((root: AuthAppRootState) => root.info.hint)
    const state = useSelector((root: AuthAppRootState) => root.ui.state)

    if (state !== UI_STATE_UNLOCKED || !showHint) {
        return <div>&nbsp;</div>
    }
    return <div className="text-muted">Hint: {hint}</div>
}
