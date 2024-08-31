# NametagEdit

[![Support](https://img.shields.io/badge/Minecraft-1.20.4-green.svg)](documentation/Support)
[![JDK](https://img.shields.io/badge/JDK-21-blue.svg)](https://jdk.java.net/java-se-ri/8-MR3)
[![NametagEditAPI](https://img.shields.io/badge/NTE-Developer%20API-ff69b4.svg)](documentation/Developers.creole)

This is a fork of NametagEdit that aims to relieve the maintenance burden between updates, but also adds the following features
- Folia support
- Minimessage support
- No character limit on prefix/suffix
- Improved luckperms support

This plugin allows users to add any string before and after their name. Individual tags can be created for players, or a group can be created that can be joined via permissions.

NametagEdit has support for LuckPerms. If a user changes groups or permissions, their tag is automatically updated.

* [Official Project Page](https://www.spigotmc.org/resources/nametagedit.3836/)
* [Development Builds](https://ci.nametagedit.com/job/NametagEdit)

# Quick Links
* [API & Developers](documentation/Developers.creole)
* [Permissions](documentation/Permissions.creole)
* [Commands](documentation/Commands.creole)
* [Configuration](documentation/Configuration.creole)
* [Common Issues](documentation/Support.creole)

# Features
✔ Converters to and from MySQL and FlatFile

✔ Efficient Flatfile support and MySQL connection pooling

✔ [LuckPerms](https://www.spigotmc.org/resources/luckperms-an-advanced-permissions-plugin.28140/) support

✔ Sortable Group/Player Tags in tab

✔ [Clip Placeholder API](https://www.spigotmc.org/resources/placeholderapi.6245/) Support 

# Frequently Asked Questions
#### Q: Will this allow me to change my skin and name?
**A:** No. This plugin creates fake scoreboard teams with packets.

#### Q: My client crashes with the reason "Cannot remove from ID#". Why is this?
**A:** Due to how scoreboards were implemented in Minecraft, a player cannot belong to two teams. Any two plugins that use packets or the Bukkit API which alter team prefixes/suffixes will have conflicts. There is currently no way around this.

#### Q: Can I sort nametags in the tab list?
**A:** Yes. Read up on how to use it [here](documentation/Configuration.creole)

# Incompatible Plugins
✖ Any plugin that creates NPCs that share the same username as players who have 'NametagEdit' nametags

✖ Any plugin that uses Team color sidebars without specifically supporting NametagEdit
