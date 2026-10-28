import {Page} from "@playwright/test";
import {expect} from "./fixtures"

import {log} from "./utils/testUtils";

export class AdminPage {
    page: Page

    constructor(page: Page) {
        this.page = page
    }

    async setConfigItem(name: string, value: string) {
        log(`Setting config item: ${name} to ${value}`)
        await this.configItemLink(name).click();
        await this.page.locator('#configItemText').click();
        await this.page.locator('#configItemText').fill(value);
        await this.page.getByRole('button', {name: 'Save'}).click();
        await this.verifyConfigItemValue(name, value)
    }

    // Matching on exact text and scoped to the table, because some config item names (for example "Path") are a substring of other text on this page.
    private configItemLink(name: string) {
        return this.page.locator('#sitesTable').getByText(name, {exact: true})
    }

    private async verifyConfigItemValue(name: string, value: string) {
        if (value === "") return // an empty value cannot be distinguished from the previous value in the table

        const row = this.page.locator('#sitesTable tr', {has: this.page.getByText(name, {exact: true})})
        await expect(row, `Verify config item ${name} is stored as: ${value}`).toContainText(value)
    }
}
