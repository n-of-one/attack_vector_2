import {test} from "./testframework/fixtures"
import {TanglePage} from "./testframework/TanglePage";
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {wait} from "./testframework/utils/testUtils";

test('test', async ({page}) => {
    test.setTimeout(70_000);

    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Log in as hacker and join existing run dev", async () => {
        await login.loginUsingLink("hacker")
        await hacker.resetRuns("dev")
        await hacker.joinExistingRun("dev")
    })

    await test.step("Start Tangle ICE puzzle in node 01", async () => {
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 01")
        await hacker.typeCommandDontWaitForPrompt("hack 1")
    })

    const icePagePromise = page.waitForEvent('popup');
    const icePage = await icePagePromise;
    const tangle = new TanglePage(icePage)


    await test.step("Verify ICE state before hacking", async () => {
        await wait(page, 0.5, "Give ICE page some time to render")
        // await hacker.wait(3.0, "Wait for ICE puzzle to fully render") //disable before re-making the screenshot
        await tangle.checkNameAndScreenshot("2.1-tangle-before")
    })

    await test.step("Solve tangle ICE puzzle", async () => {
        await tangle.moveTanglePoint(1495, 401, 1681, 823)
        await tangle.moveTanglePoint(1120, 127, 598, 886)
        await tangle.moveTanglePoint(1351, 841, 886, 896)
        await tangle.moveTanglePoint(745, 644, 767, 151)
    })

    await test.step("Verify ICE after hacking", async () => {
        await tangle.checkNameAndScreenshot("2.1-tangle-after")
        await tangle.verifyPuzzleSolved()
    })

    await test.step("Verify node shown as hacked in site map", async () => {
        await hacker.waitForNodeHackedAnimationFinished()
        await hacker.verifyScreenshotCanvas("2.1-tangle-ice-hacked")
    })
});