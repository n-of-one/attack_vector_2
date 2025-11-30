import {test} from '@playwright/test';
import {AvPage} from "./testframework/AvPage";
import {TanglePage} from "./testframework/TanglePage";

test('test', async ({page}) => {
    test.setTimeout(70_000);

    const av = new AvPage(page)

    await test.step("Log in as hacker and join existing run dev", async () => {
        await av.loginUsingLink("hacker")
        await av.resetRuns("dev")
        await av.joinExistingRun("dev")
    })

    await test.step("Start Tangel ICE puzzle in node 01", async () => {
        await av.typeCommand("qa")
        await av.typeCommand("qmove 01")
        await av.typeCommandDontWaitForPrompt("hack 1")
    })

    const icePagePromise = page.waitForEvent('popup');
    const icePage = await icePagePromise;
    const tanglePage = new TanglePage(icePage)


    await test.step("Verify ICE state before hacking", async () => {
        await av.wait(0.5, "Give ICE page some time to render")
        // await av.wait(3.0, "Wait for ICE puzzle to fully render") //disable before re-making the screenshot
        await tanglePage.checkNameAndScreenshot("2.1-tangle-before")
    })

    await test.step("Solve tangle ICE puzzle", async () => {
        await tanglePage.moveTanglePoint(1495, 401, 1681, 823)
        await tanglePage.moveTanglePoint(1120, 127, 598, 886)
        await tanglePage.moveTanglePoint(1351, 841, 886, 896)
        await tanglePage.moveTanglePoint(745, 644, 767, 151)
    })

    await test.step("Verify ICE after hacking", async () => {
        await tanglePage.checkNameAndScreenshot("2.1-tangle-after")
        await tanglePage.verifyPuzzleSolved()
    })

    await test.step("Verify node shown as hacked in site map", async () => {
        await av.wait(5.0, "Wait for ICE hack to be picked up by site and animation to be complete")
        await av.verifyScreenshotCanvas("2.1-tangle-ice-hacked")
    })
});