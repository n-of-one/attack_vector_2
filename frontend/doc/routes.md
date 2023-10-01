# Routes

This frontend-app will be hosted by the Spring Boot (Tomcat) app.

| Route                               | Description                                                                    |
|-------------------------------------|--------------------------------------------------------------------------------|
| `/login`                            | Login                                                                          |
| `/loggedOut`                        | Logout                                                                         |
| `/hacker`                           | Hacker                                                                         |
| `/x/{ice/tangle-1234-1234}`         | Tangle Ice minigame                                                            |
| `/x/{auth/tangle-1234-1234}`        | Tangle Ice auth.                                                               | 
| `/x/{app/statusLight-d204-4c27}`    | Switch app for status light <br> OR <br> Auth app for ICE protecting this app. | 
| `/x/{widget/statusLight-d204-4c27}` | Widget for status light (showing current status)                               |


# Routes v2

This frontend-app will be hosted by the Spring Boot (Tomcat) app.

| Route                                               | Description                                      |
|-----------------------------------------------------|--------------------------------------------------|
| `/login`                                            | Login                                            |
| `/loggedOut`                                        | Logout                                           |
| `/hacker`                                           | Hacker                                           |
| `/x/{widget/statusLight/node-0000-0000:layer-0001}` | Widget for status light (showing current status) |
| `/x/{app/switch/node-0000-0000:layer-0001}`         | Switch app for status light                      | 
| `/x/{app/auth/node-0000-0000:layer-0002}`           | Authorization interface for Ice                  | 
| `/x/{ice/externalAccess/node-0000-0000:layer-0002}` | Tangle Ice minigame for hacker external access   |
| `/x/{ice/siteHack/tangle-2123-4552}`                | Tangle Ice minigame for hacker during site hack  |


### Scenario 1: SL scans widget QR code / enters widget URL

- Browser opens: `/x/widget/statusLight/{layer-id}`
- `WidgetSelector.tsx` sends REST call to `/api/widget/{layer-id}` to figure out what to do
- Server finds layer and responds with the appId.
- `WidgetSelector.tsx` opens `StatusLightRoot.tsx` which renders the widget


### Scenario 2: players scans QR code of app that is not protected 

- The player opens: `/x/app/switch/{layer-id}`
- `AppSelector.tsx` sends REST call to `/api/app/{layer-id}` to figure out what to do
- Server finds layer and node that it is in. Server finds there is no ICE protecting it.
- Server responds with:
    - appId: `statusLight-0000-0000`
- Browser renders Light switch page with this appId using `StatusLightRoot.tsx`.


### Scenario 3: players scans QR code of app that is protected

- Same as above, but now the server responds with:
  - appId: null
  - redirectLayerId: node-0000-0000:layer-0002
- Browser redirects to: `/x/{auth/node-0000-0000:layer-0002}
- Player enters the correct password 
  - either because it's a password Ice
  - or because they got the password from a keystore
- Server receives password and finds next layer for player to go to
  - The next layer down is the switch app
- Browser redirects to: `/x/switch/{layer-id}` 

### Scenario 4: player scans QR code of app that is protected by two ICE layers

- Same as above, but in the last step the server redirects to the next ICE layer

### Scenario 5: player scans QR code of app that is protected, and player switches to hacking minigame

- ... when the player is in the auth app, they switch to hacking mode
- Browser redirects to: `/x/{ice/externalAccess/node-0000-0000:layer-0002}`
- After hacking is complete, the browser sends a websocket message to `/ice/next` to find the next layer to access
- The server checks what the next layer is, and returns the path to it:
  - 'app/switch/node-0000-0000:layer-0001'
  - The browser encodes this and redirect to this URL, opening the switch app.

