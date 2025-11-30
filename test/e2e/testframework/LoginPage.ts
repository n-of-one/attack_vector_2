import {expect, Page} from "@playwright/test"
import {log} from "./utils/testUtils";


export class LoginPage {
    page: Page

    constructor(page: Page) {
        this.page = page
    }

    async loginUsingLink(username: string,) {
        log(`Logging in by using link: ${username}`)
        await this.page.goto('http://localhost:3000/redirectToLogin')
        await this.page.getByRole('link', {name: username}).click()
        await this.verifyUsernameVisible(username)
    }

    async loginByTyping(username: string) {
        log(`Logging in by typing username: ${username}`)
        await this.page.goto('http://localhost:3000/redirectToLogin')
        await this.page.locator('#handle').click()
        await this.page.locator('#handle').fill(username)
        await this.page.locator('#handle').press('Enter')
        await this.verifyUsernameVisible(username)
    }

    async loginOrCreateUserAndLogin(username: string) {
        log(`Logging in by typing OR creating user: ${username}`)
        await this.page.goto('http://localhost:3000/redirectToLogin')
        await this.page.locator('#handle').click()
        await this.page.locator('#handle').fill(username)
        await this.page.locator('#handle').press('Enter')
        log(`Attempting to log in as: ${username}`)
        const userDoesNotExist = await this.page.getByText('Username not found').isVisible()

        if (userDoesNotExist) {
            log(`User does not exist, creating this user`)
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

}

