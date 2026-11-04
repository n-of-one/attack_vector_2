import {Locator, Page} from "@playwright/test";
import {expect} from "./fixtures"

import {AvPage} from "./AvPage";
import {log} from "./utils/testUtils";

export const SKILL_SCRIPT_CREDITS = "SCRIPT_CREDITS"
export const SKILL_SCRIPT_RAM = "SCRIPT_RAM"
export const SKILL_TRAIT = "TRAIT"
const TRAIT_DEFAULT_KEY = "key"

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

    /** Adds a TRAIT skill with this key and text, unless the hacker already has a TRAIT skill with this key. */
    async addTraitSkillIfMissing(key: string, text: string) {
        log(`Adding trait skill ${key}: ${text} (if missing)`)
        await expect(this.page.locator('[data-row="add-skill"]'), "Verify user details are loaded").toBeVisible()
        const traitRows = this.page.locator(`[data-row="hacker-skill"][data-skill="${SKILL_TRAIT}"]`)
        const keyInputs = traitRows.locator('input[name="traitKey"]')
        const currentKeys = () => keyInputs.evaluateAll((inputs) => inputs.map((input) => (input as HTMLInputElement).value))

        const existingKeys = await currentKeys()
        if (existingKeys.includes(key)) return

        const addSkillRow = this.page.locator('[data-row="add-skill"]')
        await addSkillRow.locator('#addSkill').selectOption(SKILL_TRAIT)
        await addSkillRow.getByText('Add', {exact: true}).click()
        await expect(traitRows, `Verify a new trait skill is added`).toHaveCount(existingKeys.length + 1)

        const newIndex = (await currentKeys()).indexOf(TRAIT_DEFAULT_KEY)
        expect(newIndex, `Verify new trait skill has default key: ${TRAIT_DEFAULT_KEY}`).toBeGreaterThanOrEqual(0)
        const newRow = traitRows.nth(newIndex)

        // Each input saves the combined value, so wait for the server to confirm the first save before editing the second input.
        await this.saveInput(newRow, 'input[name="traitKey"]', key)
        await this.saveInput(newRow, 'input[name="traitText"]', text)

        await expect.poll(currentKeys, {message: `Verify trait skill has key: ${key}`}).toContain(key)
    }

    private async saveInput(row: Locator, inputSelector: string, value: string) {
        const input = row.locator(inputSelector)
        await input.fill(value)
        await input.press('Enter') // this blurs the input, which triggers the save
        await expect(row.locator('.saveIcon'), `Verify save of ${value} is confirmed by server`).toHaveCount(0)
        await expect(input, `Verify input has value: ${value}`).toHaveValue(value)
    }

    async removeAllSkillsOfType(skillType: string) {
        log(`Removing all skills of type ${skillType}`)
        await expect(this.page.locator('[data-row="add-skill"]'), "Verify user details are loaded").toBeVisible()
        const skillRows = this.page.locator(`[data-row="hacker-skill"][data-skill="${skillType}"]`)

        let count = await skillRows.count()
        while (count > 0) {
            await skillRows.first().getByTitle('Delete').click()
            await expect(skillRows, `Verify skill ${skillType} is removed`).toHaveCount(count - 1)
            count -= 1
        }
    }
}

const exactIgnoringCase = (text: string) => new RegExp(`^${text}$`, "i")
