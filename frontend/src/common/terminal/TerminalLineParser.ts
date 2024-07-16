import {TerminalBlockType, TerminalLineData, TerminalLineBlock} from "./TerminalReducer";


let terminalLineKey = 0
const nextTerminalLineKey = () => {
    return terminalLineKey++
}

export const parseTextToTerminalLine = (line: string): TerminalLineData => {
    const rawTokens = parseLineToTokens(line)

    const normalizedTokens = normalizeSpaces(rawTokens)

    const blocks =  toBlocks(normalizedTokens)

    return {
        blocks: blocks,
        key: nextTerminalLineKey(),
    }
}


enum TokenType {
    TEXT,
    SPACE,
    STYLE,
    LINK,
    CLEAR,
}

interface Token {
    text: string,
    type: TokenType
}

let terminalBlockKey = 0
const nextTerminalBlockKey = () => {
    return terminalBlockKey++
}


enum Mode {
    TEXT,
    FORMATTING,
}

/**
 * Parses a line of text into tokens. For example:
 *
 * hello, please [b]click[/]  [http://localhost/image]link[/]
 *
 * will be parsed into:
 *
 * [text: hello,] [space] [text: please] [space] [format: b] [text: click] [clear] [space] [space] [link] [text] [clear]
 */
const parseLineToTokens = (line: string): Token[] => {

    const tokens: Token[] = []

    let mode = Mode.TEXT
    let currentText = ""
    let currentChar = null

    for (let i = 0; i < line.length; i++) {
        currentChar = line.charAt(i)

        // Text blocks are terminated by spaces, because HTML does not render multiple spaces, which we do want in a terminal
        if (currentChar === " " && mode === Mode.TEXT) {
            if (currentText) {
                tokens.push({text: currentText, type: TokenType.TEXT})
                currentText = ""
            }
            tokens.push({text: " ", type: TokenType.SPACE})
            continue
        }

        // [ characters starts a style or link block. [ characters are escaped by another [ character.
        if (currentChar === "[") {
            if (mode === Mode.TEXT) {
                mode = Mode.FORMATTING
                if (currentText) {
                    tokens.push({text: currentText, type: TokenType.TEXT})
                    currentText = ""
                }
                continue
            } else {
                // escaped '[' character
                mode = Mode.TEXT
                currentText += currentChar
                continue
            }
        }

        // links and style is terminated by a ] character
        if (currentChar === "]" && mode === Mode.FORMATTING) {
            if (currentText.startsWith("h")) {
                tokens.push({text: currentText, type: TokenType.LINK})
            } else if (currentText === "/") {
                tokens.push({text: currentText, type: TokenType.CLEAR})
            } else {
                tokens.push({text: currentText, type: TokenType.STYLE})
            }

            currentText = ""
            mode = Mode.TEXT
            continue
            // let style = currentText === "/" ? "" : currentText
            // let styleParts = style.split(" ")
            // currentClassName = styleParts.map(it => "terminal_style_" + it).join(" ")
            // currentBlock.text = ""
            // continue
        }

        currentText += currentChar
    }
    // process the last text, in case it's text
    if (mode === Mode.TEXT && currentText) {
        tokens.push({text: currentText, type: TokenType.TEXT})
    }

    return tokens
}


/**
 * Replace [space] tokens into text in such a way as to not create a double space in a text. This way it will
 * be rendered correctly in html.
 *
 * the time:  12:00
 *
 * [text: "the"] [space] [text: "time:"] [space] [space] [text: "12:00"]
 *
 * will become
 *
 * [text: "the time: "] [space] [text: "12:00"] *
 */
const normalizeSpaces = (tokensInput: Token[]): Token[] => {

    let currentText = ""
    const tokens: Token[] = []

    for (let i = 0; i < tokensInput.length; i++) {
        const token = tokensInput[i]

        if (token.type === TokenType.STYLE || token.type === TokenType.LINK || token.type === TokenType.CLEAR) {
            if (currentText) {
                tokens.push({text: currentText, type: TokenType.TEXT})
                currentText = ""
            }
            tokens.push(token)
            continue
        }

        if (token.type === TokenType.SPACE) {
            if (!currentText.endsWith(" ")) {
                // The goal of this normalization: remove space tokens that can be inserted into text
                currentText += " "
                continue
            }
            tokens.push({text: currentText, type: TokenType.TEXT})
            tokens.push({text: " ", type: TokenType.SPACE})
            currentText = ""
            continue
        }

        currentText += token.text
    }
    if (currentText) {
        tokens.push({text: currentText, type: TokenType.TEXT})
    }
    return tokens
}


/**
 * Converts tokens into blocks. For example:
 * [style: b] [text: "hello"] [clear]
 *
 * will become:
 *
 * {type: "text", text: "hello", className: "t_b"}
 */
const toBlocks = (tokens: Token[]): TerminalLineBlock[] => {

    const blocks: TerminalLineBlock[] = []

    let currentText = ""
    let currentStyle = ""
    let currentLink: string | null = null

    function createClassName() {
        let styleParts = currentStyle.split(" ")
        return styleParts.map(it => "t_" + it).join(" ")
    }

    function addTextBlockForPreviousText() {
        if (!currentText) return
        blocks.push({
            type: TerminalBlockType.TEXT,
            text: currentText, size: currentText.length,
            className: createClassName(), key: nextTerminalBlockKey(),
        })
        currentText = ""
    }

    function addLinkBlock(link: string) {
        if (!currentText) {
            // empty link, fill in the word "link"
            currentText = "link"
        }
        blocks.push({
            type: TerminalBlockType.LINK, link: link,
            text: currentText, size: currentText.length,
            className: createClassName(), key: nextTerminalBlockKey(),
        })
    }

    for (let i = 0; i < tokens.length; i++) {
        const token = tokens[i]

        if (token.type === TokenType.LINK) {
            addTextBlockForPreviousText()
            currentLink = token.text
            continue
        }

        if (token.type === TokenType.STYLE) {
            addTextBlockForPreviousText()
            currentStyle = token.text
            continue
        }

        if (token.type === TokenType.SPACE) {
            if (currentLink !== null) {
                // can't have double spaces in links. Ignore
                continue
            }

            addTextBlockForPreviousText()
            blocks.push({
                type: TerminalBlockType.SPACE,
                text: " ", size: 1,
                className: createClassName(), key: nextTerminalBlockKey(),
            })

            continue
        }

        if (token.type === TokenType.CLEAR) {
            if (currentLink === null) {

                // Clear style

                addTextBlockForPreviousText()
                currentStyle = ""
                continue
            }

            // Create link
            addLinkBlock(currentLink)
            currentLink = null
            currentText = ""
            currentStyle = "" // also clear style
            continue
        }

        // token.type === Token.TEXT
        currentText += token.text
    }

    if (currentText) {
        if (currentLink) {
            // last link was not terminated. Add it anyway.
            addLinkBlock(currentLink)
        }
        else {
            addTextBlockForPreviousText()
        }
    }

    if (blocks.length > 0 ) {
        return blocks
    }

    return [{type: TerminalBlockType.EMPTY_LINE, text: "", size: 0, className: "", key: nextTerminalBlockKey(),}]

}
