import {Page} from '@playwright/test'
import {expect, test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";
import {appLocator} from "./testframework/utils/testUtils";

const SITE_A = "test-uplink-a"
const SITE_B = "test-uplink-b"

/*
 Site A: 00 is the start node, 01 has an uplink (layer 1) to site B.
 Site B is internal: 00 is the start node, 01 has a text layer.

    A:  01 (uplink → B)         B:  01
        |                           |
        00                          00
*/

test('3.4.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, gm] = [new LoginPage(page), new GmPage(page)]

    await test.step("Delete the sites of previous test runs, A first because a site cannot be deleted while an uplink connects to it", async () => {
        await login.loginUsingLink("gm")
        await gm.deleteSiteIfItExists(SITE_A)
        await gm.deleteSiteIfItExists(SITE_B)
    })

    await test.step("Import sites, B first so the uplink in A finds its target", async () => {
        await gm.importSite(SITE_B, "v1-test-uplink-b.json")
        await gm.importSite(SITE_A, "v1-test-uplink-a.json")
    })

    await test.step("Make sites hackable, B can only be made hackable after A is imported (internal site without uplink has errors)", async () => {
        await gm.makeSiteHackable(SITE_A)
        await gm.makeSiteHackable(SITE_B)
    })
})


test('3.4.1 - Uplink', async ({page}: { page: Page }) => {
    test.setTimeout(90_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("An internal site cannot be reached from outside", async () => {
        await login.loginUsingLink("hacker")
        await page.getByText('Home').click();
        const siteNameLocator = page.getByRole('textbox', {name: 'Site name'})
        await siteNameLocator.fill(SITE_B)
        await siteNameLocator.press('Enter')
        await hacker.verifyAppText(`Site '${SITE_B}' not found`)
    })

    await test.step("The uplink shows its target", async () => {
        await hacker.startNewRun(SITE_A)
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 01", "Entered node 01: gateway", 10)
        await hacker.typeCommand("view", `1 Uplink -> ${SITE_B}`)
    })

    await test.step("Hacking the uplink shows B right away, the hacker connects to its start node while the connection sequence runs", async () => {
        await hacker.typeCommandDontWaitForPrompt("hack 1")
        await expect(page.getByText("Site:"), `Verify page to contain ${SITE_B}`).toContainText(SITE_B, {timeout: 10_000})
        await hacker.expectTerminalText(["Establishing uplink carrier...", "Carrier established, health 100%", `Routing through to ${SITE_B}.`], 20)
        await hacker.expectTerminalText("Entered node 00: internal gateway", 10)
        await hacker.waitForPrompt()
    })

    await test.step("A run on an internal site cannot be shared", async () => {
        await hacker.typeCommand("/share angler", "failed to share run, no direct route to site.")
    })

    await test.step("Disconnecting from B returns the hacker to the uplink node in A", async () => {
        await hacker.typeCommandDontWaitForPrompt("dc", [`Uplink to ${SITE_B} dropped.`, "Upgrading connection to persona matrix...",
            `Session restored at node 01 of ${SITE_A}`], 10)
        await expect(page.getByText("Site:"), `Verify page to contain ${SITE_A}`).toContainText(SITE_A, {timeout: 10_000})
        await hacker.expectTerminalText("Entered node 01: gateway", 10)
        await hacker.waitForPrompt()
    })

    await test.step("The run on B is not listed on the home page", async () => {
        await page.getByText('Home').click();
        await expect(appLocator(page), "Verify the run on A is listed").toContainText(SITE_A)
        await expect(appLocator(page), "Verify the run on B is not listed").not.toContainText(SITE_B)
    })
})
