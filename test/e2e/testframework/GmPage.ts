import {expect, Page} from "@playwright/test";
import {appLocator, confirmDialog, log} from "./utils/testUtils";

export class GmPage {
    page: Page

    constructor(page: Page) {
        this.page = page
    }

    async deleteAllRuns(site: string) {
        log(`Delete all runs for: ${site}`)
        confirmDialog(this.page, "Confirm that you want to delete all runs for this site? This will also reset the site (refresh ICE, etc.)")
        await this.page.getByRole('row').filter({hasText: site}).getByTitle("Delete all runs").click()
        await expect(appLocator(this.page), `Verify delete all success noty`).toContainText("runs removed")
    }
}