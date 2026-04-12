# Authentication & Login

Attack Vector supports multiple login methods depending on the deployment environment. After authentication, users are routed to role-specific pages.

---

# Dev Login

Development-mode login for testing and local environments. No passwords required.

## Capabilities
- Text input for username
- Quick-select buttons for gm, admin and test accounts


---

# Google OAuth Login

login via Google authentication.

## Capabilities
- Uses Google OAuth flow
- Creates hacker user on first login

---

# Admin Login

Username and password login for admin and GM accounts.

## Capabilities
- Reachable from other login pages by triple clicking the 2 of Attack Vector 2
- Username and password form fields
- Password checked against global configuration
- Brute-force protection: tracks failed login attempts by IP address

---

# Role-Based Routing

After successful login, users are sent to their role-specific page.

## Capabilities
- `admin`  → admin pages: users, config, tasks
- `gm` → gm pages: gm-scripts, sites, users, statistics
- `hacker` → hacker pages: (sites), (current run), home, (hacker-scripts), (market), (credits)
- all roles: my account page
- No valid role → redirected back to login

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
- Login stores JWT in cookie
- Hackers can only have one main browser tab open and one app browser tab
- Widgets do not require login

