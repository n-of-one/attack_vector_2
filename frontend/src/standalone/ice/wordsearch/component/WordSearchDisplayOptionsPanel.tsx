import React, {ReactNode, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";
import {
    WORD_SEARCH_DISPLAY_OPTIONS,
    WordSearchDisplayOptions,
    WordSearchFontSize,
    WordSearchGridLines,
    WordSearchLetterColor,
    WordSearchNoise
} from "../reducer/WordSearchDisplayOptionsReducer";
import {wordSearchCanvas} from "../canvas/WordSearchCanvas";

interface Choice<T> {
    value: T,
    caption: string,
    preview: ReactNode,
}

interface OptionRowProps<T> {
    title: string,
    choices: Choice<T>[],
    value: T,
    onSelect: (value: T) => void,
}

const OptionRow = <T, >({title, choices, value, onSelect}: OptionRowProps<T>) => {
    return <div className="wsOptionRow" role="group" aria-label={title}>
        <div className="wsOptionRowTitle">{title}</div>
        <div className="wsOptionTiles">
            {choices.map((choice) => {
                const selected = choice.value === value
                return <button type="button" key={String(choice.value)} title={`${title}: ${choice.caption}`} aria-pressed={selected}
                               className={"wsOptionTile" + (selected ? " wsOptionTileSelected" : "")} onClick={() => onSelect(choice.value)}>
                    <div className="wsOptionTilePreview">{choice.preview}</div>
                    <div className="wsOptionTileCaption">{choice.caption}</div>
                </button>
            })}
        </div>
    </div>
}

const Letter = ({color, size = 20}: { color: WordSearchLetterColor, size?: number }) => {
    return <span className={`wsColor_${color} wsOptionLetter`} style={{fontSize: `${size}px`}}>A</span>
}

export const WordSearchDisplayOptionsPanel = () => {
    const options = useSelector((state: WordSearchRootState) => state.displayOptions)
    const showDisplayOptions = useSelector((state: WordSearchRootState) => state.puzzle.showDisplayOptions)
    const dispatch = useDispatch()
    const [open, setOpen] = useState(false)

    if (!showDisplayOptions) return null

    const update = (changed: Partial<WordSearchDisplayOptions>) => {
        dispatch({type: WORD_SEARCH_DISPLAY_OPTIONS, options: changed})
        if (changed.gridLines) {
            wordSearchCanvas.setGridLines(changed.gridLines)
        }
        if (changed.noise) {
            wordSearchCanvas.setNoise(changed.noise)
        }
    }

    const colorChoice = (value: WordSearchLetterColor, caption: string): Choice<WordSearchLetterColor> =>
        ({value, caption, preview: <Letter color={value}/>})
    const sizeChoice = (value: WordSearchFontSize): Choice<WordSearchFontSize> =>
        ({value, caption: `${value}`, preview: <Letter color={options.color} size={value}/>})
    const gridChoice = (value: WordSearchGridLines, caption: string): Choice<WordSearchGridLines> =>
        ({value, caption, preview: <div className={`wsOptionGrid wsOptionGrid_${value}`}/>})
    const noiseChoice = (value: WordSearchNoise, caption: string): Choice<WordSearchNoise> =>
        ({value, caption, preview: <div className={`wsOptionNoise wsOptionNoise_${value}`}/>})

    return <div className="wsOptions">
        <button type="button" className="wsOptionsHeader" aria-expanded={open} onClick={() => setOpen(!open)}>
            <span>Display</span>
            <span>{open ? "▾" : "▸"}</span>
        </button>
        {open && <div className="wsOptionsBody">
            <OptionRow<WordSearchLetterColor> title="Letter color" value={options.color} onSelect={(color) => update({color})}
                                              choices={[colorChoice("DEFAULT", "Default"), colorChoice("LIGHT_BLUE", "Light blue"),
                                                  colorChoice("AMBER", "Amber")]}/>
            <OptionRow<WordSearchFontSize> title="Font size" value={options.fontSize} onSelect={(fontSize) => update({fontSize})}
                                           choices={[sizeChoice(16), sizeChoice(19), sizeChoice(22)]}/>
            <OptionRow<WordSearchGridLines> title="Grid lines" value={options.gridLines} onSelect={(gridLines) => update({gridLines})}
                                            choices={[gridChoice("DEFAULT", "Default"), gridChoice("OFF", "Off"), gridChoice("LIGHT", "Light")]}/>
            <OptionRow<WordSearchNoise> title="Background noise" value={options.noise} onSelect={(noise) => update({noise})}
                                        choices={[noiseChoice("ON", "On"), noiseChoice("OFF", "Off")]}/>
        </div>}
    </div>
}
