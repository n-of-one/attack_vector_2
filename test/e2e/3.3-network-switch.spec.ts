import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";
import {wait} from "./testframework/utils/testUtils";

const SITE_NAME = "test-network-switches"

/*
 Test site: 00 is the start node, 05 has the core (layer 1) that unlocks both network switches.
 01 has a network switch (layer 1) with routes research (02) and archives (03).
 06 has a network switch (layer 1) with routes vault (07) and labs (08). It is locked to vault in 3.3.0.

           09               04
           |                |
     07    08               02     03
       \   |                  \   /
        \  |                   01
         \ |                   |
          06 ---------------- 00 ---- 05
*/

test('3.3.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, gm, hacker] = [new LoginPage(page), new GmPage(page), new HackerPage(page)]

    await test.step("Import site", async () => {
        await login.loginUsingLink("gm")
        await gm.importSiteIfItDoesNotExist(SITE_NAME, "v1-test-network-switches.json")
    })

    await test.step("Unlock both switches and lock switch 06 to vault, whatever a previous test run left (a site reset does not change switches)", async () => {
        await login.loginUsingLink("hacker")
        await hacker.startNewRun(SITE_NAME)
        await hacker.typeCommand("qs")
        await hacker.waitForScanAnimationFinished()
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 05")
        await hacker.typeCommand("hack 1", "Hacked: 1 Core")
        await hacker.typeCommand("qmove 06")
        await hacker.typeCommand("hack 1 vault", "Topology change switch 06 locked to vault. Dropped routes: 08")
    })

    await test.step("Remove all runs (also those of previous test runs), so the test starts with an unscanned site", async () => {
        await login.loginUsingLink("gm")
        await gm.deleteAllRuns(SITE_NAME)
    })
})


test('3.3.1 - Network switches', async ({page}: { page: Page }) => {
    test.setTimeout(90_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Scan passes the pending routes of 01, but stops at the dropped route to 08: 01 to 08 are discovered, 09 is not", async () => {
        await login.loginUsingLink("hacker")
        await hacker.startNewRun(SITE_NAME)
        await hacker.typeCommand("scan", "New nodes discovered: 8", 10)
        await hacker.waitForScanAnimationFinished()
    })

    await test.step("Routes are in standby until a choice is made", async () => {
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("move 01", "Entered node 01", 10)
        await hacker.typeCommand("move 02", "blocked route to 02 held in standby by a network switch.")
        await hacker.typeCommand("move 03", "blocked route to 03 held in standby by a network switch.")
        await wait(page, 0.5, "wait for move animations to finish")
        await hacker.verifyScreenshotCanvas("3.3-switch-routes-in-standby")
    })

    await test.step("Hacking the switch shows the routing table", async () => {
        await hacker.typeCommand("hack 1", ["Routing table:", "hack 1 research", "hack 1 archives", "switch firmware locks after first route commit"])
        await hacker.typeCommand("hack 1 north", ["route error no route named north.", "hack 1 research"])
    })

    await test.step("Choosing a route opens it and drops the other route", async () => {
        await hacker.typeCommand("hack 1 research", "Topology change switch 01 locked to research. Dropped routes: 03")
        await wait(page, 2.5, "wait for the connections to change color")
        await hacker.verifyScreenshotCanvas("3.3-switch-locked-to-research")
        await hacker.typeCommand("move 03", "route down route to 03 was dropped by a network switch.")
        await hacker.typeCommand("move 02", "Entered node 02: research", 10)
        await hacker.typeCommand("move 04", "Entered node 04: behind research", 10)
    })

    await test.step("A locked switch shows which core manages the lock", async () => {
        await hacker.typeCommand("qmove 01", "Entered node 01")
        await hacker.typeCommand("hack 1", ["Switch locked. Route fixed to research", "Lock managed by core in node 05"])
    })

    await test.step("Hacking the core unlocks both switches", async () => {
        await hacker.typeCommand("qmove 05", "Entered node 05")
        await hacker.typeCommand("hack 1", ["Hacked: 1 Core", "- unlocked network switch in node 01", "- unlocked network switch in node 06",
            "Topology change switch 01 unlocked by core. Routes back in standby."])
    })

    await test.step("After unlocking, the routes are in standby again and another route can be chosen", async () => {
        await hacker.typeCommand("move 00", "Entered node 00", 10)
        await hacker.typeCommand("move 01", "Entered node 01", 10)
        await hacker.typeCommand("move 02", "blocked route to 02 held in standby by a network switch.")
        await hacker.typeCommand("hack 1 archives", "Topology change switch 01 locked to archives. Dropped routes: 02")
        await hacker.typeCommand("move 02", "route down route to 02 was dropped by a network switch.")
        await hacker.typeCommand("move 03", "Entered node 03: archives", 10)
    })

    await test.step("08 was only seen by the scan. Locking switch 06 to labs makes it connectable, so it can be entered without a new scan", async () => {
        await hacker.typeCommand("qmove 06", "Entered node 06")
        await hacker.typeCommand("hack 1 labs", "Topology change switch 06 locked to labs. Dropped routes: 07")
        await hacker.typeCommand("move 08", "Entered node 08: labs", 10)
    })
})
