import {Page} from "@playwright/test";
import {expect} from "./fixtures"

import {AvPage} from "./AvPage";
import {log} from "./utils/testUtils";

export const SKILL_SCRIPT_CREDITS = "SCRIPT_CREDITS"
export const SKILL_SCRIPT_RAM = "SCRIPT_RAM"

export class UsersPage {
    page: Page
    av: AvPage

    constructor(page: Page) {
        this.page = page
        this.av = new AvPage(page)
    }

    async openUser(userName: string) {
        log(`Open user: ${userName}`)
        await this.av.navigateToMenuItem("Users")

        // Only the user name is a link, the character name next to it is not. Both can hold the same text, so matching on the link avoids ambiguity.
        const userLink = this.page.locator('.rightPanel a').filter({hasText: exactIgnoringCase(userName)})
        await userLink.click()

        await expect(this.page.locator('#name'), `Verify details of ${userName} are shown`).toHaveValue(exactIgnoringCase(userName))
    }

    async setHackerIcon(icon: string) {
        log(`Setting hacker icon to: ${icon}`)
        const iconDropDown = this.page.locator('#hackerIcon')
        await iconDropDown.selectOption(icon)

        // The drop down only shows the new icon after the server confirmed the change.
        await expect(iconDropDown, `Verify icon is set to ${icon}`).toHaveValue(icon)
    }

    /** Adds the skill if this hacker does not have it yet, and sets its value. */
    async addSkillWithValue(skillType: string, value: string) {
        log(`Adding skill ${skillType} with value: ${value}`)
        const skillRow = this.page.locator(`[data-row="hacker-skill"][data-skill="${skillType}"]`)

        if (await skillRow.count() === 0) {
            const addSkillRow = this.page.locator('[data-row="add-skill"]')
            await addSkillRow.locator('#addSkill').selectOption(skillType)
            await addSkillRow.getByText('Add', {exact: true}).click()
            await expect(skillRow, `Verify skill ${skillType} is added`).toHaveCount(1)
        }

        const valueInput = skillRow.getByRole('textbox')
        await valueInput.fill(value)
        await valueInput.press('Enter') // this blurs the input, which triggers the save
        await expect(valueInput, `Verify skill ${skillType} has value: ${value}`).toHaveValue(value)
    }
}

const exactIgnoringCase = (text: string) => new RegExp(`^${text}$`, "i")
