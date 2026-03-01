import {Page} from "@playwright/test"
import {expect} from "./fixtures"
import {wait} from "./utils/testUtils";
import {PageAssertionsToHaveScreenshotOptions} from "playwright/types/test";

export class WordSearchPage {
    page: Page

    screenshotOptions: PageAssertionsToHaveScreenshotOptions = {
        clip: {
            x: 320, y: 100, width: 1600, height: 852 // canvas area
        }
    }
    constructor(page: Page) {
        this.page = page
    }

    async verifyIceName() {
        this.log("Verify ICE name is Jaal")
        await expect(this.page.getByText("Ice:"), "Verify ICE name to be Jaal.").toContainText("Jaal")
    }

    async checkScreenshot(screenshotName: string) {
        this.log(`Verify screenshot: ${screenshotName}`)
        await expect(this.page).toHaveScreenshot(`${screenshotName}.png`, this.screenshotOptions)
    }

    async waitForPuzzleReady() {
        this.log("Wait for puzzle to be ready")
        await expect(this.page.locator(".terminalPanel"), "Wait for first search fragment").toContainText("Search fragment:", {timeout: 5_000})
    }

    async dragWord(startX: number, startY: number, endX: number, endY: number) {
        this.log(`Drag word from (${startX},${startY}) to (${endX},${endY})`)
        await this.page.mouse.move(startX, startY)
        await this.page.mouse.down()
        await this.page.mouse.move(endX, endY, {steps: 5})
        await this.page.mouse.up()
        await wait(this.page, 1.0, "wait for word selection to be processed")
    }

    async verifyPuzzleSolved() {
        this.log("Verify puzzle solved")
        await expect(this.page.locator(".terminalPanel"), "Wait for complete animation to finish").toContainText("access granted", {timeout: 10_000})
    }

    async verifyTerminalText(text: string) {
        this.log(`Verify terminal text to contain: ${text}`)
        await expect(this.page.locator(".terminalPanel"), `Verify ICE terminal text to contain: ${text}`).toContainText(text)
    }

    // --- helper methods -- //

    log(message: string) {
        console.log(message)
    }

}
