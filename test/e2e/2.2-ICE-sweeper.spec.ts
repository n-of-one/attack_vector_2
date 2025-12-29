import {test} from "./testframework/fixtures"
import {TanglePage} from "./testframework/TanglePage";
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {wait} from "./testframework/utils/testUtils";
import {SweeperPage} from "./testframework/SweeperPage";

test('test', async ({page}) => {
    test.setTimeout(70_000);

    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Log in as hacker and join existing run dev", async () => {
        await login.loginUsingLink("hacker")
        await hacker.resetRuns("dev")
        await hacker.joinExistingRun("dev")
    })

    await test.step("Start Sweeper ICE puzzle in node 05", async () => {
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 05")
        await hacker.typeCommandDontWaitForPrompt("hack 1")
    })

    const icePagePromise = page.waitForEvent('popup');
    const icePage = await icePagePromise;
    const sweeper = new SweeperPage(icePage)


    await test.step("Verify ICE state before hacking", async () => {
        // await wait(page, 0.5, "Give ICE page some time to render")
        await wait(page, 3.0, "Wait for ICE puzzle to fully render") //disable before re-making the screenshot
        await sweeper.checkNameAndScreenshot("2.2-sweeper-before")
    })

    await test.step("Blow up mine and reset puzzle", async () => {
        await sweeper.verifyMinesLeft(8)
        await sweeper.leftClickToIndicateNoMine(642, 174)
        await sweeper.verifyMinesLeft(7)
        // await wait(page, 3.0, "Wait for ICE puzzle to fully render") //disable before re-making the screenshot
        await sweeper.verifyTerminalText("Warning corrupted memory accessed, hacker blocked")
        await sweeper.verifyTerminalText("Press (and hold) reset to restart this hack")
        await sweeper.checkNameAndScreenshot("2.2-sweeper-blown up")



        await sweeper.pressReset(2)
        await wait(page, 1.0, "Wait for reset abort messages to render")
        await sweeper.verifyTerminalText("Reset initiated by hacker")
        await sweeper.verifyTerminalText("Reset aborted. Keep holding reset to complete the reset")
        await sweeper.pressResetUntilComplete()
        await sweeper.verifyTerminalText("Reset completed by hacker")
        await wait(page, 4.0, "Wait for ICE page to reload and re-render")
        await sweeper.checkNameAndScreenshot("2.2-sweeper-before")
    })

    await test.step("Solve tangle ICE puzzle", async () => {
        await sweeper.leftClickToIndicateNoMine(1495, 401)

        // not mines
        await sweeper.leftClickToIndicateNoMine(1078, 159)
        await sweeper.leftClickToIndicateNoMine(391, 159)
        await sweeper.leftClickToIndicateNoMine(390, 847)


        await wait(page, 1.0, "Wait for sweeper to finish animating a single click") //disable before re-making the screenshot
        await sweeper.checkNameAndScreenshot("2.2-sweeper-after-first-reveal")

        // click mines
        await sweeper.verifyMinesLeft(8)
        await sweeper.rightClickToIndicateMine(642, 174)
        await sweeper.verifyMinesLeft(7)
        await sweeper.rightClickToIndicateMine(561, 339)
        await sweeper.verifyMinesLeft(6)
        await sweeper.rightClickToIndicateMine(382, 340)
        await sweeper.verifyMinesLeft(5)
        await sweeper.rightClickToIndicateMine(897, 757)
        await sweeper.verifyMinesLeft(4)
        await sweeper.rightClickToIndicateMine(839, 762)
        await sweeper.verifyMinesLeft(3)
        await sweeper.rightClickToIndicateMine(653, 682)
        await sweeper.verifyMinesLeft(2)
        await sweeper.rightClickToIndicateMine(723, 498)
        await sweeper.verifyMinesLeft(1)
        await sweeper.rightClickToIndicateMine(389, 595)
        await sweeper.verifyMinesLeft(0)

        await wait(page, 1.0, "Wait for sweeper to finish animating a single click") //disable before re-making the screenshot
        await sweeper.checkNameAndScreenshot("2.2-sweeper-after-all-mines-clicked")

        // not mines
        await sweeper.leftClickToIndicateNoMine(890, 843)
        await sweeper.leftClickToIndicateNoMine(810, 847)
        await sweeper.leftClickToIndicateNoMine(741, 668)
        await sweeper.leftClickToIndicateNoMine(728, 604)
        await sweeper.leftClickToIndicateNoMine(586, 421)
        await sweeper.leftClickToIndicateNoMine(491, 413)
        await sweeper.leftClickToIndicateNoMine(493, 335)
        await sweeper.leftClickToIndicateNoMine(401, 412)
        await sweeper.leftClickToIndicateNoMine(423, 509)
        await sweeper.leftClickToIndicateNoMine(463, 512)

    })

    await test.step("Verify ICE after hacking", async () => {
        await sweeper.checkNameAndScreenshot("2.2-sweeper-just-before-finish")
        await sweeper.leftClickToIndicateNoMine(549, 523)
        await sweeper.verifyPuzzleSolved()
    })

    await test.step("Verify node shown as hacked in site map", async () => {
        await hacker.waitForNodeHackedAnimationFinished()
        await hacker.verifyScreenshotCanvas("2.1-sweeper-ice-hacked")
    })
});