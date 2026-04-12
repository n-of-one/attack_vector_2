---
sidebar_position: 3
---
# Google authentication
The main mechanism for users to log into Attack Vector is Google authentication. Attack Vector will task Google with identifying you, so users will need to have a Google account.

After the user has confirmed their login with Google, it will give Attack Vector their email address. Attack Vector stores a one-way hash of the email address to link the email to the account. After this point, Attack Vector uses a temporary cookie to store a session token.

## Setting up Google authentication
Go to the Google Auth Platform, Clients page ([link](https://console.cloud.google.com/auth/clients)).

You will first be asked to create a project, you can give it any name and the email address that users can contact support.

Then you will be asked for some additional information for this project:

- App information:
  - Name of the app: you can use "Attack Vector" here
  - User support email: the email shown to users for support. This will show up in the Google login.
- Audience: use External
- Contact information: you can add your email here.

Now you can create a new client with the following details:
- Application type: Web application
- Name: (whatever you choose)
- Authorized JavaScript origins: to start with, enter: http://localhost . The actual value that you will be using later is the top level URL of your domain. If you are hosting your site on `https://attackvector.nl`, then this is what you enter here.

When this is created, you will see a **Client ID** that looks something like this:

`123456789012-abcdefghijklmnopqrstuvwxyz12345678.apps.googleusercontent.com`

Enter this ID as the value for the configuration item: 'Google client id', see [here](/installation/Configuration#google-client-id).

## Testing
See if you can log in with a browser that is accessing localhost. Once that works, you can switch to using your external URL.

Note that Google will be verifying where your site is running (or more accurately, it will only allow calls from JavaScript that is coming from the Authorized JavaScript origins.

Also note that the URL must be an https URL if it's not localhost.