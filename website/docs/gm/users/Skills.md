---
sidebar_position: 4
---
# Skills
Skills are abilities that hackers can have that help them in the game. There are core skills and specialized skills. The specialized skills are optional, not needed for Attack Vector, but could be used to differentiate between professional hackers and hackers that are just starting out (or characters just helping out).

Each Larp defines what skills they give to players and how players can acquire new skills. that is managed outside of Attack Vector.

## Common skills
These skills are the core skills that all competent hackers should have.

### Scan
This skill allows the hacker to use the `scan` command. Without having access to the scan command, hackers will be limited in how effective they are at hacking. They can still hack sites, but need to `move` into nodes to find out what it contains. This is dangerous as it can trigger tripwires.

Default hacker skill: **yes**

The purpose of this skill is to allow the creation of non-professional hacker characters. These characters lack skills, but can still play the game. By having this skill, the professional hackers are actually better.

### Search site

Default hacker skill: **yes**

This skill allows the hacker to initially access a site. Without this skill, there is no search box on the home page of a hacker, and the player cannot start a new hack. Instead, they must be invited to a hack by a hacker that does have this skill.

The purpose of this skill is to allow the creation of non-professional hacker characters. These characters lack skills, but can still play the game. By having this skill, the professional hackers are actually needed to be able to do hacks.

## Specialized skills
These skills give the hackers an edge over other hackers who don't have this skill.

### Bypass

Default hacker skill: **no**

This skill allows the hacker to ignore ICE in the first node of a site with respect to moving beyond this ICE. This also allows the hacker to perform a scan inside the node to reveal the site beyond this node. Normally the ICE blocks revealing the rest of the site.

### Create site

Default hacker skill: **no**

This skill allows the hacker to create their own sites. Other hackers can then hack these sites as normal. These can be in-game sites or in-game tutorials to help teach new hackers.

When a hacker has this skill, they will see a menu item **sites** that allows them to manage their sites.
### Jump

Default hacker skill: **no**

This skill allows the hacker to use the `jump` command to jump to another hacker's node. This is not blocked by ICE along the way.

### Rollback

Default hacker skill: **no**

This skill allows the hacker to use the `rollback` command to move back from a node they just entered to the node they came from. This will cancel any timers started by entering the node.

### Scripts Credits

Default hacker skill: **no**

This skill allows the hacker to sell data for script credits, and use those to buy scripts. Optionally you can have an script credits income defined with this skill.

More details can be found [here](../scripts/Managing%20script%20credits).

### Scripts RAM

Default hacker skill: **no**

This skill allows the hacker to load and execute scripts. The number value of this skill is the number of RAM blocks this player will have.

More details can be found [here](../scripts/Role%20of%20scripts).

### Stealth

Default hacker skill: **no**

This skill increases the time for a site to shutdown when the hacker triggers a tripwire. A stealth value of +30% means that the total time before shutdown is increased by 30%. For example from 10 minutes to 13 minutes.

This skill can also be applied negatively, to decrease the time. A stealth value of -30% will decrease the time from 10 minutes to 7 minutes.

### Weaken

Default hacker skill: **no**

This skill allows the hacker use the `weaken` command. This reduces the strength of a single layer of ICE. The strength is reduced by one step, for example Very Strong becomes Strong.

This skill works on one or more types of ICE, specified when setting up the skill.

You can have this skill more than once. Where each instance defines what types of ICE it affects. Each instance can be used once per site. After a site is reset, the skill (instances) can be used again on this site.