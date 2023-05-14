# Context

Authentication is a bit complex for our websocket/stomp setup.

### Problem 1. Getting authentication details from browser to the server
We are using jwt to encode the user authentication information, and normally you
would send this token in the header of the http connection. However, for websockets the browser does not support this. 

So instead we use a cookie to send the jwt token. This is not ideal, but it works.
The cookie is set when the user logs in, and is automatically sent when the websocket connection is made.

### Problem 2. Prevent multiple connections
A single hacker can only have one websocket connection for their main screen, and one for hacking ice.
If they open a second tab for hacking, we want the first tab to be disconnected.

We solve this by giving each websocket connection a unique connectionId, in addition to their userId. 
After connection, we send a message to all connections (for this type) that a new connection was openend, with
the new connectionId. All websockets (for this type) can check this ID against their own connectionId, and
take action if they are no longer the active connection.


### Problem 3. Getting the connectionId to the browser
Ideally we want to send the connectionId to the browser as an answer to their handshake request.
The challenge is that the only information the creation-handler can send back is the user-name.
So we send the following user-name back to the websocketConnection: ${userId}:${connectionId} for example:
"user-9737-4237:connection-f703-4875"


### Problem 4. Keeping track of user connections
Besides preventing duplicate connections for a single user, we also want to know which users are connected,
and what they are doing. This is used for the multiplayer aspect of the game: other players must be able to
see the connecting/disconnecting of other players.

During the websocket handshake we call the StompConnectionEventService for this.



# Flow of calls when a websocket connects


**Browser**
- The connection is set up from WebSocketConnection.ts

V

**Websocket http request**
- contains cook with the jwt

V

**JwtAuthenticationFilter**
 - take jwt from cookie
 - create unique connectionId
 - set connection type based on URL (av_ws: WS_GENERAL, ice_ws: WS_ICE, other: WEB_PAGE)
 - create the UserPrincipal and set it in SecurityContext
   - (!) Note that this is not the UserPrincipal used in websocket endpoints. It is merely used by the WebSocketAuthenticationHandler.determineUser

V

**WebSecurityConfig (allow connection on /av_ws or /ice_ws)**
- Only allow connection if UserPrincipal has role USER

V

**(MyHandshakeInterceptor)**
- Currently not used.

V

**WebSocketAuthenticationHandler.determineUser())**
- determines the user for any subsequent calls via this websocket.
    - takes input the UserPrincipal set in the JwtAuthenticationFilter
    - change the name to consist of both the userId and the connectionId.
    - The name is reported back to the browser, and this way the browser knows the unique connection-id.

**StompConfig.handleConnectEvent**
-  Call the stompConnectionEventService to register the connection
 
V

**stompConnectionEventService.connect**
- Schedule a call to the userConnectionService.connect via the taskRunner
  - We want all events that impact hackers to run via the taskRunner to prevent race conditions

V

**UserConnectionService.connect**
- Update the activity of the user
- Send a message to all connections of this user that this is the new connection for the current type (main/ice)
  - Type: SERVER_USER_CONNECTION

V

**WebsocketConnection.ts**
- The message of type SERVER_USER_CONNECTION is received and if the connectionId is not the same as the current connectionId, 
the connection is closed and the page rerouted to the Disconnected page.

# Additional notes

1. Connection type

We determine the type of connection via the URL path, and set the type in the JwtAuthenticationFilter.
We could also create different handshakeHandlers, but the advantage of setting the type in the filter
is that we have a single place for determining the type of request.

2. Websocket headers

You cannot send additional information when creating a websocket connection. You can set headers in the
webstomp.client.connect() call, but these cannot be read from the Spring Boot side.

