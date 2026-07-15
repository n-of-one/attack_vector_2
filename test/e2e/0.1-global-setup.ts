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
    await admin.setConfigItem("ICE quick playing", "true")
    await admin.setConfigItem("Simulate non-localhost", "0")
    await admin.setConfigItem("Minimum shutdown duration", "00:00:10")

    await wait(page, 0.1, "don't close the page immediately to allow changes to be sent by browser")
    await browser.close()
    console.log("Finish global setup")

    // TODO: load dev website
    // TODO: give hacker user scripts skill
    // TODO: give hacker the BIRD_1 icon
    // TODO: give angler the scripts skill
    // TODO: give angler the SHARK icon
    // TODO: hackers can use dev commands
    // TODO: hackers can reset sites
    // TODO: login: /devLogin
    // TODO: script loading during run: true
    // TODO: script lockout duration: 00:00:10
    // TODO: script ram refresh duration: 00:01:00


}