import React from "react";
import {useSelector} from "react-redux";
import {SwitchRootState, SwitchState} from "./SwitchReducers";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {layer} from "../../StandaloneGlobals";
import {CloseTabButton} from "../../ice/common/CloseTabButton";
import {getReadableTextColor} from "../../widget/statusLight/contrast-color";
import {StatusLightOption} from "../../widget/statusLight/StatusLightReducers";


export const Switch = () => {

    const state: SwitchState = useSelector((state: SwitchRootState) => state.switch)

    let content;
    if (state.currentOption === null) {
        content = <div>Loading...</div>
    } else if (state.options.length === 2) {
        content = <TwoOptionSwitch state={state}/>
    } else {
        content = <MultiOptionSwitch state={state}/>
    }

    return <div className="container-fluid" data-bs-theme="dark">
        <div className="row">
            <div className="col-12">
                        <span className="d-flex justify-content-between">
                        <h2>{ state.switchLabel}</h2>
                        <CloseTabButton/>
                        </span>
            </div>
        </div>
        {content}
    </div>
}


const TwoOptionSwitch = ({state}: { state: SwitchState }) => {

    const option1 = state.options[0] || {text: "Option 1", color: "red"};
    const option2 = state.options[1] || {text: "Option 2", color: "lime"};

    const status1KnobColor = getReadableTextColor(option1.color);
    const status2KnobColor = getReadableTextColor(option2.color);


    const knob = state.currentOption === 0 ? status1KnobColor : status2KnobColor;

    const setValue = (newOption: number) => {
        webSocketConnection.sendObject("/app/statusLight/setValue", {layerId: layer.id, value: newOption})
    }
    const textClassOption1 = state.currentOption === 0 ? "text_light" : "";
    const textClassOption2 = state.currentOption === 1 ? "text_light" : "";


    return (
        <div className="d-flex flex-row mb-3">
            <div className={`p-2 text appSwitch switchLabel ${textClassOption1}`}>{option1.text}</div>
            <div className="p-2"><span className="appSwitch form-check form-switch">
                                <input className="form-check-input switch" type="checkbox" role="switch" checked={state.currentOption === 1}
                                       onChange={() => setValue(state.currentOption === 0 ? 1 : 0)}
                                       style={{
                                           "--switch-option-1-color": option1.color,
                                           "--switch-option-2-color": option2.color,
                                           backgroundImage: svgCircle(knob),
                                       } as React.CSSProperties}
                                /></span>
            </div>
            <div className={`p-2 text switchLabel ${textClassOption2}`}>{option2.text}</div>
        </div>
    )
}

function svgCircle(fill: string): string {
    const encoded = encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" viewBox="-4 -4 8 8">
       <circle r="3" fill="${fill}"/>
     </svg>`
    );
    return `url("data:image/svg+xml,${encoded}")`;
}


export const MultiOptionSwitch = ({state}: { state: SwitchState }) => {

    if (state.currentOption === null) {
        return <div>Loading...</div>
    }

    return (
        <div className="row">
            <div className="col-12">
                {state.options.map((option: StatusLightOption, index: number) => (
                    <MultiOption key={`option${index}`} option={option} index={index} currentOption={state.currentOption!!}/>
                ))}
            </div>
        </div>
    )
}

export const MultiOption = ({option, index, currentOption}: { option: StatusLightOption, index: number, currentOption: number }) => {
    const radioId = `switch2-option-${index}`;
    const textClass = index === currentOption ? "text_light" : "";

    const handleSelect = (): void => {
        webSocketConnection.sendObject("/app/statusLight/setValue", {layerId: layer.id, value: index})
    }

    return (
        <div className="row mb-2">
            <div className="col-12">
                <div className="d-flex align-items-center">
                    <div className="me-3">
                        <input
                            type="radio"
                            className="form-check-input"
                            name="switch2-options"
                            id={radioId}
                            checked={currentOption === index}
                            onClick={handleSelect}
                        />
                    </div>
                    <label className="d-flex align-items-center mb-0" htmlFor={radioId} style={{cursor: "pointer"}}>
                        <span
                            className="me-3 switch-banner"
                            style={{"--status-light-color": option.color} as React.CSSProperties}
                        />
                        <span className={`text ${textClass}`}>{option.text}</span>
                    </label>
                </div>
            </div>
        </div>
    )
}
