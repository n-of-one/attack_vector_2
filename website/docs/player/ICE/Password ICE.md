# Password ICE
This ICE is called Rahasy in game. Rahasya is the Hindi word for `mystery`.

![Picture of a password ICE puzzle](../../assets/players/ice/password.png)

## Hacking
Password ICE cannot be hacked via a mini-game. Instead, the hacker needs to know or guess the password. The password can either be found inside the site or in the game-world.

Every time a wrong password is entered, the ICE prevents entering a new password for a certain amount of time. This time increases and caps out at 10 seconds.

This ICE can have a password hint. This can point hackers in the right direction. The password hint is shown after the third failed attempt.

In the screenshot, the password hint here is "Name of my daughter". Maybe this name can be found in the HR database. Or it can be found in the non-digital game world.

## Keystore passwords and the password UI
The password in this ICE are permanent passwords that don't change under normal circumstances.

All ICE can also have a temporary password that is only valid until the site is reset. These temporary passwords can be retrieved from keystore layers.

To enter any kind of password, you need to open to open the password UI for the ICE with the `password` terminal command. Here you can enter both temporary or permanent passwords, if the ICE has them.

Note that hacking a Password ICE layer has the benefit of opening a hacker-version of the password UI. This will keep track of failed attempts for you.


## ICE strength

This ICE does not have a strength setting.

## Multiplayer

This ICE is not inherently multi-player. When a wrong password is entered, no one can enter a new password, it does not help to have multiple players hack this ICE together.

The cooperation lies in finding the password. If the password is hidden in a big site, one hacker can stay hacking the ICE attempting new password candidates as they are found by other hackers.

Another possibility it that this ICE requires non-hacker players to retrieve the password somewhere else in-game, to allow the hackers to proceed.