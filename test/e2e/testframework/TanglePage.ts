import {Page} from "@playwright/test"
import {expect} from "./fixtures"

import {PageAssertionsToHaveScreenshotOptions} from "playwright/types/test"

export class TanglePage {
    page: Page

    screenshotOptions: PageAssertionsToHaveScreenshotOptions = {
        clip: {
            x: 320, y: 100, width: 1600, height: 852 // canvas area
        }
    }

    constructor(page: Page) {
        this.page = page
    }

    async checkNameAndScreenshot(screenshotName: string) {
        this.log(`Verify ICE name and screenshot: ${screenshotName}`)
        await expect(this.page.getByText("Ice:"), `Verify ICE name to be Gaanth.`).toContainText("Gaanth");
        await expect(this.page).toHaveScreenshot(`${screenshotName}.png`, this.screenshotOptions)
    }

    async moveTanglePoint(fromX: number, fromY: number, toX: number, toY: number) {
        this.log(`Move tangle points (${fromX}, ${fromY}) -> (${toX} ${toY})`)
        await this.page.mouse.move(fromX,fromY)
        await this.page.mouse.down()
        await this.page.mouse.move(toX,toY, { steps: 2 })
        await this.page.mouse.up()
    }

    async verifyPuzzleSolved() {
        this.log(`Verify puzzle solved`)
        await this.verifyTerminalText("Decryption complete")
        await this.verifyTerminalText("ICE grants access")
    }

    async verifyTerminalText(text: string) {
        this.log(`Verify terminal text to contain ${text}`)
        await expect(this.page.locator(".terminalPanel"), `Verify ICE terminal text to contain: ${text}`).toContainText(text)
    }

    // --- helper methods -- //

    log(message: string) {
        console.log(message)
    }

}
