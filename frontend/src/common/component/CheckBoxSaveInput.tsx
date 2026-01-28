import React, {useState} from 'react';


interface Props {
    id: string,
    className: string,
    checked: boolean
    save: (value: boolean) => void
}



export const CheckboxSaveInput = (props: Props) => {


    const [value, setValue] = useState(props.checked);

    const [saving, setSaving] = useState(false);

    if (!saving && props.checked !== value) {
        setValue(props.checked);
        setSaving(false)
    }

    const handleChange = (event: any) => {
        const newValue: boolean = event.target.checked;
        setValue(newValue)
        setSaving(true)
        props.save(newValue)
    };

    const className = props.className
    // let value;
    // if (this.state.initialized) {
    //     value = this.state.value;
    // }
    // else {
    //     value = (this.props.value) ? this.props.value : false;
    //     // Don't have text be null or undefined, this will cause React to treat the Input as
    //     // an uncontrolled component and then later detect that it is now a controlled component.
    //     // This will log errors in the console
    // }

    return (

        <input type="checkbox"
               className={className}
               checked={value}
               onChange={(event) => handleChange(event)}
        />
    );
}
