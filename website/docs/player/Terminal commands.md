---
sidebar_position: 3
---
# Terminal commands
The main way of interacting with a site is by typing commands in the terminal.

## Outside of the site
When you start a hack, you start outside of the site. You can only see the first node. 

You can choose to either scan the site, or attack it.

### Help
`help` - get a list of available commands.

### Scan
`scan` - scan the site to reveal the unprotected part of its network.

This is usually the first thing you do when you arrive at a new site to show the parts of the site that are not protected by ICE.

### Attack
`attack` - enter the site and start the attack

## Inside the site
After you have entered the `attack` command, you have entered the site. You can now move around and interact with layers.

### Help
`help` - get a list of available commands.

### View
`view` - show layers in this service. Alternative to clicking on the node on the map.

### Move
`move` - move to another node. For example: `move 01`

If there is a tripwire in the node you are moving to, it will be triggered.

### Hack
`hack` - hack a layer. For example: `hack 2`

The effect will depend on the layer type. See: [Layers overview](/player/layers/Layers%20overview)

### Open password UI
`password` - open the password UI for an ICE layer. For example: `password 2`

If you have found the password for this ICE, you can enter it here. Allows you to bypass the mini-game.

### Scan
`scan` - do a new scan after ICE has been hacked, to reveal new parts of the site.

### Disconnect
`dc` - Disconnect from the site, bringing you back outside.

## Social commands

These can be given either outside or inside a site.

### Share
`/share` - share your run with other players, allowing them to join you and cooperate.

For example: `/share paradox stalker`.

You can add multiple hacker names separated by spaces. 

You could also just tell the site name to the other players. They could use this to enter the site, but they would be in their own 'run'. This means that you cannot see each other, or collaborate. All runs are hacking the same site, so any ICE that is hacked in one run will be hacked in all runs.

## Skill based commands
Some skills give you access to new commands. You can only use them if you have the appropriate skill.

### Run a script
`run` Run a script that you have loaded in memory. For example: `run 1234-abcd`

### Download a script
`/download-script` Download a script offered by someone else. This is the same of downloading a script from the scripts page, but using this command you don't need to leave the site first.

For example: `/download-script 1234-abcd`

Note that organizers can configure if it's allowed to download a script while hacking a site.

### Weaken ICE layer
`weaken` Reduce the strength of the ICE layer. For example: `weaken 1`

To show how many uses of weaken you have left, use `weaken` without a layer number.

### Rollback
`rollback` Move back to the previous node and cancel all timers you started by entering the current node. Does not work if you did not start any timers in the current node.

### Jump
`jump` Jump to the node of another hacker. Ice does not block this movement.

For example: `jump paradox`
