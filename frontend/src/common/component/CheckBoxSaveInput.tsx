import React, {useState} from 'react';


interface Props {
    id: string,
    className: string,
    checked: boolean
    save: (value: boolean) => void
}


export const CheckboxSaveInput = (props: Props) => {

    const [value, setValue] = useState(props.checked)
    const [lastChecked, setLastChecked] = useState(props.checked)

    // A new value from the server always wins, whether it confirms our own click or comes from someone else.
    if (props.checked !== lastChecked) {
        setLastChecked(props.checked)
        setValue(props.checked)
    }

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = event.target.checked
        setValue(newValue)   // show the click immediately, before the server answers
        props.save(newValue)
    }

    return (
        <input type="checkbox"
               id={props.id}
               style={{height: "29px"}}
               className={props.className}
               checked={value}
               onChange={handleChange}
        />
    )
}
