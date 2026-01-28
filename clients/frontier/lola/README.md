# LOLA client

LOLA is a Frontier specific application that connects to Attack Vector.

## Pre-requisites
LOLA must exist as a user in AV. This user is automatically 
created for the Frontier larp, if the environment variable LARP=FRONTIER is set.


## Interface

The interface for LOLA supports:

- Calling LOLA to a specific site using the "/share lola" command.
- Having LOLA speak a sentence
- LOLA polling the Attack Vector for the timers of the site it's currently in.


## Installation
The client uses webscocket-client:
`pip install websocket-client`

The demo script uses rel to keep the process alive:
`pip install rel` . This is not needed for the actual client.

## Configuring
The client uses a hardcoded auth token.
Retrieve the token by logging into AV and going to the URL:
`/api/admin/frontier/lola/token` . This token is valid for 1 year
