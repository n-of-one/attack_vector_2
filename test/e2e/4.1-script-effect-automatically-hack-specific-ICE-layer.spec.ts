import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {ScriptManagementPage} from "./testframework/ScriptManagementPage";
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";

const SCRIPT_NAME = "hackicespecific"


async function startHack(login: LoginPage, hacker: HackerPage) {
    await test.step("Setup", async () => {
        await login.loginUsingLink("hacker")
        await hacker.joinExistingRun("dev")
        await hacker.startAttack(START_ATTACK_QUICK)
    })
}

test('4.1.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, gm, scriptManagement, hacker] = [new LoginPage(page), new GmPage(page), new ScriptManagementPage(page), new HackerPage(page)]

    await test.step("Delete existing scripts", async () => {
        await login.loginUsingLink("gm")
        await scriptManagement.deleteAllScriptsFromHacker()
        await scriptManagement.deleteExistingScriptIfItExists(SCRIPT_NAME)
    })

    await test.step("Prepare run", async () => {
        await gm.deleteAllRuns("dev")
        await login.loginUsingLink("hacker")
        await hacker.startNewRun("dev")
        await hacker.typeCommand("qs")
        await hacker.waitForScanAnimationFinished()
    })
})

test('4.1.1 - Create script and give to hacker', async ({page}: { page: Page }) => {
    test.setTimeout(5_000);
    const [login, scriptManagement] = [new LoginPage(page), new ScriptManagementPage(page)]

    await test.step("Create script", async () => {
        await login.loginUsingLink("gm")
        await scriptManagement.createScript(SCRIPT_NAME)
        await scriptManagement.addUsefulEffect("AUTO_HACK_SPECIFIC_ICE_LAYER", "Automatically hack a specific ICE layer", "node-c421-4498:layer-d20a")
        await scriptManagement.verifyHackerEffectDescription(SCRIPT_NAME, "Automatically hack layer 1 of node: 04 of site: dev")
    })

    await test.step("Give script to hacker", async () => {
        await scriptManagement.navigateToCurrentScriptsOf("hacker")
        await scriptManagement.addScriptAndLoad(SCRIPT_NAME, 3)
    })
})

test('4.1.2 - Script works on intended ICE', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await startHack(login, hacker)

    const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)

    await hacker.verifyScreenshotCanvas("node-protected")

    await test.step("Run script on ICE it isn't intended for", async () => {
        await hacker.typeCommand("qmove 02")

        const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)
        await hacker.typeCommand(`run ${scriptCode} 1`)

        await hacker.expectTerminalText("Script cannot interact with this layer.")
    })

    await test.step("Run script and verify effect on ICE it is intended for", async () => {
        await hacker.typeCommand("qmove 04")

        await hacker.typeCommand(`run ${scriptCode} 1`)

        await hacker.waitForNodeHackedAnimationFinished()
        await hacker.expectTerminalText("ICE hack complete.")
        await hacker.verifyScreenshotCanvas("node-hacked")
    })


})

// test('4.1.3 - Script does not work on other ICE', async ({page}: { page: Page }) => {
//     test.setTimeout(15_000);
//
//     const [login, hacker] = [new LoginPage(page), new HackerPage(page)]
//
//     await startHack(login, hacker)
//
//     await test.step("Run script and verify effect", async () => {
//         await hacker.verifyScreenshotCanvas("node-protected")
//
//         await hacker.typeCommand("qmove 04")
//
//         const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)
//         await hacker.typeCommand(`run ${scriptCode} 1`)
//
//         await hacker.waitForNodeHackedAnimationFinished()
//         await hacker.expectTerminalText("ICE hack complete.")
//         await hacker.verifyScreenshotCanvas("node-hacked")
//     })
// })