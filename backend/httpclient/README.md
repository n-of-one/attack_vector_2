# Http client

These are intellij http client sample calls for the REST API of Attack Vector.

They require Intellij to be run. See: https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html

Alternatively, you can use run them from the CLI. See: https://www.jetbrains.com/help/idea/http-client-cli.html


## Setup
1. Copy the `http-client.private.env.template.json` to `http-client.private.env.json`
2. Login as GM on AttackVector, open developer tools and copy the value of the `jwt` cookie.
3. Paste the jwt value in the token section of the `http-client.private.env.json` file.

## Known bugs

- If you have the editor open while performing API calls, have selected a node in the editor and add a layer to a different node, the browser hangs and needs to be closed.
