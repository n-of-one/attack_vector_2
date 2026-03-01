import {test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {wait} from "./testframework/utils/testUtils";
import {NetwalkPage} from "./testframework/NetwalkPage";

test('test', async ({page}) => {
    test.setTimeout(70_000);

    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Log in as hacker and join existing run dev", async () => {
        await login.loginUsingLink("hacker")
        await hacker.resetRuns("dev")
        await hacker.joinExistingRun("dev")
    })

    await test.step("Start Netwalk ICE puzzle in node 02", async () => {
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 02")
        await hacker.typeCommandDontWaitForPrompt("hack 1")
    })

    const icePagePromise = page.waitForEvent('popup');
    const icePage = await icePagePromise;
    const netwalk = new NetwalkPage(icePage)

    await test.step("Verify ICE state before hacking", async () => {
        await wait(page, 3.0, "Wait for ICE puzzle to fully render")
        await netwalk.checkNameAndScreenshot("2.6-netwalk-before")
    })

    await test.step("Solve netwalk ICE puzzle", async () => {
        await netwalk.leftClick(638,397)
        await netwalk.leftClick(638,397)
        await netwalk.leftClick(638,397)
        await netwalk.leftClick(551,398)
        await netwalk.leftClick(551,398)
        await netwalk.leftClick(551,398)
        await netwalk.leftClick(473,250)
        await netwalk.leftClick(473,250)
        await netwalk.leftClick(539,164)
        await netwalk.leftClick(539,164)
        await netwalk.leftClick(539,164)
        await netwalk.leftClick(482,151)
        await netwalk.leftClick(479,163)
        await netwalk.leftClick(400,148)
        await netwalk.leftClick(392,238)
        await netwalk.leftClick(398,306)
        await netwalk.leftClick(398,306)
        await netwalk.leftClick(466,314)
        await netwalk.leftClick(482,390)
        await netwalk.leftClick(482,390)
        await netwalk.leftClick(482,393)
        await netwalk.leftClick(391,401)
        await netwalk.leftClick(384,568)
        await netwalk.leftClick(384,568)
        await netwalk.leftClick(396,630)
        await netwalk.leftClick(461,470)
        await netwalk.leftClick(461,470)
        await netwalk.leftClick(461,470)
        await netwalk.leftClick(473,548)
        await netwalk.leftClick(473,548)
        await netwalk.leftClick(473,548)
        await netwalk.leftClick(484,641)
        await netwalk.leftClick(647,480)
        await netwalk.leftClick(647,480)
        await netwalk.leftClick(558,551)
        await netwalk.leftClick(558,552)
        await netwalk.leftClick(635,561)
        await netwalk.leftClick(635,561)
        await netwalk.leftClick(634,635)
        await netwalk.leftClick(720,638)
        await netwalk.leftClick(872,552)
        await netwalk.leftClick(871,642)
        await netwalk.leftClick(797,644)
        await netwalk.leftClick(796,644)
        await netwalk.leftClick(733,474)
        await netwalk.leftClick(793,488)
        await netwalk.leftClick(650,313)
        await netwalk.leftClick(630,247)
        await netwalk.leftClick(630,247)
        await netwalk.leftClick(728,240)
        await netwalk.leftClick(777,233)
        await netwalk.leftClick(698,172)
        await netwalk.leftClick(700,156)
        await netwalk.leftClick(708,151)
        await netwalk.leftClick(652,156)
        await netwalk.leftClick(652,156)
        await netwalk.leftClick(791,227)
        await netwalk.leftClick(871,237)
        await netwalk.leftClick(871,237)
        await netwalk.leftClick(712,403)
        await netwalk.leftClick(712,403)
        await netwalk.leftClick(783,388)
        await netwalk.leftClick(801,391)
        await netwalk.leftClick(872,395)
        await netwalk.leftClick(800,331)
        await netwalk.leftClick(878,322)
        await netwalk.leftClick(878,322)
        await netwalk.leftClick(878,322)
        await wait(page, 1, "Wait for last click to fully render")
        await netwalk.checkNameAndScreenshot("2.6-netwalk-just-before-hack-complete")
    })

    await test.step("Verify ICE after hacking", async () => {
        await netwalk.leftClick(871,397)
        await netwalk.verifyPuzzleSolved()
    })

    await test.step("Verify node shown as hacked in site map", async () => {
        await hacker.waitForNodeHackedAnimationFinished()
        await hacker.verifyScreenshotCanvas("2.6-netwalk-ice-hacked")
    })
});
