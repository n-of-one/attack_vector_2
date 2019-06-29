import React from "react";

const nonBreakingSpace = (key) => {
    return <span key={key}>&nbsp;</span>
};

const renderSpaces = (count) => {
    let text = [];

    for (let i = 0; i < count; i++) {
        text.push(nonBreakingSpace(i))
    }

    return text;
};

/**
 *
 * Render a number of non-breaking spaces.
 *
 * If no {n} parameter is supplied, then the total number of non-blocking spaces rendered is equal to {p}
 *
 * If an {n} parameter is supplied, then the total number of spaces is rendered that will make the total number of characters
 * of the number {n} + the spaces be equal to the {p} parameter.
 *
 * If a {t} parameter is supplied, this is the text that needs to be padded.
 *
 *
 * So if you want a total of 4 characters padded with spaces for the number 12, you use:
 * <Pad n="12" p="4">
 *
 * which returns nbsp;nbsp;
 *
 * so you can render:
 *
 * 12nbsp;nbsp;
 */
export default ({ n, p, t}) => {

    if (n !== 0 && !(n) && (!t)) {
        return renderSpaces(p);
    }
    if (n <= 0) {
        return renderSpaces(p - 1);
    }

    let textSize;
    if (n) {
        textSize = Math.floor(Math.log10(n)) + 1;
    }
    else {
        textSize = t.length;
    }

    const spaces = p - textSize;

    if (spaces > 0 && spaces < 100) {
        return renderSpaces(spaces);
    }
    else {
        return <>[[Invalid value for spaces: {spaces}]]</>
    }
};