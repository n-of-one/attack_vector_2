import {Page} from "@playwright/test";
import {expect} from "./fixtures"

import {appLocator, confirmDialog, log} from "./utils/testUtils";
import {AvPage} from "./AvPage";
import * as path from "node:path";

export class GmPage {
    page: Page
    av: AvPage

    constructor(page: Page) {
        this.page = page
        this.av = new AvPage(page)
    }

    async deleteAllRuns(site: string) {
        log(`Delete all runs for: ${site}`)
        await this.av.navigateToMenuItem("Sites")
        confirmDialog(this.page, "Confirm that you want to delete all runs for this site? This will also reset the site (refresh ICE, etc.)")
        await this.page.getByRole('row').filter({hasText: site}).getByTitle("Delete all runs").click()
        await expect(appLocator(this.page), `Verify delete all success noty`).toContainText("runs removed")
    }

    async importSiteIfItDoesNotExist(siteName: string, fileName: string, directory: string = path.join(__dirname, "..", "sites")) {
        log(`Load site if it does not yet exist: ${siteName}`)
        await this.av.navigateToMenuItem("Sites")

        // Make sure sites are shown in the browser, before checking if our site needs to be uploaded. Any site will do: the tutorial site is
        // created by the migrations, so there is always at least one row once the list has been received from the server.
        await expect(this.page.locator('#sitesTable tbody tr').first(), "Verify the site list has loaded").toBeVisible()

        const siteExists = await this.page.getByText(siteName, {exact: true}).isVisible()
        log(`Site (${siteName}) exists: ${siteExists}`)

        if (!siteExists) {
            await this.page.getByRole('button', { name: 'Choose File' }).setInputFiles(path.join(directory, fileName));
            await this.page.getByRole('button', { name: 'Upload' }).click();
            await expect(appLocator(this.page), `Verify import success noty`).toContainText(`Imported site: ${siteName}`)
            await this.page.getByText('[close]').click();

            // make site hackable
            const siteHackableCheckboxLocator = this.page.locator(`role=cell[name="${siteName}"]  >> role=checkbox`)
            await siteHackableCheckboxLocator.click();
            await expect(siteHackableCheckboxLocator).toBeChecked()
        }
    }
}