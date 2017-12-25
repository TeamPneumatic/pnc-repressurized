# PneumaticCraft: Repressurized - Changelog

This is an overview of significant new features and fixes by release.  See https://github.com/TeamPneumatic/pnc-repressurized/commits/master for a detailed list of all changes.

Changes are in reverse chronological order; newest changes at the top.

## Minecraft 1.12.2

## 0.2.1-101 (25 Dec 2017)

* Added CraftTweaker support for liquid fuels used in the Liquid Compressors and Kerosene Lamp.
* Vortex tube tube now briefly shows its hot and cold sides when placed or rotated
* Fixed the Heat Sink looking like it is facing up when actually facing down.
* Fixed CraftTweaker 'RemoveAllRecipes' function not working for all recipe handlers.
* Fixed item dupe when breaking charging stations holding items (helmet/drone...) with installed upgrades 
* Fixed fully UV Lightbox-ed Empty PCB's stacking with new Empty PCB's
* Fixed explosion-handling event related server crash after killing the wither (and potentially in other situations)
* Fixed keypresses in Programmer GUI "leaking" when the naming textfield was focused (e.g. pressing 'z' would do an Undo action)
* Zoom scrollbar in Programmer GUI can now be dragged with mouse (you can also zoom in/out with mouse wheel)
* Zoom behaviour in Programmer GUI is now more consistent (zoom in & back out and you will be at the same place in the view)
* Fixed NPE when stacking Elevator Base blocks vertically
* A sound effect now plays when writing a program to a Drone.
* Added missing translations for Pneumatic Helmet keybindings.
* Changed default keybinding for opening the Pneumatic Helmet from 'F' to 'U' (as 'F' is the 'switch to offhand' button).
* Fixed Drones causing a crash in very specific situations in combination with FTB Utiltities (having to do with authorization).
* Bugfix: Forestry ethanol does not get registered as a fuel and posts a stacktrace in the log.

## 0.2.0-85 (12 Dec 2017)

* Fixed Pneumatic Helmet block tracker client crash with unopened loot chests
* Fixed Pneumatic Helmet entity tracker client crash when targeting other players
* Fixed Pressure Chamber Interface sometimes becoming an infinite source of items
* Fixed occasional client crash with Gas Lift
* IC2 machines are back: Pneumatic Generator and Electric Compressor (but lacking the pretty models they had in 1.7.10, sorry)
* Air Compressor & Advanced Air Compressor no longer take fuel buckets
* Furnace burn time of fuel buckets is now configurable, and 10x shorter by default than before
* Drone pathfinding is a lot better now
* Drone "Right Click Block" now works a lot better
* Drones now render their held item (can be disabled in config)
* Empty PCB's now stack
* Build artifacts (including API) are now available at https://modmaven.k-4u.nl/me/desht/pneumaticcraft/pneumaticcraft-repressurized/
* Recipes are now pretty much all done with JSON
* Many, many, other minor bugfixes and overall polishing (see https://github.com/TeamPneumatic/pnc-repressurized/commits/master)

## 0.1.0-47 (19 Nov 2017)
* Hotfix: client crash when holding newly crafted GPS Tool or Amadron Tablet
* Minor GUI tweaks for Programmer and Remote

## 0.1.0-45 (18 Nov 2017)
* Initial alpha1 release
