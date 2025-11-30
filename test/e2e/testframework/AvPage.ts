import {expect, Page} from "@playwright/test";
import {log} from "./utils/testUtils";

export class AvPage {
    page: Page

    constructor(page: Page) {
        this.page = page
    }

    async navigateToMenuItem(name: string) {
        log(`Navigating to ${name}`)
        await this.page.locator(".nav-item").getByText(name).click()
        await expect(this.page.locator(".nav-item.active")).toBeVisible()
    }
}