import {InfoBadge} from "../../../common/component/ToolTip";
import React from "react";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {SilentLink} from "../../../common/component/SilentLink";
import {ActionButton} from "../../../common/component/ActionButton";
import {FormTextInputRow} from "../../../common/component/FormTextInputRow";
import {TextInput} from "../../../common/component/TextInput";
import {CLOSE_SCRIPT_TYPE, EDIT_SCRIPT_TYPE, Effect, EffectType, ScriptType} from "../../../common/script/type/ScriptTypeReducer";
import {useDispatch, useSelector} from "react-redux";
import {GmRootState} from "../../GmRootReducer";
import {CloseButton} from "../../../common/component/CloseButton";
import {DataTable} from "../../../common/component/dataTable/DataTable";
import {Hr} from "../../../common/component/dataTable/Hr";
import {EffectConfigurationLine} from "./ScriptEffectConfiguration";


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

interface ScriptTypesListProps {
    onSelect: (scriptType: ScriptType) => void
}

export const ScriptTypesTable = ({onSelect}: ScriptTypesListProps) => {
    const scriptTypes = useSelector((state: GmRootState) => state.scriptsManagement.types)

    const scriptTypeRows = scriptTypes.map(type => <ScriptTypeRow type={type} onSelect={onSelect}/>)
    const scriptTypeTexts = scriptTypes.map(type => `${type.name}~${type.category}~${type.size}~${type.defaultPrice}`)

    const hr = <Hr height={6} marginTop={3} color="black"/>


    return (<>
        <DataTable rows={scriptTypeRows} rowTexts={scriptTypeTexts} pageSize={34} hr={hr}>
            <div className="row text">
                <div className="col-lg-2 strong">Script type</div>
                <div className="col-lg-2 strong">Effects</div>
                <div className="col-lg-1 strong">Size</div>
                <div className="col-lg-2 strong">Default price</div>
                <div className="col-lg-2 strong">Category</div>
            </div>

        </DataTable>
    </>)
}

const ScriptTypeRow = ({type, onSelect}: { type: ScriptType, onSelect: (scriptType: ScriptType) => void }) => {
    return (
        <div className="row text" data-row="script">
            <div className="col-lg-2"><SilentLink onClick={() => {
                onSelect(type)
            }} text={type.name}/>
            </div>
            <div className="col-lg-2" data-element="effect-descriptions">{
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
            <div className="col-lg-2">{type.category}</div>
        </div>)
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
        <div className="col-lg-8 text-size">
            <TextInput name="create"
                       placeholder="type name"
                       buttonLabel="Create"
                       buttonClass="btn-info"
                       save={add}
                       clearAfterSubmit={true}/>
        </div>

    </>)
}

const numericInputDefault1 = (value: string) => {
    if (/^\d+$/.test(value)) return value
    return "1"
}

const textInput = (value: string) => value

const ScriptTypeDetails = ({scriptType}: { scriptType: ScriptType }) => {

    const dispatch = useDispatch()
    const close = () => {
        dispatch({type: CLOSE_SCRIPT_TYPE})
    }

    const [positiveEffect, setPositiveEffect] = React.useState<string>("")
    const [negativeEffect, setNegativeEffect] = React.useState<string>("")


    const edit = (property: string, value: string, inputFilter: (value: string) => string) => {
        const filteredValue = inputFilter(value)

        const editCommand: { [key: string]: any } = {
            scriptTypeId: scriptType.id,
            name: scriptType.name,
            category: scriptType.category,
            size: scriptType.size,
            defaultPrice: scriptType.defaultPrice,
            gmNote: scriptType.gmNote,
        }
        editCommand[property] = filteredValue
        webSocketConnection.send("/gm/scriptType/edit", editCommand)
    }

    const addEffect = (effectType: EffectType) => {
        const command = {scriptTypeId: scriptType.id, effectType}
        webSocketConnection.send("/gm/scriptType/addEffect", command)
    }

    return (<>
            <div className="d-flex flex-row justify-content-end"><CloseButton closeAction={close}/></div>
            <br/>
            <FormTextInputRow label="Name" value={scriptType.name} save={value => edit("name", value, textInput)}
                              labelColumns={3} valueColumns={3} maxLength={15}
                              toolTip="This is what the scripts will be called. This is seen by the hacker. Max length is 15 characters."/>
            <FormTextInputRow label="Category" value={scriptType.category} save={value => edit("category", value, textInput)}
                              labelColumns={3} valueColumns={3}
                              toolTip="Optional category. This can be used to organize scripts, and filter for them in the scripts panel."/>
            <FormTextInputRow label="Size" value={scriptType.size.toString()} save={value => edit("size", value, numericInputDefault1)}
                              labelColumns={3} valueColumns={2}
                              toolTip="This is the amount of RAM the script will take up. Set to 0 if you are not using RAM."/>
            {/*<FormTextInputRow label="Default price" value={scriptType.defaultPrice?.toString() || ""}*/}
            {/*                  save={value => edit("defaultPrice", value, numericInputDefault0)}*/}
            {/*                  labelColumns={3} valueColumns={2}*/}
            {/*                  toolTip="This is the default price for hackers to buy this script. (currently not yet implemented). You can override*/}
            {/*                  this for each individual hacker."/>*/}
            <FormTextInputRow label="Note" value={scriptType.gmNote} save={value => edit("gmNote", value, textInput)} labelColumns={3} valueColumns={8}
                              toolTip="This note is for GM's only, it is not shown to the hackers. It can be used to keep track of why this script type exists."/>
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
            <div className="row form-group text" data-row="add-effect">
                <label htmlFor="addEffect" className="col-lg-3 control-label text-muted">Useful effect:</label>
                <div className="col-lg-7">
                    <select className="form-control" value={positiveEffect}
                            onChange={(event: React.ChangeEvent<HTMLSelectElement>) => {
                                setPositiveEffect(event.target.value)
                            }}>
                        <option value=""></option>
                        <option value={EffectType.AUTO_HACK_SPECIFIC_ICE_LAYER}>Automatically hack a specific ICE layer</option>
                        <option value={EffectType.AUTO_HACK_ANY_ICE}>Automatically hack any ICE</option>
                        <option value={EffectType.AUTO_HACK_ICE_TYPE}>Automatically hack ICE of a specific type</option>
                        <option value={EffectType.AUTO_HACK_ICE_BY_STRENGTH}>Automatically hack ICE with low enough strength</option>
                        <option value={EffectType.DELAY_TRIPWIRE_COUNTDOWN}>Delay tripwire countdown</option>
                        <option value={EffectType.HACK_BELOW_NON_HACKED_ICE}>Hack below non-hacked ICE</option>
                        <option value={EffectType.INTERACT_WITH_SCRIPT_LAYER}>Interact with script layer</option>
                        <option value={EffectType.JUMP_TO_NODE}>Jump to node</option>
                        <option value={EffectType.JUMP_TO_HACKER}>Jump to hacker</option>
                        <option value={EffectType.SWEEPER_UNBLOCK}>Minesweeper - unblock</option>
                        <option value={EffectType.ROTATE_ICE}>Rotate ICE - change ICE type</option>
                        <option value={EffectType.SCAN_ICE_NODE}>Scan node with ICE</option>
                        <option value={EffectType.SHOW_MESSAGE}>Show message</option>
                        <option value={EffectType.SITE_STATS}>Site stats</option>
                        <option value={EffectType.TANGLE_REVEAL_CLUSTERS}>Tangle - reveal clusters</option>
                        <option value={EffectType.WORD_SEARCH_NEXT_WORDS}>Word search - show next words</option>
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
            <div className="row form-group text" data-row="add-effect">
                <label htmlFor="addEffect" className="col-lg-3 control-label text-muted">Drawback effect:</label>
                <div className="col-lg-7">
                    <select className="form-control" value={negativeEffect}
                            onChange={(event: React.ChangeEvent<HTMLSelectElement>) => {
                                setNegativeEffect(event.target.value)
                            }}>
                        <option value=""></option>
                        <option value={EffectType.DECREASE_FUTURE_TIMERS}>Decrease future timers</option>
                        <option value={EffectType.HIDDEN_EFFECTS}>Hidden effects</option>
                        <option value={EffectType.SPEED_UP_RESET_TIMER}>Speed up reset timer</option>
                        <option value={EffectType.START_RESET_TIMER}>Start reset timer</option>
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
                <div className="col-lg-11 text ">
                    <hr/>
                    <DeleteScriptButton scriptType={scriptType}/>
                </div>
            </div>

        </>
    )
}

const EffectList = (props: { scriptType: ScriptType }) => {
    return <>
        {props.scriptType.effects.map(effect => <EffectConfigurationLine scriptType={props.scriptType} effect={effect} key={effect.effectNumber}/>)}
        <div className="row text">
            <div className="col-lg-11">
                <hr/>
            </div>
        </div>
    </>
}

const DeleteScriptButton = ({scriptType}: { scriptType: ScriptType }) => {

    const forceDeleteEnabled = useSelector((state: GmRootState) => state.scriptsManagement.ui.forceDeleteEnabled)

    const deleteType = () => {
        webSocketConnection.send("/gm/scriptType/delete", { scriptTypeId: scriptType.id, force: forceDeleteEnabled })
    }

    const deleteText = forceDeleteEnabled ? "Delete type (force)" : "Delete type"
    const deleteClass = forceDeleteEnabled ? "btn-danger" : "btn-info"

    return <div className={`btn ${deleteClass} text-size`} onClick={deleteType}>{deleteText}</div>
}
