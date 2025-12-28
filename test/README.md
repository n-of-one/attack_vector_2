# Test setup

Testing is done with PlayWright.

AttackVector relies heavily on interaction between the browser and the server. 
Playwright allows testing that the interaction work correctly, rather than
that individual components work correctly.


## Manual setup

You need to manually start AttackVector (frontend and backend).


## Running tests

`npx playwright test` 
or
`npx playwright test --ui`


## Developing new tests

Use: `npx playwright codegen localhost:3000 --viewport-size="1920,1080"`

Setting the viewport is important, otherwise AttackVector will auto-scale the view.
This will then mess up any clicks done with the mouse, as the coordinates will be different.

When adding a browser, set the correct viewport in the playwright.config.ts file.



