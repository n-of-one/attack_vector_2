import {expect, Page} from "@playwright/test"
import {PageAssertionsToHaveScreenshotOptions} from "playwright/types/test"


export class AvPage {
    page: Page

    screenshotOptions: PageAssertionsToHaveScreenshotOptions = {
        clip: {
            x: 652, y: 24, width: 1256, height: 850 // canvas area
        }
    }

    constructor(page: Page) {
        this.page = page
    }

    async loginUsingLink(username: string,) {
        await this.page.goto('http://localhost:3000/redirectToLogin')
        await this.page.getByRole('link', {name: username}).click()
        await expect(this.page.getByText(`{${username}}`)).toBeVisible()
    }

    async loginByTyping(username: string) {
        await this.page.goto('http://localhost:3000/redirectToLogin')
        await this.page.locator('#handle').click()
        await this.page.locator('#handle').fill(username)
        await this.page.locator('#handle').press('Enter')
        await expect(this.page.getByText(`{${username}}`)).toBeVisible()
    }

    async loginOrCreateUserAndLogin(username: string) {
        await this.page.goto('http://localhost:3000/redirectToLogin')
        await this.page.locator('#handle').click()
        await this.page.locator('#handle').fill(username)
        await this.page.locator('#handle').press('Enter')
        const userDoesNotExist = await this.page.getByText('Username not found').isVisible()

        if (userDoesNotExist) {
            await this.loginUsingLink("gm")
            await this.page.getByText('Users').click()
            await this.page.getByRole('textbox', { name: 'User name' }).fill('tangle')
            await this.page.getByRole('button', { name: 'Create' }).click()
            await this.page.getByRole('link', { name: 'ꕻ Logout' }).click()
            await this.loginByTyping(username)
        }
        else {
            await expect(this.page.getByText(`{${username}}`)).toBeVisible()
        }
    }


    async startNewRun(siteName: string) {
        await this.page.getByRole('textbox', {name: 'Site name'}).click()
        await this.page.getByRole('textbox', {name: 'Site name'}).fill(siteName)
        await this.page.getByRole('textbox', {name: 'Site name'}).press('Enter')
        await expect(this.page.getByText("Site:")).toContainText(siteName)
        await this.waitForPrompt()
    }

    async joinExistingRun(siteName: string) {
        await this.page.getByText(siteName).click()
        await expect(this.page.getByText("Site:")).toContainText(siteName)
        await this.waitForPrompt()
    }


    async typeCommand(command: string) {
        await this.page.keyboard.press('Control+l')
        await this.page.keyboard.type(command)
        await this.page.keyboard.press('Enter')
    }

    async expectTerminalText(expected: string | string[], timeoutSeconds?: number) {
        const options = timeoutSeconds ? {timeout: timeoutSeconds * 1000} : undefined

        if (expected instanceof Array) {
            for (const text of expected) {
                await expect(this.page.locator('#app')).toContainText(text, options)
            }
        } else {
            await expect(this.page.locator('#app')).toContainText(expected, options)
        }
    }

    async waitForPrompt() {
        await this.page.locator('css=.terminalCaret').waitFor()
    }

    async wait(waitSeconds: number) {
        await this.page.waitForTimeout(waitSeconds * 1000)
    }

    async verifyScreenshotCanvas(name: string) {
        await expect(this.page).toHaveScreenshot(`${name}.png`, this.screenshotOptions)
    }

    async deleteAllRuns(site: string) {
        this.page.once('dialog', dialog => {
            console.log(`Dialog message: ${dialog.message()}`)
            dialog.accept().catch(() => {
            })
        })
        await this.page.getByRole('row').filter({hasText: site}).getByTitle("Delete all runs").click()
        await expect(this.page.locator('#app')).toContainText("Site reset")
    }
}

