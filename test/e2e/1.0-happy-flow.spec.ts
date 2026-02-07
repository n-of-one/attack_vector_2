import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {HackerPage, START_ATTACK_QUICK, START_ATTACK_REGULAR} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";
import {wait} from "./testframework/utils/testUtils";
import {LoginPage} from "./testframework/LoginPage";

test('1.0.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(10_000);

    const [login, gm] = [new LoginPage(page), new GmPage(page)]

    await login.loginUsingLink("gm")
    await gm.deleteAllRuns("dev")
})


test('1.0.1 - Fresh run and basic commands', async ({page}: { page: Page }) => {
    test.setTimeout(70_000);

    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Log in a hacker and start run", async () => {
        await login.loginUsingLink("hacker")
        await hacker.startNewRun("dev")
    })

    await test.step("Verify site loads correctly", async () => {
        await hacker.verifyScreenshotCanvas("1.0-outside-dev-unscanned")
    })


    await test.step("Command help outside", async () => {
        await hacker.typeCommand("help")
        await hacker.expectTerminalText(["You are outside of the site.",
            "There are only a few commands you can use when outside:",
            "scan",
            "attack",
            "/share <user name> (optionally more usernames)"])
    })

    await test.step("Scan from outside", async () => {
        await hacker.typeCommand("scan", "New nodes discovered: 7", 10.0)
        await hacker.waitForScanAnimationFinished()
        await hacker.verifyScreenshotCanvas("1.0-outside-dev-scanned")
    })

    await test.step("Attack", async () => {
        await hacker.startAttack(START_ATTACK_REGULAR, "Entered node 00: entry")
        await hacker.verifyScreenshotCanvas("1.0-inside-dev")
    })

    await test.step("Command help inside", async () => {
        await hacker.typeCommand("help",
            ["You are inside the site.",
                "view", "move", "hack <layer>", "scan", "password", "dc", "/share <user name>",
                "For information on shortcuts, type: help shortcuts",
            ], 10.0)
    })

    await test.step("Command help shortcuts", async () => {
        await hacker.typeCommand("help shortcuts",
            ["scroll through previous commands", "clear the terminal"])
    })

    await test.step("Command view", async () => {
        await hacker.typeCommand("view",
            ["Node name: entry", "0 OS ", "1 Text"])
    })

    await test.step("Command move - basic", async () => {
        await hacker.typeCommand("move 01")
        await wait(page, 0.5, "wait for move animations to finish")
        await hacker.verifyScreenshotCanvas("1.0-inside-dev-moved-01")
        await hacker.typeCommand("view",
            ["0 unknown (shielded by ICE)", "1 Gaanth ICE", "2 Keystore"])
    })

    await test.step("Command move - unhappy flow and backtracking", async () => {
        await hacker.typeCommand("move 07", "error no path from current node to 07.")
        await hacker.typeCommand("move 02", "blocked ICE in current node is blocking your move.")
        await hacker.typeCommand("move 01", "error already at 01.")
        await hacker.typeCommand("move 00") // move back is always allowed
        await hacker.typeCommand("view", "Node name: entry")
    })

    await test.step("Command share", async () => {
        await hacker.typeCommand("/share angler")
        await hacker.expectTerminalText("Shared run with Angler.")
    })

    await test.step("Command dc", async () => {
        await hacker.typeCommand("dc", "Disconnected")
        await wait(page, 3.0, "Nodes animate to scan mode")
        await hacker.verifyScreenshotCanvas("1.0-outside-dev-scanned") // Verify site visuals back to scan mode
    })
})


test('1.0.2 - Joining an existing run and checking services', async ({page}: { page: Page }) => {
    test.setTimeout(50_000);

    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Join dev as angler via shared run link", async () => {
        await login.loginByTyping("angler")
        await hacker.joinExistingRun("dev")
    })

    await test.step("Command: quick attack", async () => {
        await hacker.startAttack(START_ATTACK_QUICK, "Entered node 00: entry" )
        await hacker.typeCommand("view", "Node name: entry")
    })

    await test.step("Hack OS layer", async () => {
        await hacker.typeCommand("hack 0", "Hacking OS reveals nothing new")
    })

    await test.step("Hack Text layer", async () => {
        await hacker.typeCommand("hack 1",
            ["Hacked: 1 Text", "The butler did it."])
    })

    await test.step("Command quickmove", async () => {
        await hacker.typeCommand("qmove 01")
        await hacker.typeCommand("view", "2 Keystore")
    })

    await test.step("Hack keystore", async () => {
        await hacker.typeCommand("hack 2", "hacked. Password found: Gaanth/")
    })

    await test.step("Trigger & hack tripwire", async () => {
        await hacker.typeCommand("qmove 07", "1 triggered site reset in 10 seconds")
        await hacker.typeCommand("hack 1", "This tripwire is managed by core in node 07")
    })

    await test.step("Hack core", async () => {
        await hacker.typeCommand("hack 2",
            ["Hacked: 2 Core", "- stopped timer for this site"])
    })

    await test.step("Trigger tripwire and let it reset the site", async () => {
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 07")
        await hacker.verifyAppText('00:00:07');
        await hacker.verifyAppText('[Site] ➜ shutdown for 15 seconds');
        await hacker.verifyAppText('00:00:04');
        await hacker.verifyAppText('00:00:02');
        await hacker.expectTerminalText(["Site down for maintenance for 15 seconds", "Disconnected (server abort)"])

        await wait(page, 5.0, "Nodes animate to scan mode quickly")
        await hacker.verifyScreenshotCanvas("1.0-outside-dev-resetting-angler")
        await hacker.typeCommand("attack")
        await hacker.expectTerminalText("Connection refused. (site is in shutdown mode)")
        await wait(page, 10.0, "Wait for shutdown to complete")
        await hacker.expectTerminalText("Site connection available again")
    })
    await test.step("Attack site again", async () => {
        await hacker.startAttack(START_ATTACK_QUICK)
    })
})