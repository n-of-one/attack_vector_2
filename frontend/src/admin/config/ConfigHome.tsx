import React, {useEffect, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {AdminRootState} from "../AdminRootReducer";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {
    ConfigItemHackerDefaultSpeed,
    ConfigItemHackerDeleteRunLinks,
    ConfigItemHackerEditCharacterName,
    ConfigItemHackerEditUserName,
    ConfigItemHackerScriptLoadDuringRun,
    ConfigItemHackerScriptLockoutDuration,
    ConfigItemHackerScriptRamRefreshDuration,
    ConfigItemHackerShowSKills,
    ConfigItemHackerTutorialSiteName
} from "./items/ConfigItemsHacker";
import {ConfigEntry, ConfigItem, ConfigItemCategories, ConfigItemNames, SELECT_CONFIG} from "./ConfigReducer";
import {SilentLink} from "../../common/component/SilentLink";
import {ConfigItemLoginGoogleClientId, ConfigItemLoginPassword, ConfigItemLoginPath} from "./items/ConfigItemsLogin";
import {
    ConfigItemDevSimulateNonLocalHost,
    ConfigItemHackerResetSite,
    ConfigItemHackerUseDevCommands,
    ConfigItemMinimumShutdownDuration,
    ConfigItemQuickPlaying,
    ConfigItemTestingMode
} from "./items/ConfigItemsDev";
import {ConfigItemLarpFrontierLolaEnabled, ConfigItemLarpFrontierOrthankToken, ConfigItemLarpName} from "./items/ConfigItemsLarp";


export const ConfigHome = () => {

    const configState = useSelector((state: AdminRootState) => state.config)

    useEffect(() => {
        webSocketConnection.sendWhenReady('/admin/config/get', null)
    }, []);

    const dispatch = useDispatch()

    const selectItem = (entry: ConfigEntry) => {
        dispatch({type: SELECT_CONFIG, entry: entry})
    }

    return (
        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div>
                    <h3 className="text-info">Configuration</h3><br/>
                    {configState.currentItem === null ? <div className="text">Select an item on the right, to edit it</div> : <></>}
                </div>
                <div>
                    <ConfigItemElement item={configState.currentItem} value={configState.currentValue}/>
                </div>

            </div>
            <div className="col-lg-6 rightPane">
                <div className="rightPanel">
                    <table className="table table-sm text-muted text" id="sitesTable">
                        <thead>
                        <tr>
                            <td className="strong">Group</td>
                            <td className="strong">Name</td>
                            <td className="strong">Value</td>
                        </tr>
                        </thead>
                        <tbody>
                        {
                            configState.entries.map((configEntry) => {
                                const category = ConfigItemCategories[configEntry.item]
                                const name = ConfigItemNames[configEntry.item]
                                return (
                                    <tr key={configEntry.item}>
                                        <td className="table-very-condensed">{category.substring(3)}</td>
                                        <td><SilentLink onClick={() => selectItem(configEntry)}><>{name}</>
                                        </SilentLink></td>
                                        <td className={highlightSelected(configEntry.item, configState.currentItem)}>{configEntry.value}</td>
                                    </tr>)
                            })
                        }
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    )
}

const highlightSelected = (item: ConfigItem, currentItem: ConfigItem | null) => {
    return (item === currentItem) ? "text-white" : ""
}


const ConfigItemElement = (props: { item: ConfigItem | null, value: string }) => {
    if (props.item === null) {
        return <></>
    }

    switch (props.item) {
        case ConfigItem.LARP_NAME:
            return <ConfigItemLarpName value={props.value}/>


        case ConfigItem.HACKER_SHOW_SKILLS:
            return <ConfigItemHackerShowSKills value={props.value}/>
        case ConfigItem.HACKER_EDIT_USER_NAME:
            return <ConfigItemHackerEditUserName value={props.value}/>
        case ConfigItem.HACKER_EDIT_CHARACTER_NAME:
            return <ConfigItemHackerEditCharacterName value={props.value}/>
        case ConfigItem.HACKER_DELETE_RUN_LINKS:
            return <ConfigItemHackerDeleteRunLinks value={props.value}/>
        case ConfigItem.HACKER_TUTORIAL_SITE_NAME:
            return <ConfigItemHackerTutorialSiteName value={props.value}/>
        case ConfigItem.HACKER_SCRIPT_RAM_REFRESH_DURATION:
            return <ConfigItemHackerScriptRamRefreshDuration value={props.value}/>
        case ConfigItem.HACKER_SCRIPT_LOCKOUT_DURATION:
            return <ConfigItemHackerScriptLockoutDuration value={props.value}/>
        case ConfigItem.HACKER_SCRIPT_LOAD_DURING_RUN:
            return <ConfigItemHackerScriptLoadDuringRun value={props.value}/>
        case ConfigItem.HACKER_DEFAULT_SPEED:
            return <ConfigItemHackerDefaultSpeed value={props.value}/>

        case ConfigItem.LOGIN_PATH:
            return <ConfigItemLoginPath value={props.value}/>
        case ConfigItem.LOGIN_PASSWORD:
            return <ConfigItemLoginPassword value={props.value}/>
        case ConfigItem.LOGIN_GOOGLE_CLIENT_ID:
            return <ConfigItemLoginGoogleClientId value={props.value}/>

        case ConfigItem.DEV_SIMULATE_NON_LOCALHOST_DELAY_MS:
            return <ConfigItemDevSimulateNonLocalHost value={props.value}/>
        case ConfigItem.DEV_HACKER_RESET_SITE:
            return <ConfigItemHackerResetSite value={props.value}/>
        case ConfigItem.DEV_MINIMUM_SHUTDOWN_DURATION:
            return <ConfigItemMinimumShutdownDuration value={props.value}/>
        case ConfigItem.DEV_TESTING_MODE:
            return <ConfigItemTestingMode value={props.value}/>
        case ConfigItem.DEV_HACKER_USE_DEV_COMMANDS:
            return <ConfigItemHackerUseDevCommands value={props.value}/>
        case ConfigItem.DEV_QUICK_PLAYING:
            return <ConfigItemQuickPlaying value={props.value}/>

        case ConfigItem.LARP_SPECIFIC_FRONTIER_ORTHANK_TOKEN:
            return <ConfigItemLarpFrontierOrthankToken value={props.value}/>
        case ConfigItem.LARP_SPECIFIC_FRONTIER_LOLA_ENABLED:
            return <ConfigItemLarpFrontierLolaEnabled value={props.value}/>

        default:
            return <h3 className="text-danger">Unknown config item: {props.item}</h3>
    }
}


export const ConfigItemText = (props: { name: string, value: string, item: string }) => {

    const [currentValue, setCurrentValue] = useState(props.value)

    const save = () => {
        webSocketConnection.send("/admin/config/set", {item: props.item, value: currentValue})
    }

    return (
        <>
            <div className="form-group text-size">
                <label htmlFor="exampleInputEmail1" className="text-white">{props.name}</label><br/><br/>
                <input type="text" className="form-control" id="configItemText" value={currentValue} onChange={(event) => setCurrentValue(event.target.value)}/><br/>
                <button type="button" className="btn btn-primary btn-sm text-size" onClick={save}>Save</button>
            </div>
            <br/>
            <hr className="thin-hr"/>
        </>
    )
}
