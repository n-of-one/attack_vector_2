# Connections

AV relies on websocket connections. There are different types of connections, as described below.


## Overview
There are different reasons to connect the the backend:

- Admin who is managing setup
- GM who is creating a site in advance
- GM who is creating missions

- A Hacker player who is logged in with a laptop using their main browser tab to hack sites
- A Hacker player who is logged in with a laptop who has a secondary browser tab open to hack ICE

- A GM who uses a phone to represent widget 
- A Hacker who uses their phone to hack ICE outside of a site


## Connection types
We have the following connection types:

1. STATELESS
1. WS_UNRESTRICTED
1. WS_HACKER_MAIN
1. WS_NETWORK_APP
1. Local-app

### 1. STATELESS  - Stateless connections
These are connections that are not web-socket connections. These are for:
- HTML, CSS, JS, etc.
- REST calls

There is no limit on these connections.



### 2. WS_UNRESTRICTED - Unrestricted
There is no restriction to the amount of connections of this type.
- All GM activity
- All Admin activity
- All widgets

### 3. WS_HACKER_MAIN - Hacker main
This is the connection to the hacker part of AV. Any hacker can only have one connection of this type. 
If they open a second tab, the first tab is disconnected.

Rationale: the software is not designed to support hackers hacking multiple sites at the same time.

### WS_NETWORK_APP- Networked-app
This is the connection of a user (hacker/non-hacker) to an App or to Ice, connecting over the network.  Used for the second tab to hack Ice, or the second tab to access an App.

A user can only have a single networked-app connection at a time. If they open a second tab, the first tab is disconnected.

Rationale: a hacker has a location in a site, and only hackers in the node where the Ice/app resides, can hack it.

### Local-app
This is a connection of a hacker to an App or Ice that is not part of a site. It is used when phones are used to 
interact with local apps or Ice.

A user must be in the vicinity of the local app/ice to connect to it. 

TODO: can we use https://developer.mozilla.org/en-US/docs/Web/API/BluetoothDevice to enforce this?
