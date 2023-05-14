# Context

Authentication is a bit complex for our websocket/stomp setup.

### Problem 1. Getting authentication details from browser to the server
We are using jwt to encode the user authentication information, and normally you
would send this token in the header of the http connection. However, for websockets the browser does not support this. 

So instead we use a cookie to send the jwt token. This is not ideal, but it works.
The cookie is set when the user logs in, and is automatically sent when the websocket connection is made.

### Problem 2. Allowing multiple web-sockt connections for a single user
A hacker will have a normal websocket connection, and possibly also a connection when hacking ice. 
These need to be different connections.

We send the following user-name back to the websocketConnection: ${userId}:${connectionId} for example:
"user-9737-4237:connection-f703-4875"

### Problem 3. Keeping track of user connections
We need to know if a user is connected, both for their normal connection and their ice connection.

This is used for multiplayer: showing when a user has left a run.

It is also used to prevent a user from having multiple connections at the same time.





Browser

V

Websocket http request
- contains cook with the jwt

V

JwtAuthenticationFilter
 - take jwt from cookie
 - create unique connectionId
 - set connection type based on URL (av_ws: WS_GENERAL, ice_ws: WS_ICE, other: WEB_PAGE)
 - create the UserPrincipal and set it in SecurityContext
   - (!) Note that this is not the UserPrincipal used in websocket endpoints. It is merely used by the WebSocketAuthenticationHandler.determineUser

V

WebSecurityConfig (allow connection on /av_ws or /ice_ws)
- Only allow connection if UserPrincipal has role USER

V

MyHandshakeInterceptor
- Currently not used.
- Could be used to map servlet request info to attributes of the SessionConnectEvent

V

WebSocketAuthenticationHandler.determineUser()
- determines the user for any subsequent calls via this websocket.
    - takes input the UserPrincipal set in the JwtAuthenticationFilter
    - change the name to consist of both the userId and the connectionId.
    - The name is reported back to the browser, and this way the browser knows the unique connection-id.

StompConfig.handleConnectEvent -
- 
 
V
