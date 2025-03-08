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
import {DataTable} from "../../../common/component/dataTable/DataTable";
import {Hr} from "../../../common/component/dataTable/Hr";
import {DropDownSaveInput} from "../../../common/component/DropDownSaveInput";
import {LayerType} from "../../../common/enums/LayerTypes";


export const ScriptTypeManagement = () => {
    const editTypeId = useSelector((state: GmRootState) => state.scriptsManagement.editTypeId)
    const editType = useSelector((state: GmRootState) => state.scriptsManagement.types.find(type => type.id === editTypeId))
    const dispatch = useDispatch()

    const mainElement = editType ? <ScriptTypeDetails scriptType={editType}/> : <ChooseOrCreateScriptType/>

    const selectScriptType = (scriptType: ScriptType) => {
        dispatch({type: EDIT_SCRIPT_TYPE, data: scriptType.id})
    }

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
                <div className="rightPanel">
                    <ScriptTypesTable onSelect={selectScriptType}/>
                </div>
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
        const editCommand = {scriptTypeId: scriptType.id, name: newValue, size: scriptType.size, defaultPrice: scriptType.defaultPrice}
        webSocketConnection.send("/gm/scriptType/edit", editCommand)
    }

    const editSize = (newValue: string) => {
        const editCommand = {scriptTypeId: scriptType.id, name: scriptType.name, size: newValue, defaultPrice: scriptType.defaultPrice}
        webSocketConnection.send("/gm/scriptType/edit", editCommand)
    }

    const editDefaultPrice = (newValue: string) => {
        const editCommand = {scriptTypeId: scriptType.id, name: scriptType.name, size: scriptType.size, defaultPrice: newValue}
        webSocketConnection.send("/gm/scriptType/edit", editCommand)
    }

    const addEffect = (effectType: EffectType) => {
        const command = {scriptTypeId: scriptType.id, effectType}
        webSocketConnection.send("/gm/scriptType/addEffect", command)
    }

    const deleteType = () => {
        webSocketConnection.send("/gm/scriptType/delete", scriptType.id)
    }

    return (<>
            <div className="d-flex flex-row justify-content-end"><CloseButton closeAction={close}/></div>
            <br/>
            <FormTextInputRow label="Name" value={scriptType.name} save={editName} labelColumns={2} valueColumns={2}/>
            <FormTextInputRow label="Size" value={scriptType.size.toString()} save={editSize} labelColumns={2} valueColumns={2}/>
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
                        <option value={EffectType.SITE_STATS}>Site stats</option>
                        <option value={EffectType.DELAY_TRIPWIRE_COUNTDOWN}>Delay tripwire countdown</option>
                        <option value={EffectType.SCAN_ICE_NODE}>Scan node with ICE</option>
                        <option value={EffectType.JUMP_TO_NODE}>Jump to node</option>
                        <option value={EffectType.JUMP_TO_HACKER}>Jump to hacker</option>
                        <option value={EffectType.SWEEPER_UNBLOCK}>Minesweeper - unblock</option>
                        <option value={EffectType.TANGLE_REVEAL_CLUSTERS}>Tangle - reveal clusters</option>
                        <option value={EffectType.WORD_SEARCH_NEXT_WORDS}>Word search - show next words</option>
                        <option value={EffectType.AUTO_HACK_SPECIFIC_ICE}>Automatically hack specific ICE type</option>
                        <option value={EffectType.AUTO_HACK_ANY_ICE}>Automatically hack any ICE</option>
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
                        <option value={EffectType.HIDDEN_EFFECTS}>Hidden effects</option>
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
            <br/>
            <div className="row">
                <div className="col-lg-10 text ">
                    <hr/>
                    <div className="btn btn-info textSize" onClick={deleteType}>Delete type</div>
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
            <EffectValue effect={effect} scriptType={scriptType}/>

            <div className="col-lg-1" style={{paddingTop: "9px"}}>
                <SilentLink onClick={deleteEffect} title="Remove effect">
                    <span className="glyphicon glyphicon-trash"/>
                </SilentLink>
            </div>
        </div>
    </>

}

const EffectValue = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    switch (effect.type) {
        case EffectType.HIDDEN_EFFECTS:
        case EffectType.SITE_STATS:
        case EffectType.AUTO_HACK_ANY_ICE:
        case EffectType.TANGLE_REVEAL_CLUSTERS:
            return <div className="col-lg-2"/>
        case EffectType.JUMP_TO_NODE:
        case EffectType.JUMP_TO_HACKER:
            return <EffectValueJumpBlockedType effect={effect} scriptType={scriptType}/>
        case EffectType.DECREASE_FUTURE_TIMERS:
        case EffectType.START_RESET_TIMER:
        case EffectType.SPEED_UP_RESET_TIMER:
        case EffectType.SCAN_ICE_NODE:
        case EffectType.SWEEPER_UNBLOCK:
        case EffectType.WORD_SEARCH_NEXT_WORDS:
            return <EffectValueText effect={effect} scriptType={scriptType}/>
        case EffectType.AUTO_HACK_SPECIFIC_ICE:
            return <EffectValueIceType effect={effect} scriptType={scriptType}/>
        default:
            return <div>Unknown effect type</div>
    }


}

const EffectValueText = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    return <div className="col-lg-2">
        <TextSaveInput className="form-control"
                       placeholder="" value={effect.value as string}
                       save={(value) => {
                           webSocketConnection.send("/gm/scriptType/editEffect", {"scriptTypeId": scriptType.id, "effectNumber": effect.effectNumber, value})
                       }}
                       readonly={false}/>
    </div>
}


const EffectValueJumpBlockedType = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    return <div className="col-lg-2">
        <DropDownSaveInput className="form-control"
                           selectedValue={effect.value as string}
                           save={(value) => {
                               webSocketConnection.send("/gm/scriptType/editEffect", {
                                   "scriptTypeId": scriptType.id,
                                   "effectNumber": effect.effectNumber,
                                   value
                               })
                           }}
                           readonly={false}>
            <>
                <option value="IGNORE_ICE">Ignoring ICE</option>
                <option value="BLOCKED_BY_ICE">Blocked by ICE</option>
            </>
        </DropDownSaveInput>
    </div>
}

const EffectValueIceType = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    return <div className="col-lg-2">
        <DropDownSaveInput className="form-control"
                           selectedValue={effect.value as string}
                           save={(value) => {
                               webSocketConnection.send("/gm/scriptType/editEffect", {
                                   "scriptTypeId": scriptType.id,
                                   "effectNumber": effect.effectNumber,
                                   value
                               })
                           }}
                           readonly={false}>
            <>
                <option value={LayerType.PASSWORD_ICE}>Password</option>
                <option value={LayerType.TANGLE_ICE}>Tangle</option>
                <option value={LayerType.WORD_SEARCH_ICE}>Word search</option>
                <option value={LayerType.NETWALK_ICE}>Netwalk</option>
                <option value={LayerType.TAR_ICE}>Tar</option>
                <option value={LayerType.SWEEPER_ICE}>Minesweeper</option>
            </>
        </DropDownSaveInput>
    </div>
}


interface ScriptTypesListProps {
    onSelect: (scriptType: ScriptType) => void
}

export const ScriptTypesTable = ({onSelect}: ScriptTypesListProps) => {
    const scriptTypes = useSelector((state: GmRootState) => state.scriptsManagement.types)

    const scriptTypeRows = scriptTypes.map(type => <ScriptTypeRow type={type} onSelect={onSelect}/>)
    const scriptTypeTexts = scriptTypes.map(type => `${type.name}~${type.size}~${type.defaultPrice}`)

    const hr = <Hr height={6} marginTop={3} color="black"/>


    return (<>
        <DataTable rows={scriptTypeRows} rowTexts={scriptTypeTexts} pageSize={35} hr={hr}>
            <div className="row text">
                <div className="col-lg-2 strong">Script type</div>
                <div className="col-lg-2 strong">Effects</div>
                <div className="col-lg-1 strong">Size</div>
                <div className="col-lg-2 strong">Default price</div>
            </div>

        </DataTable>
    </>)
}

const ScriptTypeRow = ({type, onSelect}: { type: ScriptType, onSelect: (scriptType: ScriptType) => void }) => {
    return (
        <div className="row text">
            <div className="col-lg-2"><SilentLink onClick={() => {
                onSelect(type)
            }} text={type.name}/>
            </div>
            <div className="col-lg-2">{
                type.effects.map((effect: Effect) => {
                    const text: string = effect.type === EffectType.HIDDEN_EFFECTS ? "*" : effect.effectNumber.toString()
                    return (<span key={effect.effectNumber}>
                        <InfoBadge infoText={effect.playerDescription} key={effect.effectNumber}
                                   badgeText={text} badgeClass={effect.hidden ? 'bg-dark' : undefined}/>
                        &nbsp;</span>)
                })
            }
            </div>
            <div className="col-lg-1">{type.size}</div>
            <div className="col-lg-2">{type.defaultPrice}</div>
        </div>)
}
