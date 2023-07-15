# URL paths

These are the entry points into the application:


## Attack-Vector site

### Site: `/`, `/gm`, `/hacker`

This is the entrypoint into the site for hackers, GMs and admins.

### Editor: `/editor/{site-id}`

Editor for a specific site

## Direct links to ICE or app 
The following paths are decoded. `/!/ice/{layer-id}` will really look like `/!/aWJnLGpqYmIlMTpqNCA6OyQmKH91bHNlNS8oKS4=`

### App: `/!/widget/{app-id}`

Permanent link/QR to the Widget of an App. Used by a phone to display the status of the App


### App: `/!/app/{app-id}` 

Permanent link/QR to App. Used to manipulate things outside of AV by non-hackers. 

This will show either the app or the passcode page of the ICE protecting it. A hacker can switch from the passcode page
to the hacking screen for this ICE.

### App: `/!/app/{app-id}?hacking={hacking}&level={level}`

Link for a hacker accessing an app. `hacking=true` will cause the browser to render the hacking view instead of
the ICE authorizatino page.

The `level={level}` allows for stating that the levels above have already been hacked, allowing a hacker to
progress beyond the first level of ice.

### Ice: `/!/iceLayer/{layer-id}` 

Permanent link/QR to Ice at this layer. Can be used by hackers to hacker the ICE outside of AV. A site can be made 
that allows this, forcing hackers to be physically present somewhere to hack the ICE.  

### Link to Ice from site hacking: `/!/ice/{ice-id}?user={user-id}` 

Temporary link to Ice for hacker when hacker is hacking a site. Meant to be single use by the hacker


## Access to apps that are protected with ICE

A (non-hacker) player scans a QR code for a light switch. The node that holds the light switch has Tangle ICE layer on top of 
the light app.

0. OS
1. Light switch app
2. Tangle ICE layer

### Scenario 1: non hacker players scans QR code

- The player opens: `/!/app/{app-id}`
- Browser sends REST call to `/api/app/{app-id}` to figure out what to do
- Server finds app and node that it is in. Server finds Tangle ICE layer protecting app
- Server checks the IceStatus of the ICE layer, the player is not cleared for access.
- Server responds with:
  - action: `auth`
  - type: `TANGLE_ICE`
  - iceId: `{ice-id}`
  - nextUrl: `/!/app/{app-id}`
- Browser renders ICE authorization page
- Player enters passcode (or password)
- Server receives correct passcode/password and marks player as cleared for access in the IceStatus of the tangle ICE.
- Server responds with: browser, please redirect to : TODO: `/!/app/{app-id}` ?
- Browser redirects to `/!/app/{app-id}`
- Server finds app and node that it is in. Server finds Tangle ICE layer protecting app
- Server checks the IceStatus of the ICE layer, the player is now cleared for access.
- Server responds with:
    - action: `app`
    - type: `(null)`
    - iceId: `(null)`
    - nextUrl: `(null)`
- Browser renders Light switch page


### Scenario 2: hacker players scans QR code

(Start is the same as scenario 1)
- The player opens: `/!/app/{app-id}`
- Browser sends REST call to `/api/app/{app-id}` to figure out what to do
- Server finds app and node that it is in. Server finds Tangle ICE layer protecting app
- Server checks the IceStatus of the ICE layer, the player is not cleared for access.
- Server responds with:
    - action: `auth`
    - type: `TANGLE_ICE`
    - iceId: `{ice-id}`
    - nextUrl: `/!/app/{app-id}`
- Browser renders ICE authorization page

(different from here)

- Player switches to hacking mode
  - browser redirects to: `/!/app/{app-id}?hacking=true`
- Browser sends REST call to `/api/app/{app-id}?hacking=true` to figure out what to do 
- Server responds with:
  - action: `auth`
  - appId: `(null)`
  - iceId: `{ice-id}`
  - type: `TANGLE_ICE`
  - nextUrl: `/!/app/{app-id}?layer=1&hacking=true`
- Browser renders hacking page for ICE
- Hacker hacks ice
- Browser redirects to `/!/app/{app-id}?layer=1&hacking=true`
- Browser sends REST call to `/api/app/{app-id}?layer=1` to figure out what to do
- Server responds with:
    - action: `app`
    - type: `(null)`
    - iceId: `(null)`
    - nextUrl: `(null)`
- Browser renders Light switch page


### Scenario 3: Hacker hacks ICE from

- The player opens: `/!/ice/{ice-id}?user={user-id}`
- Browser renders hacking page for ICE