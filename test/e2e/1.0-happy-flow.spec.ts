import {Page, test} from '@playwright/test'
import {AvPage} from "./testframework/AvPage"

test('1.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(10_000);

    const av = new AvPage(page)
    await av.loginUsingLink("gm")
    await av.deleteAllRuns("dev")
})


test('1.1 - Fresh run and basic commands', async ({page}: { page: Page }) => {
    test.setTimeout(70_000);

    const av = new AvPage(page)

    await test.step("Log in a hacker and start run", async () => {
        await av.loginUsingLink("hacker")
        await av.startNewRun("dev")
    })

    await test.step("Verify site loads correctly. Animation takes about 1s to complete", async () => {
        await av.wait(1.5, "Animation takes about 1s to complete")
        await av.verifyScreenshotCanvas("1.1-outside-dev-unscanned")
    })

    await test.step("Command help outside", async () => {
        await av.typeCommand("help")
        await av.expectTerminalText(["You are outside of the site.",
            "There are only a few commands you can use when outside:",
            "scan",
            "attack",
            "/share <user name> (optionally more usernames)"])
    })

    await test.step("Scan from outside", async () => {
        await av.typeCommand("scan", "New nodes discovered: 7", 10.0)
        await av.wait(4.0, "time to render/animate all nodes and connections")
        await av.verifyScreenshotCanvas("1.1-outside-dev-scanned")
    })

    await test.step("Attack", async () => {
        await av.typeCommand("attack",
            ["- Persona creation complete",
                "Connection established.",
                "Entered node 00: entry"], 10.0)
        await av.verifyScreenshotCanvas("1.1-inside-dev")
    })

    await test.step("Command help inside", async () => {
        await av.typeCommand("help",
            ["You are inside the site.",
                "view", "move", "hack <layer>", "scan", "password", "dc", "/share <user name>",
                "For information on shortcuts, type: help shortcuts",
            ], 10.0)
    })

    await test.step("Command help shortcuts", async () => {
        await av.typeCommand("help shortcuts",
            ["scroll through previous commands", "clear the terminal"])
    })

    await test.step("Command view", async () => {
        await av.typeCommand("view",
            ["Node name: entry", "0 OS ", "1 Text layer", "2 Process", "3 Database"])
    })

    await test.step("Command move - basic", async () => {
        await av.typeCommand("move 01")
        await av.wait(0.5, "give it some extra time to finish animations")
        await av.verifyScreenshotCanvas("1.1-inside-dev-moved-01")
        await av.typeCommand("view",
            ["0 unknown (shielded by ICE)", "1 Gaanth ICE", "2 Keystore"])
    })

    await test.step("Command move - unhappy flow and backtracking", async () => {
        await av.typeCommand("move 07", "error no path from current node to 07.")
        await av.typeCommand("move 02", "blocked ICE in current node is blocking your move.")
        await av.typeCommand("move 01", "error already at 01.")
        await av.typeCommand("move 00") // move back is always allowed
        await av.typeCommand("view", "Node name: entry")
    })

    await test.step("Command share", async () => {
        await av.typeCommand("/share angler")
        await av.expectTerminalText("Shared run with Angler.")
    })

    await test.step("Command dc", async () => {
        await av.typeCommand("dc", "Disconnected")
        await av.wait(3.0, "Nodes animate to scan mode")
        await av.verifyScreenshotCanvas("1.1-outside-dev-scanned") // Verify site visuals back to scan mode
    })
})


test('1.2 - Joining an existing run and checking services', async ({page}: { page: Page }) => {
    test.setTimeout(50_000);

    const av = new AvPage(page)

    await test.step("Join dev as angler via shared run link", async () => {
        await av.loginByTyping("angler")
        await av.joinExistingRun("dev")
    })

    await test.step("Command: quick attack", async () => {
        await av.typeCommand("qa", "Persona established, hack started")
        await av.typeCommand("view", "Node name: entry")
    })

    await test.step("Hack OS layer", async () => {
        await av.typeCommand("hack 0", "Hacking OS reveals nothing new")
    })

    await test.step("Hack Text layer", async () => {
        await av.typeCommand("hack 1",
            ["Hacked: 1 Text layer", "The butler did it."])
    })

    await test.step("Command quickmove", async () => {
        await av.typeCommand("qmove 01")
        await av.typeCommand("view", "2 Keystore")
    })

    await test.step("Hack keystore", async () => {
        await av.typeCommand("hack 2", "hacked. Password found: Gaanth/")
    })

    await test.step("Trigger & hack tripwire", async () => {
        await av.typeCommand("qmove 07", "1 triggered site reset in 10 seconds")
        await av.typeCommand("hack 1", "This tripwire is managed by core in node 07")
    })

    await test.step("Hack core", async () => {
        await av.typeCommand("hack 2",
            ["Hacked: 2 Core", "- stopped timer for this site"])
    })

    await test.step("Trigger tripwire and let it reset the site", async () => {
        await av.typeCommand("qmove 00")
        await av.typeCommand("qmove 07")
        await av.verifyAppText('00:00:06 [Site] ➜ shutdown for 15 seconds');
        await av.verifyAppText('00:00:05 [Site] ➜ shutdown for 15 seconds');
        await av.verifyAppText('00:00:04 [Site] ➜ shutdown for 15 seconds');
        await av.verifyAppText('00:00:03 [Site] ➜ shutdown for 15 seconds');
        await av.verifyAppText('00:00:02 [Site] ➜ shutdown for 15 seconds');
        await av.expectTerminalText(["Site down for maintenance for 15 seconds", "Disconnected (server abort)"])

        await av.wait(5.0, "Nodes animate to scan mode quickly")
        await av.verifyScreenshotCanvas("1.2-outside-dev-scanned (angler)")
        await av.typeCommand("attack")
        await av.expectTerminalText("Connection refused. (site is in shutdown mode)")
        await av.wait(10.0, "Wait for shutdown to complete")
        await av.expectTerminalText("Site connection available again")
    })
    await test.step("Attack site again", async () => {
        await av.typeCommand("qa", "Persona established, hack started")
    })
})

