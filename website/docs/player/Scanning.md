---
sidebar_position: 2
---
# Scanning
When you start a new hacking run, or join an existing one, you start outside of the site.

## Outside
If you have the Scan skill (see [here](skills/Scan)), you can run the `scan` command. This will reveal the nodes of the site, up to and including any nodes containing ICE layers.

Once the scan is complete, you can click on the map. This shows the layers that make up that node. This is useful for planning your hacking run.

If you don't have the Scan skill, a scan will be performed automatically when you `attack` the site. The disadvantage is that if is a tripwire timer in the first node, the timer will be running when you look at the scan results.

## Inside
Nodes with ICE layers block scanning. But after you hack all the ICE in a node, you can perform a new `scan`. This will reveal a new portion of the site.

If you don't have the Scan skill, you will automatically start a scan when entering a node behind the node that contained the ICE. The disadvantage is that you might immediately start a tripwire timer.

## Hacking runs
Scan information is part of a hacking run. If you start a hacking run against a site, it will start without any scan information from other hacking runs.