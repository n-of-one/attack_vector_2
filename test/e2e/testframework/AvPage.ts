import {expect, Page} from "@playwright/test"
import {PageAssertionsToHaveScreenshotOptions} from "playwright/types/test"


export class AvPage {
    page: Page

    screenshotOptions: PageAssertionsToHaveScreenshotOptions = {
        clip: {
            x: 646, y: 0, width: 1264, height: 905 // canvas area
        }
    }

    constructor(page: Page) {
        this.page = page
    }

    get appLocator() {
        return this.page.locator('#app')
    }

    async loginUsingLink(username: string,) {
        this.log(`Logging in by using link: ${username}`)
        await this.page.goto('http://localhost:3000/redirectToLogin')
        await this.page.getByRole('link', {name: username}).click()
        await this.verifyUsernameVisible(username)
    }

    async loginByTyping(username: string) {
        this.log(`Logging in by typing username: ${username}`)
        await this.page.goto('http://localhost:3000/redirectToLogin')
        await this.page.locator('#handle').click()
        await this.page.locator('#handle').fill(username)
        await this.page.locator('#handle').press('Enter')
        await this.verifyUsernameVisible(username)
    }

    async loginOrCreateUserAndLogin(username: string) {
        await this.page.goto('http://localhost:3000/redirectToLogin')
        await this.page.locator('#handle').click()
        await this.page.locator('#handle').fill(username)
        await this.page.locator('#handle').press('Enter')
        this.log(`Attempting to log in a : ${username}`)
        const userDoesNotExist = await this.page.getByText('Username not found').isVisible()

        if (userDoesNotExist) {
            this.log(`User does not exist, creating this user`)
            await this.loginUsingLink("gm")
            await this.page.getByText('Users').click()
            await this.page.getByRole('textbox', {name: 'User name'}).fill('tangle')
            await this.page.getByRole('button', {name: 'Create'}).click()
            await this.page.getByRole('link', {name: 'ꕻ Logout'}).click()
            await this.loginByTyping(username)
        } else {
            await this.verifyUsernameVisible(username)
        }
    }

    private async verifyUsernameVisible(username: string) {
        await expect(this.page.getByText(`{${username}}`), `Verify page to contain ${username}`).toBeVisible()
    }


    async startNewRun(siteName: string) {
        this.log(`Starting new run for: ${siteName}`)
        await this.page.getByRole('textbox', {name: 'Site name'}).click()
        await this.page.getByRole('textbox', {name: 'Site name'}).fill(siteName)
        await this.page.getByRole('textbox', {name: 'Site name'}).press('Enter')
        await expect(this.page.getByText("Site:"), `Verify page to contain ${siteName}`).toContainText(siteName)
        await this.waitForPrompt()
    }

    async joinExistingRun(siteName: string) {
        this.log(`Joining existing run: ${siteName}`)
        await this.page.getByText(siteName).click()
        await expect(this.page.getByText("Site:"), `Verify page to contain ${siteName}`).toContainText(siteName)
        await this.waitForPrompt()
    }
    async typeCommand(command: string, expectedInTerminal?: string | string[], timeoutSeconds?: number) {
        await this.typeCommandDontWaitForPrompt(command, expectedInTerminal, timeoutSeconds)
        await this.waitForPrompt()
    }

    async typeCommandDontWaitForPrompt(command: string, expectedInTerminal?: string | string[], timeoutSeconds?: number) {
        this.log(`typing command: ${command}`)
        await this.page.keyboard.press('Control+l')
        await this.page.keyboard.type(command)
        await this.page.keyboard.press('Enter')
        if (expectedInTerminal) {
            await this.expectTerminalText(expectedInTerminal, timeoutSeconds)
        }
    }

    async expectTerminalText(expected: string | string[], timeoutSeconds?: number) {
        this.log(`verifying terminal test: ${expected}`)
        const options = timeoutSeconds ? {timeout: timeoutSeconds * 1000} : undefined

        if (expected instanceof Array) {
            for (const text of expected) {
                await expect(this.appLocator, `Verify terminal text: ${text}`).toContainText(text, options)
            }
        } else {
            await expect(this.appLocator, `Verify terminal text: ${expected}`).toContainText(expected, options)
        }
    }

    async waitForPrompt() {
        await this.page.locator('css=.terminalCaret').waitFor()
    }

    async wait(waitSeconds: number, reason: string) {
        this.log(`waiting for: ${waitSeconds} seconds. Reason: ${reason}`)
        await this.page.waitForTimeout(waitSeconds * 1000)
    }

    async verifyScreenshotCanvas(name: string) {
        this.log(`verifying screenshot: ${name}`)
        await expect(this.page).toHaveScreenshot(`${name}.png`, this.screenshotOptions)
    }

    async deleteAllRuns(site: string) {
        this.log(`deleting all runs for: ${site}`)
        this.confirmDialog("Confirm that you want to delete all runs for this site? This will also reset the site (refresh ICE, etc.)")
        await this.page.getByRole('row').filter({hasText: site}).getByTitle("Delete all runs").click()
        await expect(this.appLocator, `Verify delete all success noty`).toContainText("runs removed")
    }

    async resetRuns(site: string) {
        this.log(`resetting runs for: ${site}`)
        this.confirmDialog("Confirm that you want to reset")
        await this.page.getByRole('row').filter({hasText: site}).getByTitle("Reset site").click()
        await expect(this.appLocator, `Verify site reset success noty`).toContainText("site reset")
    }

    private confirmDialog(expected: string) {
        this.page.once('dialog', dialog => {
            this.log(`Dialog message received: ${dialog.message()}`)
            expect(dialog.message(), `Verify dialog message: ${expected}`).toContain(expected)
            dialog.accept().catch(() => {
            })
        })

    }

    async verifyAppText(text: string) {
        this.log(`Verify text on page via App locator to contain ${text}`)
        await expect(this.appLocator, `Verify text on page via App locator to contain ${text}`).toContainText(text)
    }

    async setConfigItem(name: string, value: string) {
        this.log(`DialSetting config item: ${name} to ${value}`)
        await this.page.getByText(name).click();
        await this.page.locator('#configItemText').click();
        await this.page.locator('#configItemText').fill(value);
        await this.page.getByRole('button', {name: 'Save'}).click();
    }

    // --- helper methods -- //

    log(message: string) {
        console.log(message)
    }

}

