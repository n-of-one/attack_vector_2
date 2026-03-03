import {test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {WordSearchPage} from "./testframework/WordSearchPage";

test('test', async ({page}) => {
    test.setTimeout(70_000);

    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Log in as hacker and join existing run dev", async () => {
        await login.loginUsingLink("hacker")
        await hacker.resetRuns("dev")
        await hacker.joinExistingRun("dev")
    })

    await test.step("Start WordSearch ICE puzzle in node 03", async () => {
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 03")
        await hacker.typeCommandDontWaitForPrompt("hack 1")
    })

    const icePagePromise = page.waitForEvent('popup');
    const icePage = await icePagePromise;
    const wordSearch = new WordSearchPage(icePage)

    await test.step("Verify ICE state before hacking", async () => {
        await wordSearch.verifyIceName()
        await wordSearch.waitForPuzzleReady()
        await wordSearch.checkScreenshot("2-5-wordsearch-before")
    })

    await test.step("Solve word search ICE puzzle", async () => {
        await wordSearch.verifyTerminalText("Search fragment: PROTOCOL 1/5")
        await wordSearch.dragWord(438, 561, 717, 561)
        await wordSearch.verifyTerminalText("Search fragment: CAPTCHA 2/5")
        await wordSearch.dragWord(639, 520, 400, 521)
        await wordSearch.verifyTerminalText("Search fragment: REALTIME 3/5")
        await wordSearch.dragWord(355, 604, 636, 604)
        await wordSearch.verifyTerminalText("Search fragment: JPEGGING 4/5")
        await wordSearch.dragWord(435, 437, 713, 442)

        await hacker.verifyScreenshotCanvas("2.5-wordsearch-ice-intermediate")

        await wordSearch.verifyTerminalText("Search fragment: TOOLBAR 5/5")
        await wordSearch.dragWord(354, 199, 596, 204)
    })

    await test.step("Verify ICE after hacking", async () => {
        await wordSearch.verifyPuzzleSolved()
    })

    await test.step("Verify node shown as hacked in site map", async () => {
        await hacker.waitForNodeHackedAnimationFinished()
        await hacker.verifyScreenshotCanvas("2.5-wordsearch-ice-hacked")
    })
});
