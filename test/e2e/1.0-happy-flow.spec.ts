import {expect, Page, test} from '@playwright/test'
import {AvPage} from "./testframework/AvPage"

// test.use({
//     viewport: {
//         height: 945,
//         width: 1920
//     }
// })

test('1.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(10_000);

    const av = new AvPage(page)
    await av.loginUsingLink("gm")
    await av.deleteAllRuns("dev")
})


test('1.1 - Fresh run and basic commands', async ({page}: { page: Page }) => {
    test.setTimeout(70_000);

    const av = new AvPage(page)
    await av.loginUsingLink("hacker")
    await av.startNewRun("dev")

    // Site loads correctly. Animation takes about 1s to complete.
    await av.wait(1.5)
    await av.verifyScreenshotCanvas("1.1-outside-dev-unscanned")

    // Command help outside
    await av.typeCommand("help")
    await av.waitForPrompt()
    await av.expectTerminalText(["You are outside of the site.",
        "There are only a few commands you can use when outside:",
        "scan",
        "attack",
        "/share <user name> (optionally more usernames)"])

    // Scan from outside
    await av.typeCommand("scan")
    await av.expectTerminalText("New nodes discovered: 7", 10.0)
    await av.wait(4.0)// time to render/animate all nodes and connections
    await av.verifyScreenshotCanvas("1.1-outside-dev-scanned")

    // Attack
    await av.typeCommand("attack")
    await av.expectTerminalText(["- Persona creation complete",
        "Entering node",
        "Connection established."], 10.0)
    await av.waitForPrompt()
    await av.verifyScreenshotCanvas("1.1-inside-dev")

    // Command help inside
    await av.typeCommand("help")
    await av.expectTerminalText(["You are inside the site.",
        "view", "move", "hack <layer>", "scan", "password", "dc", "/share <user name>",
        "For information on shortcuts, type: help shortcuts",
    ], 10.0)

    // Command help shortcuts
    await av.typeCommand("help shortcuts")
    await av.expectTerminalText(["scroll through previous commands", "clear the terminal"])

    // Command view
    await av.typeCommand("view")
    await av.expectTerminalText(["Node name: entry", "0 OS ", "1 Text layer", "2 Process", "3 Database"])

    // Command move
    await av.typeCommand("move 01")
    await av.waitForPrompt()
    await av.wait(0.5) // give it some extra time to finish animations
    await av.verifyScreenshotCanvas("1.1-inside-dev-moved-01")
    await av.typeCommand("view")
    await av.expectTerminalText(["0 unknown (shielded by ICE)", "1 Gaanth ICE", "2 Keystore"])

    // Command move
    await av.typeCommand("move 07")
    await av.expectTerminalText("error no path from current node to 07.")
    await av.typeCommand("move 02")
    await av.expectTerminalText("blocked ICE in current node is blocking your move.")
    await av.typeCommand("move 01")
    await av.expectTerminalText("error already at 01.")
    await av.typeCommand("move 00") // move back is always allowed
    await av.waitForPrompt()
    await av.typeCommand("view")
    await av.expectTerminalText("Node name: entry")

    // Command share
    await av.typeCommand("/share angler")
    await av.expectTerminalText("Shared run with Angler.")

    // Command dc
    await av.typeCommand("dc")
    await av.expectTerminalText("Disconnected")
    await av.wait(3.0) // Nodes animate to scan mode
    await av.verifyScreenshotCanvas("1.1-outside-dev-scanned") // Verify site visuals back to scan mode
})


test('1.2 - Joining an existing run and checking services', async ({page}: { page: Page }) => {
    test.setTimeout(50_000);

    const av = new AvPage(page)
    await av.loginByTyping("angler")
    await av.joinExistingRun("dev")

    // Command: quick attack
    await av.typeCommand("qa")
    await av.expectTerminalText("Persona established, hack started")
    await av.waitForPrompt()

    await av.typeCommand("view")
    await av.expectTerminalText("Node name: entry")

    // Hack OS layer
    await av.typeCommand("hack 0")
    await av.expectTerminalText("Hacking OS reveals nothing new")

    // Hack Text layer
    await av.typeCommand("hack 1")
    await av.expectTerminalText(["Hacked: 1 Text layer", "The butler did it."])

    // Command quickmove
    await av.typeCommand("qmove 01")
    await av.waitForPrompt()
    await av.typeCommand("view")
    await av.expectTerminalText("2 Keystore")

    // Hack keystore
    await av.typeCommand("hack 2")
    await av.expectTerminalText("hacked. Password found: Gaanth/")

    // Trigger & hack tripwire
    await av.typeCommand("qmove 07")
    await av.waitForPrompt()
    await av.expectTerminalText("1 triggered site reset in 10 seconds")
    await av.typeCommand("hack 1")
    await av.expectTerminalText("This tripwire is managed by core in node 07")

    // Hack core
    await av.typeCommand("hack 2")
    await av.expectTerminalText(["Hacked: 2 Core", "- stopped timer for this site"])

    // Trigger tripwire and let it reset the site
    await av.typeCommand("qmove 00")
    await av.waitForPrompt()
    await av.typeCommand("qmove 07")
    await av.waitForPrompt()
    await expect(page.locator('#app')).toContainText('00:00:06 [Site] ➜ shutdown for 15 seconds');
    await expect(page.locator('#app')).toContainText('00:00:05 [Site] ➜ shutdown for 15 seconds');
    await expect(page.locator('#app')).toContainText('00:00:04 [Site] ➜ shutdown for 15 seconds');
    await expect(page.locator('#app')).toContainText('00:00:03 [Site] ➜ shutdown for 15 seconds');
    await expect(page.locator('#app')).toContainText('00:00:02 [Site] ➜ shutdown for 15 seconds');
    await av.expectTerminalText(["Site down for maintenance for 15 seconds", "Disconnected (server abort)"])

    await av.wait(5.0) // Nodes animate to scan mode quickly
    await av.verifyScreenshotCanvas("1.2-outside-dev-scanned (angler)")
    await av.typeCommand("attack")
    await av.expectTerminalText("Connection refused. (site is in shutdown mode)")
    await av.wait(10.0)
    await av.expectTerminalText("Site connection available again")
    await av.typeCommand("qa")
    await av.expectTerminalText("Persona established, hack started")

})

