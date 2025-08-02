import React from "react";

/**
 *
 * Render a number of non-breaking spaces.
 *
 * If no {n} parameter is supplied, then the total number of non-blocking spaces rendered is equal to {length}
 *
 * If an {n} parameter is supplied, then the total number of spaces is rendered that will make the total number of characters
 * of the number {n} + the spaces be equal to the {length} parameter.
 *
 * If a {t} parameter is supplied, this is the text that needs to be padded.
 *
 *
 * So if you want a total of 4 characters padded with spaces for the number 12, you use:
 * <Pad numberValue={12} length={4}>
 *
 * which returns &nbsp;&nbsp;
 * * so you can render: 12&nbsp;&nbsp;
 *
 * You can also provide a text to have this padded:
 *
 * <Pad textValue="Hello" length={6}
 *
 * which returns &nbsp;
 * So you can render: &nbsp;Hello
 */

interface PadProps {
    length: number,
    numberValue?: number,
    textValue?: string
}

export const Pad = (props: PadProps) => {
    const {numberValue, length, textValue} = props

    if (numberValue === undefined && textValue === undefined) {
        return (<span dangerouslySetInnerHTML={{__html: renderSpaces(length)}}/>);
    }

    if (numberValue !== undefined && numberValue <= 0) {
        return (<span dangerouslySetInnerHTML={{__html: renderSpaces(length - 1)}}/>);
    }

    let textSize;
    if (numberValue) {
        textSize = Math.floor(Math.log10(numberValue)) + 1;
    } else {
        textSize = textValue!.length;
    }

    const spaces = length - textSize;

    if (spaces > 0 && spaces < 100) {
        return (<span dangerouslySetInnerHTML={{__html: renderSpaces(spaces)}} />)
    } else {
        return <>[[Invalid value for spaces: {spaces}]]</>
    }
}

const renderSpaces = (count:number) => {
    let text = "";

    for (let i = 0; i < count; i++) {
        text += "&nbsp;"
    }

    return text;
};

const ZEROES = "00000000000000000000000000000000000000";
export const zeroPad = (numberValue: number, totalPositions: number) => {
    const numberPositions = (numberValue === 0) ? 1 : 1 + Math.floor(Math.log10(numberValue));

    return ZEROES.substring(0, (totalPositions - numberPositions)) + numberValue;
};

