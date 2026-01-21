---
slug: /
---

## Goal

Version 2 of Attack Vector is to be as low effort for larp organizers to use as possible.

It's been developed for the Dutch Larp [Eos Frontier](https://eosfrontier.space/), but it can be used in any sci-fi event that favors player involvement over realism.

![Picture of several hackers cooperating hacking a site using Attack Vector](../assets/organizers/av_on_frontier.png)
_Players cooperating to solve a Netwalk ICE puzzle on Frontier 17. Photo courtesy of [Hephaestus Aperture](https://www.hephaestus-aperture.com/)._

Attack Vector is [open source](https://github.com/n-of-one/attack_vector_2) under the MIT license, meaning you can use it in most ways you want, including commercially. I (Erik, the creator) encourage it to be used on Live Action Roleplaying events.

## Hosting Attack Vector

Attack Vector is free, but it's also a piece of software that needs to be hosted somewhere.

The simplest option is to just install it on a laptop or desktop. There are installation instructions for Windows and Linux in the [Readme](https://github.com/n-of-one/attack_vector_2/blob/main/README.md).

Another option is to host it on the cloud. It runs on an Amazon AWS t4g.micro, costing around 1 dollar-cent/euro-cent per hour of usage. There are additional costs for having an IP address and having a domain name.

Running Attack Vector requires Java 21+ and Mongodb.

## Discord

There is a Discord dedicated to using Attack Vector on your event.

Invite link: https://discord.gg / EkNFsHMbfC

*Please remove the spaces from this URL yourself. This is a precaution against bots using this link.*


If you consider using Attack Vector, have questions or problems using it, please go here and ask for help. I usually reply within a day.

## Customization for a specific event

Attack Vector 2 can be changed to fit the aesthetic of a specific event, but this will require some development. I am happy to perform some simple changes as long as the organization provides the graphics and cooperates in this process.

A bigger effort is to connect the login of Attack Vector to the login of the event's website. I will help with this depending on the effort involved.

## Privacy

Attack Vector aims to be as privacy friendly as possible. It does not use cookies to track users, it does not store any personal information other than the information stored in there by the game masters.

Google Authentication is the default way of logging in so that the system does not know any real usernames or passwords. The system does not store any personal information returned by Google like the name or email address of the person logging in. See the [privacy statement](http://localhost:3000/privacy) for details.


## Development and future features

Version 1 of Attack Vector was first used on an event in 2016. This version did not support multiplayer like version 2. However, over the years a lot of features were added that are currently missing from version 2, see below.

Version 2 of Attack Vector was first used on an event in 2023.

Features that will be implemented in the future:


- Creation of site-templates

One of the major challenges during an event, is that sometimes players come up with some system they want to hack, that the game-masters did not anticipate. To solve this, version 1 allowed the game-masters to create a number of site-templates in advance, but without any specific information in them.

During the event, they could use a very simple web-form to create a site based on a template, and fill in the information that the players would find inside.

This way, even game masters who don't understand the specifics of how to create good sites, could still provide a site for the players to hack.



- Discord integration

Version 1 had a Discord bot that would send updates to a Discord channel. This way, game masters could see the outcome of a hack on their phone.