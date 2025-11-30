import {Page} from "@playwright/test";
import {log} from "./utils/testUtils";

export class AdminPage {
    page: Page

    constructor(page: Page) {
        this.page = page
    }

    async setConfigItem(name: string, value: string) {
        log(`DialSetting config item: ${name} to ${value}`)
        await this.page.getByText(name).click();
        await this.page.locator('#configItemText').click();
        await this.page.locator('#configItemText').fill(value);
        await this.page.getByRole('button', {name: 'Save'}).click();
    }

}