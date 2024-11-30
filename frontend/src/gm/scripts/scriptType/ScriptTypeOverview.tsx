import {InfoBadge} from "../../../common/component/ToolTip";
import React from "react";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {TextSaveInput} from "../../../common/component/TextSaveInput";
import {SilentLink} from "../../../common/component/SilentLink";
import {ActionButton} from "../../../common/component/ActionButton";
import {FormTextInputRow} from "../../../common/component/FormTextInputRow";
import {TextInput} from "../../../common/component/TextInput";
import {CLOSE_SCRIPT_TYPE, EDIT_SCRIPT_TYPE, Effect, EffectType, ScriptType} from "./ScriptTypeReducer";
import {useDispatch, useSelector} from "react-redux";
import {GmRootState} from "../../GmRootReducer";
import {CloseButton} from "../../../common/component/CloseButton";


export const ScriptTypeOverview = () => {
    const editTypeId = useSelector((state: GmRootState) => state.scriptsManagement.editTypeId)
    const editType = useSelector((state: GmRootState) => state.scriptsManagement.types.find(type => type.id === editTypeId))

    const mainElement = editType ? <ScriptTypeDetails scriptType={editType}/> : <ChooseOrCreateScriptType/>

    return (
        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">Manage script types </h3><br/>
                </div>
                {mainElement}
            </div>

            <div className="col-lg-6 rightPane rightPane">
                <ScriptTypesList/>
            </div>
        </div>
    )
}

const ChooseOrCreateScriptType = () => {

    const add = (name: string) => {
        webSocketConnection.send("/gm/scriptType/add", name)
    }

    return (<>
        <br/>
        <div className="text">
            Create new script type<br/>
            <br/>
        </div>
        <div className="col-lg-8">
            <TextInput placeholder="type name"
                       buttonLabel="Create"
                       buttonClass="btn-info"
                       save={add}
                       clearAfterSubmit={true}/>
        </div>

    </>)
}


const ScriptTypeDetails = ({scriptType}: { scriptType: ScriptType }) => {

    const dispatch = useDispatch()
    const close = () => {
        dispatch({type: CLOSE_SCRIPT_TYPE})
    }
    const [positiveEffect, setPositiveEffect] = React.useState<string>("")
    const [negativeEffect, setNegativeEffect] = React.useState<string>("")


    const editName = (newValue: string) => {
        const editCommand = {scriptTypeId: scriptType.id, name: newValue, ram: scriptType.ram, defaultPrice: scriptType.defaultPrice}
        webSocketConnection.send("/gm/scriptType/edit", editCommand)
    }

    const editRam = (newValue: string) => {
        const editCommand = {scriptTypeId: scriptType.id, name: scriptType.name, ram: newValue, defaultPrice: scriptType.defaultPrice}
        webSocketConnection.send("/gm/scriptType/edit", editCommand)
    }

    const editDefaultPrice = (newValue: string) => {
        const editCommand = {scriptTypeId: scriptType.id, name: scriptType.name, ram: scriptType.ram, defaultPrice: newValue}
        webSocketConnection.send("/gm/scriptType/edit", editCommand)
    }

    const addEffect = (effectType: EffectType) => {
        const command = {scriptTypeId: scriptType.id, effectType}
        webSocketConnection.send("/gm/scriptType/addEffect", command)
    }

    return (<>
            <div className="d-flex flex-row justify-content-end"><CloseButton closeAction={close}/></div>
            <br/>
            <FormTextInputRow label="Name" value={scriptType.name} save={editName} labelColumns={2} valueColumns={4}/>
            <FormTextInputRow label="RAM" value={scriptType.ram.toString()} save={editRam} labelColumns={2} valueColumns={2}/>
            <FormTextInputRow label="Default price" value={scriptType.defaultPrice?.toString() || ""} save={editDefaultPrice} labelColumns={2}
                              valueColumns={2}/>
            <br/>
            <br/>
            <div className="row text">
                <div className="col-lg-2"><strong>Effects</strong></div>
            </div>
            <EffectList scriptType={scriptType}/>
            <br/>
            <br/>
            <div className="row text">
                <div className="col-lg-2"><strong>Add effects</strong></div>
            </div>
            <br/>
            <div className="row form-group text">
                <div className="colg-lg-3"/>
                <label htmlFor="addEffect" className="col-lg-3 control-label text-muted">Useful effect:</label>
                <div className="col-lg-6">
                    <select className="form-control" value={positiveEffect}
                            onChange={(event: React.ChangeEvent<HTMLSelectElement>) => {
                                setPositiveEffect(event.target.value)
                            }}>
                        <option value=""></option>
                        <option value={EffectType.DELAY_TRIPWIRE_COUNTDOWN}>Delay tripwire countdown</option>
                        <option value={EffectType.SCAN_ICE_NODE}>Scan node with ICE</option>
                    </select>
                </div>
                <div className="col-lg-1">
                    <ActionButton text="Add" onClick={() => {
                        if (positiveEffect) {
                            addEffect(positiveEffect as EffectType)
                            setPositiveEffect("")
                        }
                    }}/>
                </div>
            </div>
            <br/>
            <div className="row form-group text">
                <div className="colg-lg-3"/>
                <label htmlFor="addEffect" className="col-lg-3 control-label text-muted">Drawback effect:</label>
                <div className="col-lg-6">
                    <select className="form-control" value={negativeEffect}
                            onChange={(event: React.ChangeEvent<HTMLSelectElement>) => {
                                setNegativeEffect(event.target.value)
                            }}>
                        <option value=""></option>
                        <option value={EffectType.DECREASE_FUTURE_TIMERS}>Decrease future timers</option>
                        <option value={EffectType.START_RESET_TIMER}>Start reset timer</option>
                        <option value={EffectType.SPEED_UP_RESET_TIMER}>Speed up reset timer</option>
                    </select>
                </div>
                <div className="col-lg-1">
                    <ActionButton text="Add" onClick={() => {
                        if (negativeEffect) {
                            addEffect(negativeEffect as EffectType)
                            setNegativeEffect("")
                        }
                    }}/>
                </div>
            </div>
        </>
    )
}

const EffectList = (props: { scriptType: ScriptType }) => {
    return <>
        {props.scriptType.effects.map(effect => <EffectLine scriptType={props.scriptType} effect={effect} key={effect.effectNumber}/>)}
        <div className="row text">
            <div className="col-lg-10">
                <hr/>
            </div>
        </div>
    </>
}

const EffectLine = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {

    const deleteEffect = () => {
        webSocketConnection.send("/gm/scriptType/deleteEffect", {"scriptTypeId": scriptType.id, "effectNumber": effect.effectNumber})
    }

    const editValue = effect.value ? (<div className="col-lg-2">
        <TextSaveInput className="form-control"
                       placeholder="" value={effect.value}
                       save={(value) => {
                           webSocketConnection.send("/gm/scriptType/editEffect", {"scriptTypeId": scriptType.id, "effectNumber": effect.effectNumber, value})
                       }}
                       readonly={false}/>
    </div>) : <div className="col-lg-2"/>


    return <>
        <div className="row text">
            <div className="col-lg-10">
                <hr/>
            </div>
        </div>
        <div className="row form-group text">
            <div className="col-lg-1" style={{marginTop: "0", marginRight: "-30px"}}>
                <InfoBadge infoText={effect.gmDescription} placement="top"/>
            </div>

            <div className={`col-lg-6`}>
                {effect.name}
            </div>
            {editValue}

            <div className="col-lg-1" style={{paddingTop: "9px"}}>
                <SilentLink onClick={deleteEffect} title="Remove effect">
                    <span className="glyphicon glyphicon-trash"/>
                </SilentLink>
            </div>
        </div>
    </>

}

const ScriptTypesList = () => {

    const scriptTypes = useSelector((state: GmRootState) => state.scriptsManagement.types)
    const dispatch = useDispatch()
    const selectScriptType = (scriptType: ScriptType) => {
        dispatch({type: EDIT_SCRIPT_TYPE, data: scriptType.id})
    }

    return (
        <div className="siteMap">
            <table className="table table-sm text-muted text" id="sitesTable">
                <thead>
                <tr>
                    <td className="strong">Script type</td>
                    <td className="strong">Effects</td>
                    <td className="strong">RAM</td>
                    <td className="strong">Default price</td>
                </tr>
                </thead>
                <tbody>
                {
                    scriptTypes.map((type: ScriptType) => {
                        return (
                            <tr key={type.id}>
                                <td><SilentLink onClick={() => {
                                    selectScriptType(type)
                                }} text={type.name}/></td>
                                <td>{
                                    type.effects.map((effect: Effect) => {
                                        return (<>
                                            <InfoBadge infoText={effect.playerDescription} key={effect.effectNumber}
                                                       badgeText={effect.effectNumber.toString()}/>
                                            &nbsp;</>)
                                    })
                                }
                                </td>
                                <td>{type.ram}</td>
                                <td>{type.defaultPrice}</td>
                            </tr>)
                    })
                }
                </tbody>
            </table>
        </div>
    )
}
