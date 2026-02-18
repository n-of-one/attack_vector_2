import {Locator, Page} from "@playwright/test"
import {expect} from "./fixtures"

import {AvPage} from "./AvPage";
import {closeAllPopups, log, wait} from "./utils/testUtils";


export class ScriptManagementPage {
    page: Page
    av: AvPage

    constructor(page: Page) {
        this.page = page
        this.av = new AvPage(page)
    }

    async deleteExistingScriptIfItExists(scriptName: string) {
        await this.navigateToScriptTypeManagement()
        await this.deleteScriptTypeIfItExists(scriptName)
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
        await this.deleteScriptRows(rows)
    }

    async deleteAllScriptsFromHacker() {
        log(`Delete scripts of this hacker`)

        await this.navigateToCurrentScriptsOf('hacker')

        const rows = this.page.locator('[data-row="hacker-script"]')
        await this.deleteScriptRows(rows)
    }

    private async deleteScriptRows(locator: Locator) {
        while (await locator.count() > 0) {
            const rowToDelete = locator.first()
            const scriptCode = await rowToDelete.locator('[data-element="code"]').innerText();
            await rowToDelete.getByTitle('delete').click()
            await expect(locator.locator('[data-element="code"]', {hasText: scriptCode})).toHaveCount(0)
        }

    }

    async navigateToScriptTypeManagement() {
        log(`Navigate to script type management`)
        await this.av.navigateToMenuItem('Scripts')
        await this.page.locator('a').filter({hasText: 'Script types'}).click()
        await expect(this.page.getByText('Manage script types'), "Verify showing script types").toBeVisible()
        await expect(this.page.getByText('Category'), "Verify script types loaded by checking header rows scripts table").toBeVisible()
    }

    async deleteScriptTypeIfItExists(scriptName: string) {
        log(`Delete existing script type if it exists: ${scriptName}`)
        const existingScript = this.page.getByText(scriptName)
        const exists = await existingScript.isVisible()

        if (exists) {
            await this.deleteScript(scriptName);
        }
    }

    private async deleteScript(scriptName: string) {
        await this.page.getByText(scriptName).click()
        await this.verifyShowingScriptDetails(scriptName)
        await this.page.getByText('Delete type').click()

        const deleteSuccess = await this.wasScriptDeletionSuccessfulInOneGo(scriptName)

        if (!deleteSuccess) {
            log(`Force deleting script type: ${scriptName}`)
            await this.page.locator(".btn", {hasText: "Delete type (force)"}).click()
        }

        await expect(this.page.getByText('Create new script type'), "Verify script page closed").toBeVisible()
        await expect(this.page.getByText(`Script type ${scriptName} deleted`), "Verify script deletion confirmation popup").toBeVisible()
        await closeAllPopups(this.page)
    }

    private async wasScriptDeletionSuccessfulInOneGo(scriptTypeName: string) {
        let scriptTypeDeleted: boolean
        let deleteForceButton: boolean

        do {
            scriptTypeDeleted = await this.page.locator("#app", {hasText: `Script type ${scriptTypeName} deleted`}).count() > 0;
            deleteForceButton = await this.page.locator(".btn", {hasText: "Delete type (force)"}).count() > 0;
            await wait(this.page, 0.1, "await delete processing")
        }
        while (!deleteForceButton && !scriptTypeDeleted)
        return scriptTypeDeleted;


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

    async addUsefulEffect(effectKey: string, effectName: string, value?: string) {
        await this.addEffect(effectKey, effectName, "Useful effect", value)
    }

    async addDrawbackEffect(effectKey: string, effectName: string, value?: string) {
        await this.addEffect(effectKey, effectName, "Drawback effect", value)
    }

    async addEffect(effectKey: string, effectName: string, type: string, value?: string) {
        log(`Adding use effect: ${effectName}`)
        const usefulEffectRow = this.page.locator('[data-row="add-effect"]', {hasText: type})
        await usefulEffectRow.getByRole('combobox').selectOption(effectKey)
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

        await expect(rows, `Verify all script ${scriptName} added to hacker's scripts`).toHaveCount(count)

        while (await rows.count() > 0) {
            const rowsToLoad = rows.first()
            const scriptCode = await rowsToLoad.locator('[data-element="code"]').innerText();
            await rowsToLoad.getByTitle('Instant load in memory').click()
            await expect(rows.locator('[data-element="code"]', {hasText: scriptCode})).toHaveCount(0)
        }
    }
}
