import {Page} from "@playwright/test";
import {expect} from "./fixtures"
import {PageAssertionsToHaveScreenshotOptions} from "playwright/types/test";
import {appLocator, confirmDialog, countOccurrences, log} from "./utils/testUtils";

export const START_ATTACK_REGULAR = false
export const START_ATTACK_QUICK = true

const canvasScreenshotOptions: PageAssertionsToHaveScreenshotOptions = {
    clip: {
        x: 646, y: 0, width: 1264, height: 905
    },
    timeout: 5000
}

export class HackerPage {
    page: Page

    constructor(page: Page) {
        this.page = page
    }

    async startNewRun(siteName: string) {
        log(`Start new run for: ${siteName}`)

        await this.page.getByText('Home').click();
        const siteNameLocator = this.page.getByRole('textbox', {name: 'Site name'})

        await siteNameLocator.click()
        await siteNameLocator.fill(siteName)
        await siteNameLocator.press('Enter')
        await expect(this.page.getByText("Site:"), `Verify page to contain ${siteName}`).toContainText(siteName)
        await this.waitForEnterSiteAnimationFinished()
        await this.waitForPrompt()
    }

    async joinExistingRun(siteName: string) {
        log(`Join existing run: ${siteName}`)
        await this.page.getByText('Home').click();
        await this.page.getByText(siteName).click()
        await expect(this.page.getByText("Site:"), `Verify page to contain ${siteName}`).toContainText(siteName)
        await this.waitForEnterSiteAnimationFinished()
        await this.waitForPrompt()
    }

    async typeCommand(command: string, expectedInTerminal?: string | string[], timeoutSeconds?: number) {
        await this.typeCommandDontWaitForPrompt(command, expectedInTerminal, timeoutSeconds)
        await this.waitForPrompt()
    }

    async typeCommandDontWaitForPrompt(command: string, expectedInTerminal?: string | string[], timeoutSeconds?: number) {
        log(`Type command: ${command}`)
        await this.page.keyboard.press('Control+l')
        await this.page.keyboard.type(command)
        await this.page.keyboard.press('Enter')
        if (expectedInTerminal) {
            await this.expectTerminalText(expectedInTerminal, timeoutSeconds)
        }
    }

    async expectTerminalText(expected: string | string[], timeoutSeconds?: number) {
        log(`Verify terminal test: ${expected}`)
        const options = timeoutSeconds ? {timeout: timeoutSeconds * 1000} : undefined

        const expectedArray = (expected instanceof Array) ? expected : [expected]

        for (const text of expectedArray) {
            await expect(this.page.locator(".terminalPanel"), `Verify terminal text: ${text}`).toContainText(text, options)
        }
    }

    async expectNotTerminalText(text: string) {
        log(`Verify terminal does not contain text: ${text}`)
        await expect(this.page.locator(".terminalPanel"), `Verify terminal text: ${text}`).not.toContainText(text)

    }

    async verifyScreenshotCanvas(name: string) {
        log(`Verify screenshot: ${name}`)
        await expect(this.page).toHaveScreenshot(`${name}.png`, canvasScreenshotOptions)
    }


    async resetRuns(site: string) {
        log(`Reset runs for: ${site}`)
        confirmDialog(this.page, "Confirm that you want to reset")
        await this.page.getByRole('row').filter({hasText: site}).getByTitle("Reset site").click()
        await expect(appLocator(this.page), `Verify site reset success noty`).toContainText("site reset")
    }


    async waitForPrompt() {
        await this.page.locator('css=.terminalCaret').waitFor()
    }

    async verifyAppText(text: string) {
        log(`Verify text on page via App locator to contain ${text}`)
        await expect(appLocator(this.page), `Verify text on page via App locator to contain ${text}`).toContainText(text)
    }

    async getCodeForScript(scriptName: string): Promise<string> {
        await this.page.locator(".glyphicon-expand.scriptPanelButtonIcon").click()

        const rows = this.page.locator('[data-row="hacker-script"]', {hasText: scriptName})
        expect(await rows.count()).toBeGreaterThan(0)
        const scriptRow = rows.first()
        const code = await scriptRow.locator('[data-element="code"]').innerText();

        // code script panel before we continue
        await this.page.locator(".glyphicon-collapse-up.scriptPanelButtonIcon").click()
        return code
    }

    async startAttack(quick: boolean, expectedTerminalLine: string = "Entered node") {
        const command = quick ? "qa" : "attack"
        const expectedInTerminal = quick ? [expectedTerminalLine, "Persona established, hack started"] : ["- Persona creation complete",
            "Connection established.", expectedTerminalLine]
        await this.typeCommand(command)
        await this.waitForStartAttackAnimationFinished()
        await this.expectTerminalText(expectedInTerminal)
    }

    async waitForScanAnimationFinished() {
        expect(true, `Waiting scan animation: scanAnimationsFinished`).toBeTruthy()
        await this.waitForEvent("scanAnimationsFinished")
    }

    async waitForNodeHackedAnimationFinished() {
        expect(true, `Waiting node hacked animation: nodeHackedAnimationFinished`).toBeTruthy()
        await this.waitForEvent("nodeHackedAnimationFinished")
    }

    private async waitForEnterSiteAnimationFinished() {
        expect(true, `Waiting enter site animation: enterSiteAnimationsFinished`).toBeTruthy()
        await this.waitForEvent("enterSiteAnimationsFinished")
    }

    private async waitForStartAttackAnimationFinished() {
        expect(true, `Waiting start attack animation: startAttackAnimationsFinished`).toBeTruthy()
        await this.waitForEvent("startAttackAnimationsFinished")
    }

    // private async waitForEvent(eventNameInPlayerWrightContext: string) {
    //     log(`Waiting for browser to fire event: ${eventNameInPlayerWrightContext}`)
    //     await this.page.evaluate(eventNameInBrowserContext => new Promise(resolve => {
    //         window.addEventListener(eventNameInBrowserContext, resolve, {once: true});
    //     }), eventNameInPlayerWrightContext);
    // }


    private async waitForEvent(eventNameInPlayerWrightContext: string) {
        log(`Waiting for browser to fire event: ${eventNameInPlayerWrightContext}`)
        await this.page.evaluate(eventNameInBrowserContext => {
            return (globalThis as any).__testSupport.waitForEvent(eventNameInBrowserContext)
        }, eventNameInPlayerWrightContext);
    }

    async verifyScanInfo(expectedText: string, expectedTimes: number) {
        const scanInfoText = (await this.page.locator("#scanInfo").allTextContents()).join("");
        const foundTimes = countOccurrences(scanInfoText, expectedText)
        if (foundTimes != expectedTimes) {
            throw new Error(`In scan info the text "${expectedText}" was expected to be present ${expectedTimes} times, but found it '${foundTimes} times. 
            Contents of the scan info box: ${scanInfoText}`);
        }
    }

    async setFontSize(size: number) {
        log(`Setting font size to: ${size} px`)
        await this.page.getByText('{').click();
        await this.page.getByLabel('Font size ?').selectOption(size.toString());
    }
}