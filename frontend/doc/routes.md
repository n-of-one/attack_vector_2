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
