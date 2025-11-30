import {chromium} from '@playwright/test';
import {AdminPage} from "./testframework/AdminPage";
import {LoginPage} from "./testframework/LoginPage";
import {wait} from "./testframework/utils/testUtils";

export default async function globalSetup() {
    const browser = await chromium.launch()
    const page = await browser.newPage()
    const [login, admin] = [new LoginPage(page), new AdminPage(page)]

    await login.loginUsingLink("admin")
    await admin.setConfigItem("Testing mode", "true")
    await admin.setConfigItem("Simulate non-localhost", "0")

    await wait(page, 0.1, "don't close the page immediately to allow changes to be sent by browser")
    await browser.close()
}