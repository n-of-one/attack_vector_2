# Authentication & Login

Attack Vector supports multiple login methods depending on the deployment environment. After authentication, users are routed to role-specific pages.

---

# Dev Login

Development-mode login for testing and local environments.

## Capabilities
- Text input for username (no password)
- Quick-select buttons for common test accounts: "hacker", "Stalker", "Paradox", "Angler" (hackers), "gm" (game master), "admin" (administrator)
- Creates user on first login if not found
- Only available when the login path is configured to `/devLogin`

---

# Google OAuth Login

Production login via Google authentication.

## Capabilities
- "Login with Google" button launches Google OAuth flow
- Backend validates Google JWT and checks audience claim
- Creates hacker user on first login
- Issues JWT cookie on success
- Error shown if Google client ID is not configured or authentication fails

---

# Admin Login

Username and password login for admin and GM accounts.

## Capabilities
- Username and password form fields
- Password checked against global configuration
- Brute-force protection: tracks failed login attempts by IP address
- System user accounts cannot log in

---

# Role-Based Routing

After successful login, users are sent to their role-specific page.

## Capabilities
- ADMIN → admin dashboard
- GM → GM management page
- HACKER → hacker home page
- No valid role → redirected back to login
- User type stored in browser cookie alongside JWT

---

# Logout

Ends the user session.

## Capabilities
- Clears all authentication cookies (jwt, type, roles, userName)
- Displays logged-out page with a "Login" button to return

---

# Session Management

Handles concurrent logins and session lifecycle.

## Capabilities
- Each login generates a new JWT cookie with expiration
- Logging in on a new tab/browser force-disconnects the previous session
- Previous session receives a disconnect message and closes its WebSocket connection
