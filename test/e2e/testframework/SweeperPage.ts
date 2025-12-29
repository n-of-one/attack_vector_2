import {Page} from "@playwright/test"
import {expect} from "./fixtures"

import {PageAssertionsToHaveScreenshotOptions} from "playwright/types/test"
import {wait} from "./utils/testUtils";

export class SweeperPage {
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
        await expect(this.page.getByText("Ice:"), `Verify ICE name to be Visphotak.`).toContainText("Visphotak");
        await expect(this.page).toHaveScreenshot(`${screenshotName}.png`, this.screenshotOptions)
    }

    async leftClickToIndicateNoMine(x: number, y: number) {
        this.log(`Left click to indicate no mine(${x}, ${y})`)
        await this.page.mouse.click(x, y, {button: "left"})
        await wait(this.page, 0.2, "wait for sweeper click to render")
    }

    async rightClickToIndicateMine(x: number, y: number) {
        this.log(`Right click to indicate no mine(${x}, ${y})`)
        await this.page.mouse.click(x, y, {button: "right"})
        await wait(this.page, 0.2, "wait for sweeper click to render")
    }

    async verifyPuzzleSolved() {
        this.log(`Verify puzzle solved`)
        await this.verifyTerminalText("Memory restored")
        await this.verifyTerminalText("ICE grants access")
    }

    async verifyTerminalText(text: string) {
        this.log(`Verify terminal text to contain ${text}`)
        await expect(this.page.locator(".terminalPanel"), `Verify ICE terminal text to contain: ${text}`).toContainText(text)
    }

    async pressReset(seconds: number) {
        await this.page.mouse.move(781,30)
        await this.page.mouse.down();
        await this.page.waitForTimeout(1000 * seconds);
        await this.page.mouse.up();
    }

    async pressResetUntilComplete() {
        await this.page.mouse.move(781,30)
        await this.page.mouse.down();

        const start = new Date().getTime();
        const TIMEOUT = 12 * 1000;
        while (new Date().getTime() < start + TIMEOUT) {
            const terminalText = await this.page.locator('.terminalPanel').textContent()
            if (terminalText.includes("Reset completed")) {
                break
            }
            await this.page.waitForTimeout(100)
        }
        await this.page.mouse.up();
    }

    async verifyMinesLeft(minesLeft: number) {
        await expect(this.page.locator('[data-testid="minesLeft"]')).toContainText(`${minesLeft}`)
    }


    // --- helper methods -- //

    log(message: string) {
        console.log(message)
    }

}
