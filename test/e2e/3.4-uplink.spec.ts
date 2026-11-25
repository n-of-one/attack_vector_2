import {Page} from '@playwright/test'
import {expect, test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";
import {appLocator, wait} from "./testframework/utils/testUtils";

const SITE_A = "test-uplink-a"
const SITE_B = "test-uplink-b"

/*
 Site A: 00 is the start node. 01 has two uplinks to site B: layer 1 to its start node, layer 2 (back door) to its node 03.
 Site B is internal: 00 is the start node, 01 has ICE, 03 is the vault.

    A:  01 (uplinks → B 00, B 03)       B:  03 vault
        |                                   |
        00                                  01 hallway (ICE)  --  02 records
                                            |
                                            00 entry
*/

const hackUplink = async (page: Page, hacker: HackerPage, layer: number, expectedArrival: string) => {
    await hacker.typeCommandDontWaitForPrompt(`hack ${layer}`)
    await expect(page.getByText("Site:"), `Verify page to contain ${SITE_B}`).toContainText(SITE_B, {timeout: 10_000})
    await hacker.expectTerminalText(["Establishing uplink carrier...", "Carrier established, health 100%", `Routing through to ${SITE_B}.`], 20)
    await hacker.expectTerminalText(expectedArrival, 10)
    await hacker.waitForPrompt()
}

const disconnectBackToA = async (page: Page, hacker: HackerPage) => {
    await hacker.typeCommandDontWaitForPrompt("dc", [`Uplink to ${SITE_B} dropped.`, "Upgrading connection to persona matrix...",
        `Session restored at node 01 of ${SITE_A}`], 10)
    await expect(page.getByText("Site:"), `Verify page to contain ${SITE_A}`).toContainText(SITE_A, {timeout: 10_000})
    await hacker.expectTerminalText("Entered node 01: gateway", 10)
    await hacker.waitForPrompt()
}

const goToUplinksInA = async (hacker: HackerPage) => {
    await hacker.startNewRun(SITE_A)
    await hacker.startAttack(START_ATTACK_QUICK)
    await hacker.typeCommand("qmove 01", "Entered node 01: gateway", 10)
}

test('3.4.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, gm] = [new LoginPage(page), new GmPage(page)]

    await test.step("Delete the sites of previous test runs, A first because a site cannot be deleted while an uplink connects to it", async () => {
        await login.loginUsingLink("gm")
        await gm.deleteSiteIfItExists(SITE_A)
        await gm.deleteSiteIfItExists(SITE_B)
    })

    await test.step("Import sites, B first so the uplinks in A find their target", async () => {
        await gm.importSite(SITE_B, "test-uplink-b.json")
        await gm.importSite(SITE_A, "test-uplink-a.json")
    })

    await test.step("Make sites hackable, B can only be made hackable after A is imported (internal site without uplink has errors)", async () => {
        await gm.makeSiteHackable(SITE_A)
        await gm.makeSiteHackable(SITE_B)
    })

    await test.step("Remove the runs of previous test runs: deleting a site keeps its runs, and an uplink continues a run the hacker has on the target", async () => {
        await gm.deleteAllRuns(SITE_A)
        await gm.deleteAllRuns(SITE_B)
    })
})


test('3.4.1 - Uplink to the start node', async ({page}: { page: Page }) => {
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

    await test.step("The uplinks show their target", async () => {
        await goToUplinksInA(hacker)
        await hacker.typeCommand("view", [`1 Uplink -> ${SITE_B}`, `2 Back door -> ${SITE_B}`])
    })

    await test.step("Hacking the uplink shows B right away, the hacker connects to its start node while the connection sequence runs", async () => {
        await hackUplink(page, hacker, 1, "Entered node 00: entry")
    })

    await test.step("The scan stops at the ICE in the hallway: the records node is seen, the vault is not revealed", async () => {
        await hacker.expectTerminalText("New nodes discovered: 3")
        await wait(page, 1, "wait for scan and arrive animations to finish")
        await hacker.verifyScreenshotCanvas("3.4-uplink-to-start-node")
    })

    await test.step("A run on an internal site cannot be shared", async () => {
        await hacker.typeCommand("/share angler", "failed to share run, no direct route to site.")
    })

    await test.step("Disconnecting from B returns the hacker to the uplink node in A", async () => {
        await disconnectBackToA(page, hacker)
    })

    await test.step("The run on B is not listed on the home page", async () => {
        await page.getByText('Home').click();
        await expect(appLocator(page), "Verify the run on A is listed").toContainText(SITE_A)
        await expect(appLocator(page), "Verify the run on B is not listed").not.toContainText(SITE_B)
    })
})


test('3.4.2 - Uplink to the back door', async ({page}: { page: Page }) => {
    test.setTimeout(90_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Hacking the back door connects the hacker to the vault, behind the ICE in the hallway", async () => {
        await login.loginUsingLink("angler")
        await goToUplinksInA(hacker)
        await hackUplink(page, hacker, 2, "Entered node 03: vault")
    })

    await test.step("The scan from the vault stops at the ICE in the hallway: the start node is known, but not scanned", async () => {
        await hacker.expectTerminalText("New nodes discovered: 2")
        await wait(page, 1, "wait for scan and arrive animations to finish")
        await hacker.verifyScreenshotCanvas("3.4-uplink-to-back-door")
    })

    await test.step("The hacker can walk from the vault to the ICE in the hallway", async () => {
        await hacker.typeCommand("move 01", "Entered node 01: hallway", 10)
    })

    await test.step("Disconnecting from B returns the hacker to the uplink node in A", async () => {
        await disconnectBackToA(page, hacker)
    })
})


test('3.4.3 - Uplink continues the run the hacker already has on the target', async ({page}: { page: Page }) => {
    test.setTimeout(90_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("The hacker entered B at the start node in 3.4.1. Via the back door from a new run on A, they continue that run on B", async () => {
        await login.loginUsingLink("hacker")
        await goToUplinksInA(hacker)
        await hackUplink(page, hacker, 2, "Entered node 03: vault")
    })

    await test.step("Nothing new is discovered at the vault, and the start node is scanned (from 3.4.1)", async () => {
        await hacker.expectTerminalText("New nodes discovered: 0")
        await wait(page, 1, "wait for scan and arrive animations to finish")
        await hacker.verifyScreenshotCanvas("3.4-uplink-to-back-door-continued-run")
    })
})
