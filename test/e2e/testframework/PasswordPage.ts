import {Page} from "@playwright/test"
import {expect} from "./fixtures"
import {wait} from "./utils/testUtils";

export class PasswordPage {
    page: Page

    constructor(page: Page) {
        this.page = page
    }

    async verifyIceName() {
        this.log("Verify ICE name is Rahasy")
        await expect(this.page.getByText("Ice:"), "Verify ICE name to be Rahasy.").toContainText("Rahasy")
    }

    async waitForPasswordPrompt() {
        this.log("Wait for password input to be ready")
        await this.page.locator('[data-testid="passwordContentSection"][data-ready="true"]').waitFor()
    }

    async submitPassword(password: string) {
        this.log(`Submit password: ${password}`)
        await this.page.keyboard.type(password)
        await this.page.keyboard.press('Enter')
        await wait(this.page, 0.5, "wait for password submission response")
    }

    async verifyPasswordRejected(password: string) {
        this.log(`Verify password "${password}" was rejected`)
        await expect(this.page.getByText(`Password incorrect: ${password}`), `Verify rejection notification for "${password}"`).toBeVisible()
    }

    async verifyPasswordAttemptListed(password: string) {
        this.log(`Verify password attempt "${password}" in list`)
        await expect(this.page.locator('li', {hasText: password}), `Verify "${password}" in attempts list`).toBeVisible()
    }

    async verifyHintNotVisible() {
        this.log("Verify hint is not visible")
        await expect(this.page.getByText("Password hint:"), "Verify hint is not visible").not.toBeVisible()
    }

    async verifyHintVisible(hint: string) {
        this.log(`Verify hint is visible: ${hint}`)
        await expect(this.page.getByText("Password hint:"), "Verify hint is visible").toBeVisible()
        await expect(this.page.getByText("Password hint:"), `Verify hint text contains: ${hint}`).toContainText(hint)
    }

    async verifyAttemptCount(count: number) {
        this.log(`Verify attempt count is ${count}`)
        await expect(this.page.locator('li'), `Verify ${count} attempts in list`).toHaveCount(count)
    }

    async verifyTimeoutBetweenPasswordAttempts(seconds: number) {
        this.log(`Verify timeout shown with ${seconds} seconds`)
        const timeoutElement = this.page.locator('h4.text-warning')
        await expect(timeoutElement, "Verify timeout is shown").toBeVisible()
        await expect(timeoutElement, `Verify timeout value is ${seconds}`).toContainText(`${seconds}`)
    }

    async waitForUnlock() {
        this.log("Wait for timeout to expire (unlock)")
        await expect(this.page.getByText("Time-out:"), "Wait for timeout to disappear").not.toBeVisible({timeout: 6_000})
        await this.waitForPasswordPrompt()
    }

    async verifyPasswordAccepted() {
        this.log("Verify password accepted")
        await this.verifyTerminalText("Password accepted")
        await this.verifyTerminalText("ICE grants access")
    }

    async verifyTerminalText(text: string) {
        this.log(`Verify terminal text to contain: ${text}`)
        await expect(this.page.locator(".terminalPanel").first(), `Verify ICE terminal text to contain: ${text}`).toContainText(text)
    }

    // --- helper methods -- //

    log(message: string) {
        console.log(message)
    }

}
