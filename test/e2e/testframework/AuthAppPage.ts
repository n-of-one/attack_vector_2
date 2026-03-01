import {Page} from "@playwright/test"
import {expect} from "./fixtures"
import {wait} from "./utils/testUtils";

export class AuthAppPage {
    page: Page

    constructor(page: Page) {
        this.page = page
    }

    async verifyAuthorizationRequired() {
        this.log("Verify 'Authorization required' heading")
        await expect(this.page.getByText("Authorization required"), "Verify heading").toBeVisible()
    }

    async verifyPromptText() {
        this.log("Verify 'Please enter Password to continue'")
        await expect(this.page.getByText("Please enter Password to continue"), "Verify prompt text").toBeVisible()
    }

    async waitForPasswordInput() {
        this.log("Wait for password input to be ready")
        await expect(this.page.locator('.form-control:not([disabled])'), "Wait for enabled password input").toBeVisible()
    }

    async submitPassword(password: string) {
        this.log(`Submit password: ${password}`)
        await this.page.locator('.form-control').fill(password)
        await this.page.locator('.ice-input-submit').click()
        await wait(this.page, 0.5, "wait for password submission response")
    }

    async verifyLocked(seconds: number) {
        this.log(`Verify locked with ${seconds} seconds wait`)
        await expect(this.page.getByText(`Please wait ${seconds} seconds`), `Verify lockout message with ${seconds}s`).toBeVisible()
    }

    async waitForUnlock() {
        this.log("Wait for lockout to expire (unlock)")
        await expect(this.page.getByText("Please wait"), "Wait for lockout to disappear").not.toBeVisible({timeout: 6_000})
        await this.waitForPasswordInput()
    }

    async verifyHintVisible(hint: string) {
        this.log(`Verify hint is visible: ${hint}`)
        await expect(this.page.getByText(`Hint: ${hint}`), `Verify hint text: ${hint}`).toBeVisible()
    }

    async verifyHintNotVisible() {
        this.log("Verify hint is not visible")
        await expect(this.page.getByText("Hint:"), "Verify hint is not visible").not.toBeVisible()
    }

    async verifyAuthenticationSuccess() {
        this.log("Verify authentication success")
        await expect(this.page.getByText("Authentication success"), "Verify success message").toBeVisible()
    }

    async verifyIceBanner(iceName: string) {
        this.log(`Verify ICE banner: Protected by ${iceName}`)
        await expect(this.page.getByText(`Protected by ${iceName}`), `Verify ICE banner with ${iceName}`).toBeVisible()
    }

    // --- helper methods -- //

    log(message: string) {
        console.log(message)
    }
}
