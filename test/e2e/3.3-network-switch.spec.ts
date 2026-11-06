import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";
import {wait} from "./testframework/utils/testUtils";

const SITE_NAME = "test-network-switch"

/*
 Test site: 00 is the start node, 01 has the network switch (layer 1) with routes research (02) and archives (03).
 05 has the core (layer 1) that unlocks the switch.

     04
     |
     02     03
       \   /
        01
        |
        00 -- 05
*/

test('3.3.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, gm, hacker] = [new LoginPage(page), new GmPage(page), new HackerPage(page)]

    await test.step("Import site and remove old runs", async () => {
        await login.loginUsingLink("gm")
        await gm.importSiteIfItDoesNotExist(SITE_NAME, "v1-test-network-switch.json")
        await gm.deleteAllRuns(SITE_NAME)
    })

    await test.step("Unlock the switch, in case a previous test run left it locked (a site reset does not unlock it)", async () => {
        await login.loginUsingLink("hacker")
        await hacker.startNewRun(SITE_NAME)
        await hacker.typeCommand("qs")
        await hacker.waitForScanAnimationFinished()
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 05")
        await hacker.typeCommand("hack 1", "Hacked: 1 Core")
    })

    await test.step("Remove the run, so the next test starts with an unscanned site", async () => {
        await login.loginUsingLink("gm")
        await gm.deleteAllRuns(SITE_NAME)
    })
})


test('3.3.1 - Scan penetrates a network switch that is not locked', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Regular scan from outside discovers the nodes behind the switch: 01 to 05", async () => {
        await login.loginUsingLink("hacker")
        await hacker.startNewRun(SITE_NAME)
        await hacker.typeCommand("scan", "New nodes discovered: 5", 10)
        await hacker.waitForScanAnimationFinished()
    })
})


test('3.3.2 - Choose a route', async ({page}: { page: Page }) => {
    test.setTimeout(40_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Routes are in standby until a choice is made", async () => {
        await login.loginUsingLink("hacker")
        await hacker.joinExistingRun(SITE_NAME)
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
})


test('3.3.3 - Unlock the network switch via the core', async ({page}: { page: Page }) => {
    test.setTimeout(40_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Hacking the core unlocks the switch", async () => {
        await login.loginUsingLink("hacker")
        await hacker.joinExistingRun(SITE_NAME)
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("move 05", "Entered node 05", 10)
        await hacker.typeCommand("hack 1", ["Hacked: 1 Core", "- unlocked network switch in node 01",
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

    await test.step("Unlock the switch again, so the next test run starts with an unlocked switch", async () => {
        await hacker.typeCommand("qmove 05", "Entered node 05")
        await hacker.typeCommand("hack 1", "- unlocked network switch in node 01")
    })
})


test('3.3.4 - Cleanup', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, gm] = [new LoginPage(page), new GmPage(page)]

    await test.step("Remove runs", async () => {
        await login.loginUsingLink("gm")
        await gm.deleteAllRuns(SITE_NAME)
    })
})
