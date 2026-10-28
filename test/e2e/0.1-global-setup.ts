import {chromium} from '@playwright/test';
import {AdminPage} from "./testframework/AdminPage";
import {LoginPage} from "./testframework/LoginPage";
import {GmPage} from "./testframework/GmPage";
import {SKILL_SCRIPT_CREDITS, SKILL_SCRIPT_RAM, UsersPage} from "./testframework/UsersPage";
import {wait} from "./testframework/utils/testUtils";
import * as path from "node:path";

/**
 * The tests expect the same setup that V07_DevelopmentDefaults creates. That migration step only runs when the application is started in
 * development mode, which is often not the case. So this setup performs the steps of V07 itself, via the UI.
 *
 * Everything here is idempotent: it is safe to run against a server that did run V07, and it is safe to run repeatedly.
 */

const DEV_SITE = "dev"
const DEV_SITE_FILE = "v1-dev.json"
const DEV_SITE_DIRECTORY = path.join(__dirname, "..", "..", "backend", "src", "main", "resources", "sites")

export default async function globalSetup() {
    console.log("Start global setup")
    const browser = await chromium.launch()
    const page = await browser.newPage()
    const [login, admin, gm, users] = [new LoginPage(page), new AdminPage(page), new GmPage(page), new UsersPage(page)]

    // Bootstrap: /adminLogin is the only login page that is available before the login path is configured below.
    await login.loginAsAdmin()
    await configureForDevelopment(admin)

    // The admin cannot manage sites, and the login links are available now that the login path is set to /devLogin.
    await login.loginUsingLink("gm")
    await gm.importSiteIfItDoesNotExist(DEV_SITE, DEV_SITE_FILE, DEV_SITE_DIRECTORY)
    await addSkillsAndIcon(users, "hacker", "BIRD_1")
    await addSkillsAndIcon(users, "angler", "SHARK")

    await wait(page, 0.1, "don't close the page immediately to allow changes to be sent by browser")
    await browser.close()
    console.log("Finish global setup")
}

async function configureForDevelopment(admin: AdminPage) {
    await admin.setConfigItem("Larp name", "dev")
    await admin.setConfigItem("Path", "/devLogin")

    await admin.setConfigItem("Hackers can reset sites", "true")
    await admin.setConfigItem("Hackers can use dev commands", "true")

    await admin.setConfigItem("Show skills", "true")
    await admin.setConfigItem("Edit user name", "true")
    await admin.setConfigItem("Edit character name", "true")
    await admin.setConfigItem("Tutorial site name", "") // empty: hackers should not be sent into the tutorial
    await admin.setConfigItem("Script loading during run", "true")
    await admin.setConfigItem("Script lockout duration", "1:00")
    await admin.setConfigItem("Script RAM refresh duration", "0:10")

    // These deviate from V07: the tests need a system without delays.
    await admin.setConfigItem("Testing mode", "true")
    await admin.setConfigItem("ICE quick playing", "true")
    await admin.setConfigItem("Simulate non-localhost", "0")
    await admin.setConfigItem("Minimum shutdown duration", "00:00:10")
}

// The icons are used in the screenshots of the e2e tests.
async function addSkillsAndIcon(users: UsersPage, userName: string, icon: string) {
    await users.openUser(userName)
    await users.setHackerIcon(icon)
    await users.addSkillWithValue(SKILL_SCRIPT_CREDITS, "1000")
    await users.addSkillWithValue(SKILL_SCRIPT_RAM, "100")
}
