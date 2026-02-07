import {chromium} from '@playwright/test';
import {AdminPage} from "./testframework/AdminPage";
import {LoginPage} from "./testframework/LoginPage";
import {wait} from "./testframework/utils/testUtils";

export default async function globalSetup() {
    console.log("Start global setup")
    const browser = await chromium.launch()
    const page = await browser.newPage()
    const [login, admin] = [new LoginPage(page), new AdminPage(page)]

    await login.loginUsingLink("admin")
    await admin.setConfigItem("Testing mode", "true")
    await admin.setConfigItem("Simulate non-localhost", "0")
    await admin.setConfigItem("Minimum shutdown duration", "00:00:10")

    await wait(page, 0.1, "don't close the page immediately to allow changes to be sent by browser")
    await browser.close()
    console.log("Finish global setup")
}