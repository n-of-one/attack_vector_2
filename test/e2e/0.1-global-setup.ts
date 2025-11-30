
import {AvPage} from "./testframework/AvPage"

import { chromium } from '@playwright/test';

export default async function globalSetup() {
    const browser = await chromium.launch()
    const page = await browser.newPage()
    const avPage = new AvPage(page)


    await avPage.loginUsingLink("admin")
    await avPage.setConfigItem("Testing mode", "true")
    await avPage.setConfigItem("Simulate non-localhost", "0")

    await avPage.wait(0.1) // don't close the page immediately.
    await browser.close()
}