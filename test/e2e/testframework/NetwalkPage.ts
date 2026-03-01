import {Page} from "@playwright/test"
import {expect} from "./fixtures"

import {PageAssertionsToHaveScreenshotOptions} from "playwright/types/test"
import {wait} from "./utils/testUtils";

export class NetwalkPage {
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
        await expect(this.page.getByText("Ice:"), `Verify ICE name to be Sanrachana.`).toContainText("Sanrachana");
        await expect(this.page).toHaveScreenshot(`${screenshotName}.png`, this.screenshotOptions)
    }

    async leftClick(x: number, y: number) {
        this.log(`Click cell to rotate at (${x}, ${y})`)
        await this.page.mouse.click(x, y, {button: "left"})
        await wait(this.page, 0.2, "wait for netwalk click to render")
    }

    async verifyPuzzleSolved() {
        this.log(`Verify puzzle solved`)
        await this.verifyTerminalText("Configuration restored")
        await this.verifyTerminalText("Access granted")
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
