# Multi-User

Attack Vector supports multiple hackers collaborating in the same run, with real-time presence tracking and WebSocket synchronization.

---

# Hacker Presence

Tracking and displaying where each hacker is in the site.

## Capabilities
- Hackers appear as icons on the network map at their current node
- Each hacker has a unique icon and display name
- Presence states: OFFLINE, ONLINE (logged in but not in run), OUTSIDE (in run, before entering site), INSIDE (navigating the site)
- Presence updates in real-time as hackers move between nodes
- Joining and leaving a run is broadcast to all other hackers in that run

---

# Collaborative Hacking

Multiple hackers working together in the same run.

## Capabilities
- Multiple hackers can be inside the same site simultaneously
- Hackers share the same run state (same nodes discovered, same ICE status)
- Sharing a run via `/share <username>` gives another hacker access to join
- Some ICE types support collaborative solving (e.g., Sweeper: multiple hackers can work the same grid)
- Hackers see each other's presence on the ICE puzzle pages
- One hacker hitting a mine in Sweeper only blocks that hacker — others can continue

---

# Force Disconnect

Handling duplicate logins from the same user.

## Capabilities
- Only one active session per user
- Logging in on a new tab or browser disconnects the previous session
- Previous session receives a disconnect notification and closes its WebSocket
- If the disconnected hacker was in a run, they are removed and other hackers are notified

---

# WebSocket Channels

Real-time communication infrastructure.

## Capabilities
- Per-run channel: all hackers in a specific run see each other's movements, scans, and hacks
- Per-site channel: editors see each other's changes
- Per-user channel: individual messages (force disconnect, personal notifications)
- Per-ICE channel: hackers in the same ICE puzzle see shared state updates
