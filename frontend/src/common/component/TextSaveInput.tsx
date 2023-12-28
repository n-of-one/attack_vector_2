import React, {useState} from 'react';

const ENTER_KEY = 13;

/*

When the focus is lost or enter is hit, the component fires its "save" method as provided by the props.
During the saving, a floppy disk 'save' icon is shown.

This component does something that is not supported by React.
We want to have the component showing the "save" icon and then wait for an update from the server
However, there is no good way to detect an update from the server. The only thing we have are the props inserted by the parent, and the fact that
the component got re-rendered.

The update from the server can be one of two options:
- The update was accepted, and the server sends back the supplied value as the new value for this component.
- The update was rejected, and the server sends back the old value for this component.

The component is re-rendered whenever we make a change to it. We want to show the 'save' icon, so we will at least rerender once.

The solution is to track the state of the component outside of the component. This is against React best practice. But it allows the component
to check if it's in the state where it's waiting for a response from the server.

Any solution where state is tracked inside the component will not work, because the component will be re-rendered each time the state is updated
*/


interface Props {
    id?: string,
    value?: string | number,
    placeholder?: string,
    type?: string, // textArea or undefined for textInput
    save: (value: string) => void,
    className: string,
    rows?: number,
    readonly?: boolean,

}

enum State {
    IDLE = "IDLE",
    EDITING = "EDITING",
    SHOW_SAVE = "SHOW_SAVE",
    WAITING_FOR_SERVER = "WAITING_FOR_SERVER",
}

const stateMap: Map<string, State> = new Map<string, State>()

export const TextSaveInput = (props: Props) => {
    const readonly = props.readonly ? props.readonly : false
    // convert potential number values to string, otherwise equals detection does not work
    const valueFromProps = "" + props.value

    const [value, setValue] = useState(valueFromProps)
    const [id, setId] = useState(new Date().getTime() + ":" + Math.random())
    const [saveIcon, setSaveIcon] = useState(false)

    if (!stateMap.has(id)) {
        stateMap.set(id, State.IDLE)
    }
    const state = stateMap.get(id) as State

    if (state === State.IDLE && valueFromProps !== value) { // update from external
        setValue(valueFromProps)
    }

    if (state === State.EDITING) {
        // nothing to do, wait for next state. Ignore updates from external, they will be overwritten when this user saves.
    }

    if (state === State.SHOW_SAVE) {
        stateMap.set(id, State.WAITING_FOR_SERVER)
    }

    if (state === State.WAITING_FOR_SERVER) {
        if (valueFromProps === value) { // save successful
        } else { // save unsuccessful, revert back to previous values
            setValue(valueFromProps)
        }
        stateMap.set(id, State.IDLE)
        setSaveIcon(false)
    }

    const handleChange = (event: any) => {
        // Called when value of input changes (key pressed, or text pasted)
        if (state === State.IDLE) {
            stateMap.set(id, State.EDITING)
        }
        const newValue = event.target.value;
        setValue(newValue)
    }

    const handleKeyDown = (event: any, defocusOnEnter: boolean) => {
        if (state === State.EDITING) {
            if (event.keyCode === ENTER_KEY && defocusOnEnter) {
                event.target.blur();
            }
        }
    }

    const handleBlur = () => {
        if (state === State.EDITING) {
            if (value === props.value) { // nothing changed, no save needed.
                stateMap.set(id, State.IDLE)
            }
            else {
                stateMap.set(id, State.SHOW_SAVE)
                props.save(value) // trigger call to server, that will in turn trigger call to update this component.
                setSaveIcon(true)
            }
        }
    }


// Don't have text be null or undefined, this will cause React to treat the Input as
// an uncontrolled component and then later detect that it is now a controlled component.
// This will log errors in the console
    let text = value;
    if (text === null || text === undefined) {
        text = ''
    }

    let icon = (saveIcon) ? (<span className="form-control-feedback d-flex justify-content-end saveIcon"><span className="glyphicon glyphicon-floppy-save"
                                                                                                    aria-hidden="true"/></span>) : '';

    if (props.type === "textArea") {
        return (
            <span>
                <textarea id={props.id}
                          className={props.className}
                          placeholder={props.placeholder}
                          value={text}
                          onChange={(event) => handleChange(event)}
                          onKeyDown={(event) => handleKeyDown(event, false)}
                          onBlur={() => handleBlur()}
                          rows={props.rows}
                          disabled={readonly}
                />
                {icon}
            </span>
        );
    } else {
        return (
            <span>
                    <input id={props.id}
                           type="text" className={props.className}
                           placeholder={props.placeholder}
                           value={text}
                           onChange={(event) => handleChange(event)}
                           onKeyDown={(event) => handleKeyDown(event, true)}
                           onBlur={() => handleBlur()}
                           disabled={readonly}
                    />
                {icon}
                </span>
        );
    }
}
