import {Page} from "@playwright/test"
import {expect} from "./fixtures"

import {AvPage} from "./AvPage";
import {log, wait} from "./utils/testUtils";


export class ScriptManagementPage {
    page: Page
    av: AvPage

    constructor(page: Page) {
        this.page = page
        this.av = new AvPage(page)
    }

    async deleteExistingScriptIfItExists(scriptName: string) {
        await this.navigateToCurrentScriptsOf('hacker')
        await this.deleteAllScriptsFromHackerWithName(scriptName)

        await this.navigateToScriptTypeManagement()
        await this.deleteScriptIfItExists(scriptName)
    }

    async navigateToCurrentScriptsOf(hackerName: string) {
        log(`Navigate to current scripts of ${hackerName}`)
        await this.av.navigateToMenuItem('Scripts')

        await this.page.locator('a').filter({hasText: 'Current scripts'}).click()
        await this.page.locator('a').filter({hasText: hackerName}).click()

        await expect(this.page.getByText('Current scripts of: hacker'), `Verify showing scripts of ${hackerName}`).toBeVisible()
        await expect(this.page.getByText('Code'), "Verify scripts loaded by checking for 'Code' header in scripts table").toBeVisible()
    }

    async deleteAllScriptsFromHackerWithName(scriptName: string) {
        log(`Delete scripts with name ${scriptName} for this hacker`)
        const rows = this.page.locator('[data-row="hacker-script"]', {hasText: scriptName})
        while (await rows.count() > 0) {
            const rowToDelete = rows.first()
            const scriptCode = await rowToDelete.locator('[data-element="code"]').innerText();
            await rowToDelete.getByTitle('delete').click()
            await expect(rows.locator('[data-element="code"]', {hasText: scriptCode})).toHaveCount(0)
        }
    }

    async navigateToScriptTypeManagement() {
        log(`Navigate to script type management`)
        await this.av.navigateToMenuItem('Scripts')
        await this.page.locator('a').filter({hasText: 'Script types'}).click()
        await expect(this.page.getByText('Manage script types'), "Verify showing script types").toBeVisible()
        await expect(this.page.getByText('Category'), "Verify script types loaded by checking header rows scripts table").toBeVisible()
    }

    async deleteScriptIfItExists(scriptName: string) {
        log(`Delete existing script if it exists: ${scriptName}`)
        const existingScript = this.page.getByText(scriptName)
        const exists = await existingScript.isVisible()

        if (exists) {
            await this.page.getByText(scriptName).click();
            await this.verifyShowingScriptDetails(scriptName)
            await this.page.getByText('Delete type').click();

            await expect(this.page.getByText('Create new script type'), "Verify script page closed").toBeVisible()

            await expect(this.page.getByText(scriptName), `Script type ${scriptName} deleted`).toBeVisible()
        }
    }

    private async verifyShowingScriptDetails(scriptName: string) {
        await expect(this.page.getByRole('textbox', {name: "Name"}), `Verify showing script details of ${scriptName}`).toHaveValue(scriptName)
    }


    async createScript(scriptName: string) {
        log(`Create script with name: ${scriptName}`)
        await this.navigateToScriptTypeManagement()
        await this.page.getByRole('textbox', {name: 'type name'}).fill(scriptName)
        await this.page.getByRole('button', {name: 'Create'}).click()
        await this.verifyShowingScriptDetails(scriptName)
    }

    async addUsefulEffect(effectName: string, value?: string) {
        log(`Adding use effect: ${effectName}`)
        const usefulEffectRow = this.page.locator('[data-row="add-effect"]', {hasText: "Useful effect"})
        await usefulEffectRow.getByRole('combobox').selectOption('AUTO_HACK_SPECIFIC_ICE_LAYER')
        await usefulEffectRow.locator('a').click()

        if (value) {
            log(`  Setting effect value to ${value}`)
            const effectRow = this.page.locator('[data-row="script-effect-one-row"]', {hasText: effectName}).getByRole('textbox')
            await effectRow.click()
            await effectRow.fill(value)
            await effectRow.press('Enter')
        }
    }

    async verifyHackerEffectDescription(scriptName: string, expectedDescription: string) {
        log(`Verify description of effect of: ${scriptName} is: ${expectedDescription}`)
        const scriptRowLocator = this.page
            .locator('[data-row="script"]', { hasText: scriptName})
            .locator('[data-element="effect-descriptions"]', { hasText: "1"} )
            .locator(".badge")

        await scriptRowLocator.hover()
        await expect(this.page.getByText(expectedDescription)).toBeVisible()
    }

    async addScriptAndLoad(scriptName: string, count: number = 1) {
        log(`Add script: ${scriptName} and loading in RAM`)

        for (let i = 0; i < count; i++) {
            await this.page.locator('[data-row="script"]', { hasText: scriptName }).getByText(scriptName).click()
            await wait(this.page, 0.05, "Wait a little after adding a script to allow system to process the click")
        }

        const rows = this.page.locator('[data-row="hacker-script"]', {
            hasText: scriptName,
            has: this.page.getByTitle('Instant load in memory')
        })

        while (await rows.count() > 0) {
            const rowsToLoad = rows.first()
            const scriptCode = await rowsToLoad.locator('[data-element="code"]').innerText();
            await rowsToLoad.getByTitle('Instant load in memory').click()
            await expect(rows.locator('[data-element="code"]', {hasText: scriptCode})).toHaveCount(0)
        }
    }
}
