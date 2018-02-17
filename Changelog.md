# PneumaticCraft: Repressurized - Changelog

This is an overview of significant new features and fixes by release.  See https://github.com/TeamPneumatic/pnc-repressurized/commits/master for a detailed list of all changes.

Changes are in reverse chronological order; newest changes at the top.

## Minecraft 1.12.2

## x.x.x-xxx (Unreleased)

* Added GPS Area Tool, a way to make selecting areas with the Programmer easier.
* You can 'paste' Coordinate puzzle pieces in the Programmer by taking a GPS Tool and left-clicking it on the programming area (the GPS Area Tool does the same for the Area piece).
* (Logistics) Drones can now be spawned from a Dispenser.
* Fixed a crash with the Amadron Tablet when adding a trade when not having a item supply location bound to the tablet.
* Fixed Drones sometimes refusing to teleport while they should (most likely to notice with Place Block commands).
* Altered camera orientations for some held items (wrenches & other tools)
* Oil worldgen can now be blacklisted by dimension ID (e.g. disable oil generation in Twilight Forest)
* Using entity filters in Pneumatic Helmet entity tracker module now works on dedicated servers
* RF Import & RF Export programming pieces are now available (they were in, but registration was getting skipped due to a bug).  Also note that despite the name, these pieces work with Forge Energy, which is RF-compatible.
* The Spawner Agitator is now found far less frequently in dungeon loot chests (it can still be crafted, though)

## 0.4.1-141 (10 Feb 2018)

* Hopefully made PNC:R more robust with compatibility with other mods adding "oil" as a fluid.  PNC:R will now log an error and disable oil worldgen if it can't find oil as a block, rather than crashing the instance.
* Added config option 'B:explosionCrafting', true by default. Setting this to false disables explosion crafting of compressed iron. If you disable this, you'll need another way to get initial compressed iron (e.g. via a CraftTweaker recipe)
* Picking up modded fluids with Ceramics Clay Bucket no longer converts the Clay Bucket to a vanilla iron Bucket.
* Drone "Condition: Items" puzzle piece now works properly.
* Fixed potential client-side NPE in Aphorism Tiles when drama text is unavailable.

## 0.4.0-135 (1 Feb 2018)

* Programmer Area enhancements: Sphere, Cylinder and Pyramid types can now be configured as 'hollow', and the Area widget configuration GUI now has a much cleaner layout
* Programmer enhancement: added a search textfield, shown when the full widget tray is expanded, allowing easy location of puzzle pieces by name
* Programmer widget tray can now be toggled with the Tab hotkey (in addition to the existing Space hotkey), possibly useful when the new search field has focus
* Aerial Interface: added support for CoFH "Essence of Knowledge" experience fluid
* Aerial Interface: the Experience tab in the GUI now has a button to select the desired fluid type when you have multiple mods providing experience fluids (the old method of inserting some fluid to set the accepted type no longer works)
* Added two ComputerCraft methods for Elevators: getCurrentHeight() and getTargetHeight()
* Drone death messages to the owner are now more informative (include reason why drone died)
* Minigun tracers and Vortex entities no longer look so weird (bad X offset) when fired from the left hand
* Fixed client being kicked when trying to load very large drone programs from pastebin (exceeding 32K client->server packet limit; now sent in multiple packets - 1.7.10 had a higher packet size limit)
* Fixed client crash when using variables to define areas (related to changes in 0.3.0 regarding size limit validation)
* Fixed client crash in Programmer when zooming all the way out, exiting Programmer and then re-opening it
* Fixed some XP calculation inaccuracies in the Aerial Interface; pumping XP in & out should not cause any unexpected XP loss or gain now
* Fixed missing textures in the Area wiki page
* IGW updates to Aerial Interface and Programmer wiki pages

## 0.3.1-125 (18 Jan 2018)

* Fix AbstractMethodError crashes introduced in 0.3.0.

## 0.3.0-124 (17 Jan 2018)

* Added Transfer Widget, an early game item/fluid transfer item that can be placed _between_ blocks.
* Added Spawner Agitator, which keeps Mob Spawners active even if players are not around.
* Added Crop Support, an item which when placed on a crop improves growth speed.
* Network Data Storage item now shows required puzzle pieces in its item tooltip when holding Shift
* Big performance improvement when searching in the item search GUI
* Better default orientation of the Omnidirection Hopper when it's placed: output now faces the clicked block, and input faces the player.
* The size limit for programming puzzle piece areas has been increased from 100,000 to 250,000 and can now be adjusted in config (see I:maxProgrammingArea)
* The size limit for programming puzzle piece areas is now validated in the Programmer before the drone is programmed, instead of forcing a drone with an over-large area to suicide after it's placed.
* Fixed (hopefully) reflection-based crashes on startup when running with certain ASM-using coremods
* Fixed NPE's when breaking certain inventories or tanks with a Logistics Frame attached
* Fixed item dupe when Logistics Drone imported from inventories under certain circumstances
* Breaking any PneumaticCraft: Repressurized inventory in creative mode now drops the inventory contents instead of voiding them
* Fixed clientside crash when selecting the "Check For Air" option in "Condition: Block" programming widget
* Performance improvement: the tile entities for Pressure Chamber Wall/Glass & Elevator Frames no longer tick. Pressure Chamber Glass should be fine to use in small-scale decorative builds, or for its excellent blast-resistant properties. 
* Lots of work (mainly cleanup) on the In-Game-Wiki docs.
* Fixed Elevator Callers not working above the level of Elevator Frames - frames can now stop two blocks below the top Elevator Caller (thanks TeamSpen210)

## 0.2.2-102 (25 Dec 2017)

* Fixed crash when removing an opened Pneumatic Door in survival mode.

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
