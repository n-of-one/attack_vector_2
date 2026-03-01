# Admin

The admin interface provides system-wide configuration and monitoring. Restricted to users with the `admin`  role.

---

# Configuration Management

Setting and viewing application configuration items.

## Capabilities
- View all configuration items with current values
- Edit configuration values
- Configuration categories:
  - **Generic**: larp name
  - **Login**: Google OAuth client ID, login path, master password
  - **Hacker**: delete run links, edit character name, edit username, show skills, tutorial site name, script RAM refresh duration, script lockout duration, load scripts during run, default hacker speed
  - **LARP-specific**: Frontier/Lola features, Orthank token
  - **Developer**: testing mode, ICE quick playing, simulated non-localhost delay, minimum shutdown duration, dev commands enabled, hacker reset site enabled

---

# User Management

Same capabilities as GM user management (create, edit, delete users).

---

# Task Monitor

- View active backend tasks with due time, action, user, and identifiers.
