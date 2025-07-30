# NametagEditX

![MC Version](https://img.shields.io/badge/Minecraft-1.21.4-green.svg)
![JDK](https://img.shields.io/badge/JDK-21-blue.svg)
[![NametagEditAPI](https://img.shields.io/badge/NTE-Developer%20API-ff69b4.svg)](https://github.com/Warriorrrr/NametagEditX/blob/main/documentation/Developers.creole)

This is a fork of [NametagEdit](https://github.com/sgtcaze/NametagEdit), originally developed by sgtcaze & Corey. It contains a bunch of extra features over the original:
- Folia support
- Minimessage support
- Virtually no character limit on prefix/suffix
- Improved luckperms support

This plugin allows users to add any string before and after their name. Individual tags can be created for players, or a group can be created that can be joined via permissions.

NametagEdit has support for LuckPerms. If a user changes groups or permissions, their tag is automatically updated.

* [Official Project Page](https://modrinth.com/project/nametageditx)
* [Development Builds](https://ci.warriorrr.dev/job/NametagEditX/)

# Quick Links
* [API & Developers](https://github.com/Warriorrrr/NametagEditX/blob/main/documentation/Developers.creole)
* [Permissions](https://github.com/Warriorrrr/NametagEditX/blob/main/documentation/Permissions.creole)
* [Commands](https://github.com/Warriorrrr/NametagEditX/blob/main/documentation/Commands.creole)
* [Configuration](https://github.com/Warriorrrr/NametagEditX/blob/main/documentation/Configuration.creole)
* [Common Issues](https://github.com/Warriorrrr/NametagEditX/blob/main/documentation/Support.creole)

# Features
✔ Converters to and from MySQL and FlatFile

✔ Efficient Flatfile support and MySQL connection pooling

✔ [LuckPerms](https://luckperms.net/) support

✔ Sortable Group/Player Tags in tab

✔ [Clip Placeholder API](https://www.spigotmc.org/resources/placeholderapi.6245/) Support 

# Frequently Asked Questions
#### Q: Will this allow me to change my skin and name?
**A:** No. This plugin creates fake scoreboard teams with packets.

#### Q: My client crashes with the reason "Cannot remove from ID#". Why is this?
**A:** Due to how scoreboards were implemented in Minecraft, a player cannot belong to two teams. Any two plugins that use packets or the Bukkit API which alter team prefixes/suffixes will have conflicts. There is currently no way around this.

#### Q: Can I sort nametags in the tab list?
**A:** Yes. Read up on how to use it [here](https://github.com/Warriorrrr/NametagEditX/blob/main/documentation/Configuration.creole)

# Incompatible Plugins
✖ Any plugin that creates NPCs that share the same username as players who have 'NametagEdit' nametags

✖ Any plugin that uses Team color sidebars without specifically supporting NametagEdit
