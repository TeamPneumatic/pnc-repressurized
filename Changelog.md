# PneumaticCraft: Repressurized - Changelog

This is an overview of significant new features and fixes by release.  See https://github.com/TeamPneumatic/pnc-repressurized/commits/master for a detailed list of all changes.

Changes are in reverse chronological order; newest changes at the top.

## Minecraft 1.12.2

### 0.7.1-231 (9 Jul 2018)
#### Fixes
* Fixed startup crash under some circumstances (most likely with Thaumcraft not installed) related to new Pneumatic Armor
* No longer attempts to render armor initialisation progress bar if armor piece has no pressure

### 0.7.0-228 (8 Jul 2018)
#### New
* Big new feature: a fully-fledged Pneumatic Armor suite!  Added in Pneumatic Chestplate, Leggings & Boots, each with their own upgrades and abilities (read on...)
* To see all upgrades and abilities for each armor piece, put them in a Charging Station, and open the upgrade inventory from the Charging Station GUI (click the "Inv." button)
* Charging Upgrades (1-5) in the Chestplate charge all pressurizable armor and items you're carrying from the Chestplate's air tank (the Chestplate has a very large tank by default, and all pieces can be further upgraded with Volume Upgrades)
* Security Upgrade in the Chestplate gives protection from fire & lava (pressure cost: high)
* Magnet Upgrade (1-6) in the Chestplate attracts nearby items to the player (pressure cost: very low).  The Magnet respects Immersive Engineering's Conveyor Belts (in fact any item entity with the "PreventRemoteMovement" NBT tag) and Botania's Solegnolia.
* Speed Upgrades (1-3) in the Leggings give a run speed boost (pressure cost: very low)
* Range Upgrades (1-6) in the Leggings give a jump boost (pressure cost: low)
* Dispenser Upgrades (1-4) in the Boots allow you to kick entities for moderate damage and heavy knockback (pressure cost: low)
* The Pneumatic Boots give step assist for free and fall protection for a modest pressure cost (depending on the height of the fall)
* Jet Boots Upgrades (1-8) in the Boots allow limited flight (pressure cost: high)
* Pneumatic Armor has the same armor rating as iron, but +1 Toughness (for comparison, diamond armor has +2 Toughness)
* Armor Upgrades (1-4) in any armor piece increase its protection and toughness.  2 Armor Upgrades will provide protection equivalent to diamond, 4 upgrades is superior to diamond.
* Item Life Upgrades (1-6) in any armor piece allow auto-repair at a cost of pressure.  More upgrades cause faster, but less air-efficient, repair.
* All Pneumatic Helmet functions remain available; just to note that Horses can now be tamed via helmet hacking.
* All features can be toggled on/off using the familiar Pneumatic Helmet options GUI, and as before, keys can be bound to toggle any feature without opening the GUI.
#### Updates
* Quality of Life: shift-clicking a Pneumatic armor piece while in the Charging Station GUI will move it to the right place (armor slots -> charging slot, charging slot -> armor slot, player inv slots -> charging slot)
* Aphorism Tiles are now considered passable by drones, like vanilla signs.  (Note Aphorism Tiles will not be washed away by water, so useful for an underwater base)
* Performance: Aphorism Tiles are no longer ticking tile entities.
* Aphorism Tiles can now be recoloured by right-clicking with any dye (oredict-aware).  Tile border and background can be recoloured independently.
* Drones will now auto-equip the best (highest damage, taking enchantments into account) weapon in their inventory when entering combat (only applies with upgraded inventories, of course).
* Handheld minigun now has a proper rotating model!
* Minigun bullets cause a (cosmetic) particle effect on blocks they hit.  No block breaking, though.
#### Fixes
* Drones have been taught how to melee 1.9 style.  They were still fighting 1.7.10 style, which made them hopeless at melee.
* Hacked drones will no longer obnoxiously shove their owner around; instead they'll come to the owner and land in front of them.
* Fix: better behaviour for drones when targeting entities they can't pathfind to (they were getting stuck in a about-to-teleport loop)
* Fix: server crash if a drone carrying multiple buckets tried to milk a cow.  Now the drone will drop any milk buckets that it has no inventory space for on the ground.
* Fix: Pressure Chambers with multiple valves will now be properly reformed when world is reloaded (previously auxiliary valves didn't think they were part of the multiblock, and leaked air)
* Fix: right clicking a refinery block with a bucket or other fluid container will now extract the right fluid (previously it always tried to extract from the bottom block in the stack, regardless of which block was clicked)
* Fix: the Light Condition puzzle piece was miscalculating light levels

### 0.6.8-219 (27 Jun 2018)
#### Updates
* Change to Drones and Security Upgrades (liquid protection): 1 Security Upgrade will now allow drones to swim through liquids, 2 Security Upgrades will create a temporary 3x3x3 air bubble around drones which are in a liquid, and 3+ Security Upgrades will permanently remove any liquids a drone flies through.  Drones will still never pathfind through lava, though.
#### Fixes
* Fixed Computer Control program piece not getting registered with Open Computers installed
* Fixed drones having their AI overridden by carried entities (Entity Import program piece)
* Fixed inability to place Heat Frames on inventories
* Fixed possible lockup (endless loop) on startup with drones
* Fixed spurious "Drone has no UUID!" error messages on the client
* Fixed occasional crashes when teleporting into areas with client-side (TESR) renderers

### 0.6.7-210 (10 Jun 2018)

#### New
* Added support for Thaumcraft 6.  Thaumcraft Upgrade is now craftable and adds Goggles of Revealing functionality to the Pneumatic Helmet.  Aspects have been added to a few PneumaticCraft items (beyond any Thaumcraft auto-detection).
* The Omnidirectional Hopper and Liquid Hopper now have comparator support to measure their fullness.

#### Updates
* Logistics Drones now grab as many of the requested item type as can fit in a single item stack, instead of taking simply the first stack from the inventory.
* Upgrade descriptions for Pneumatic Helmet & Drones in the Charging Station GUI are now much more complete and accurate; all applicable upgrades are documented, and inapplicable upgrades are no longer shown and cannot be installed (e.g. Logistics Drones can't use Dispenser Upgrades).
* Particles in the Pressure Chamber are slightly less dense now, and render higher inside the chamber.  Allows items in the chamber to actually be seen when the pressure is high; previously particle cloud obscured any items.
* Any items crafted with Air Canisters now get any air in the canister(s)
#### Fixes
* Fix: Sync issue with Pressure Gauge GUI toggling the '<' / '>' button.
* Fix: crash with Ice & Fire Cyclops attacking Sentry Turrets
* Fix: dedicated server crash with drones and the Magnet Upgrade
* Fix: dedicated server crash when hacking mob spawners with Pneumatic Helmet
* Fix: client desync for Logistics Frames after configuring them with Logistics Configurator
* Fix: Logistics Frames & other semiblocks not always sync'ing properly to the client on initial player login
* Fix: items not always transferring in Logistics Network (using tubes & Logistics Modules)
* Fix: it's now possible to place blocks against GUI-less blocks like Compressed Iron Block without needing to sneak-right-click

### 0.6.6-192 (14 May 2018)

#### Updates
* The Universal Sensor no longer uses air in the "Constant" redstone emitter mode (still requires minimum pressure though)
#### Fixes
* Fix: crash when right-clicking some PneumaticCraft blocks with other mods' wrenches
* Fix: another fix to Speed Upgrade crafting with stacks of more than one item in the table

### 0.6.5-189 (11 May 2018)
#### Fixes
* Fix: crash when crafting Speed Upgrades in some situations.
* Fix: Gas Lift now works with infinite fluid blocks such as Better With Addons Aqueducts.

### 0.6.4-186 (3 May 2018)

#### Updates
* Compressed Iron Gear now added to Ore Dictionary as "gearIronCompressed".  This allows Thermal Expansion Compactor to craft Compressed Iron Gears when the Gearworking Die Augment is installed.
* Plastic Mixer JEI page now uses Ore Dictionary to show the dyes instead of hard-coded vanilla dye items. (Mods and/or packs can remove vanilla dyes from the oredict so hardcoded items can be misleading)
#### Fixes
* Fix: client crash when looking at RFTools Powercells with the Pneumatic Helmet Block Upgrade Tracker active (and RF scanning enabled).  The RF scanner still doesn't get the RF level; a more comprehensive data syncing framework for the Pneumatic Helmet is being considered.
* Fix: Removed Upgrade Info tab from Refinery GUI (the block doesn't take any upgrades)
* Fix: Nasty item-equip sound loop being played when Pneumatic Helmet equipped and Charging Station GUI open

### 0.6.3-181 (25 Apr 2018)
#### Fixes
* Fix: Shift-click dupe bug (introduced in 0.6.0) when clicking items into some PneumaticCraft inventories.
* Fix: Dyeing a drone by right-clicking it now only uses one dye from the stack, not two.
* Fix: Blocks can now be placed against the Charging Station & Elevator Caller with sneak-right-click.

### 0.6.2-179 (24 Apr 2018)
#### Fixes
* Fix: Server crash when Spawner Agitators are used in conjunction with the Despawning Spawners mod.
* Fix: Omnidirectional Hoppers now respect the sidedness of the block they pull from (e.g. will no longer pull input items or fuel from the bottom side of a vanilla furnace).

### 0.6.1-176 (22 Apr 2018)
#### Fixes
* Fix: Crash with liquid lookup with JEI.
* Fix: Drones being quirky sometimes when digging.

### 0.6.0-174 (21 Apr 2018)

#### New
* Added Harvesting Drone, and Harvesting Piece.
* QoL improvement: Speed Upgrades can now be crafted with fluid containers holding more than a bucket of lubricant.  The Liquid Hopper works for this, and the Thermal Expansion and EnderIO tanks can also be used.  Other mods' containers may also work.
#### Updates
* QoL improvement: adding extra Refinery blocks to an existing stack will now automatically redistribute any output fluids to the appropriate tanks, so the Refinery can continue to run.  E.g. adding a Refinery to a 2-block stack which already contains Diesel and LPG will auto-move the LPG from the second to the third (newly added) block.
* Programmable Controller: all faces except the bottom face can be used to access the fake drone's inventory.  The bottom face can be used to insert or extract the programmable item (Drone or Network API).
* QoL improvement: Thermopneumatic Processing Plant now only accepts items/fluids that can be used in recipes.
#### Fixes
* Fix (cosmetic): Amadron no longer shows slot highlights where there isn't a trade widget (i.e. on the last page of trades).
* Fix: Breaking any PneumaticCraft tile entity with an auto-smelt pick (e.g. from Tinker's Construct) no longer smelts the tile entity's contents.
* Fix: MCMP2 support hard-disabled for now (switching it on in config would crash your game before)
* Fix: Elevator Caller now works properly when you have more than 12 floors
* Fix: Programmable Controller inventory handling now works properly (inventory manipulation widgets were accessing the controller's 1-slot inventory instead of the fake drone's inventory)
* Fix: Removing the programmable item (Drone or Network API) from the Programmable Controller's slot now properly stops and resets the running program.
* Fix: Rotating PNC blocks with other mods' wrenches should no longer also open the block's GUI.
* Fix: The Charging Station now only allows 1 item to be inserted, to prevent duping Machine Upgrades.
* Performance: fluid tank rendering now uses a FastTESR, better for client FPS.

### 0.5.1-163 (21 Mar 2018)
#### Updates
* Drones now also render held hoes upside down (just like other tools).
* Inserting/extracting the top side of the Programmable Controller block inserts/extracts "drone" inventory.
#### Fixes
* Fixed the Programmable Controller since the initial port (most notably a crash involving FTB Utilities).
* Fix server crash with Mekanism cardboard box & spawner agitator

### 0.5.0-159 (26 Feb 2018)
#### New
* Added GPS Area Tool, a way to make selecting areas with the Programmer easier.
* You can 'paste' Coordinate puzzle pieces in the Programmer by taking a GPS Tool and left-clicking it on the programming area (the GPS Area Tool does the same for the Area piece).
* Oil worldgen can now be blacklisted by dimension ID (e.g. disable oil generation in Twilight Forest) - see I:oilWorldGenBlacklist in pneumaticcraft.cfg
#### Updates
* Drones & Logistics Drones can now be spawned from a Dispenser
* Altered camera orientations for some held items (wrenches & other tools - thanks Teamspen210)
* Gas Lift air (pressure) usage when extending the tube is now dependent on the hardness of the block being broken (stone is the same, softer blocks are cheaper, and obsidian is much more expensive to break)
* Advanced Pressure Tubes can now be used in the Gas Lift, and will reduce the pressure cost to break blocks to 50%
* The Spawner Agitator is now found 10x less frequently in dungeon loot chests (it can still be crafted, though)
* Reworked some event handling code which should provide a significant performance improvement in worlds with many loaded tile entities
#### Fixes
* RF Import & RF Export programming pieces are now available (they were in, but registration was getting skipped due to a bug).  Also note that despite the name, these pieces work with Forge Energy, which is RF-compatible.
* Fixed a crash with the Amadron Tablet when adding a trade when not having a item supply location bound to the tablet
* Fixed Drones sometimes refusing to teleport while they should (most likely to notice with Place Block commands)
* Fixed entity filters in Pneumatic Helmet entity tracker module not working on dedicated servers
* Hopefully fixed server-side NPE with Programmable Controller and FTB Utilities claim protection
* Fixed Gas Lift being able to break unbreakable blocks (bedrock, ender portal frames...)

### 0.4.1-141 (10 Feb 2018)
#### New
* Added config option 'B:explosionCrafting', true by default. Setting this to false disables explosion crafting of compressed iron. If you disable this, you'll need another way to get initial compressed iron (e.g. via a CraftTweaker recipe)
#### Fixes
* Hopefully made PNC:R more robust with compatibility with other mods adding "oil" as a fluid.  PNC:R will now log an error and disable oil worldgen if it can't find oil as a block, rather than crashing the instance.
* Picking up modded fluids with Ceramics Clay Bucket no longer converts the Clay Bucket to a vanilla iron Bucket.
* Drone "Condition: Items" puzzle piece now works properly.
* Fixed potential client-side NPE in Aphorism Tiles when drama text is unavailable.

### 0.4.0-135 (1 Feb 2018)
#### New
* Programmer Area enhancements: Sphere, Cylinder and Pyramid types can now be configured as 'hollow', and the Area widget configuration GUI now has a much cleaner layout
* Programmer enhancement: added a search textfield, shown when the full widget tray is expanded, allowing easy location of puzzle pieces by name
* Programmer widget tray can now be toggled with the Tab hotkey (in addition to the existing Space hotkey), possibly useful when the new search field has focus
* Aerial Interface: added support for CoFH "Essence of Knowledge" experience fluid
* Aerial Interface: the Experience tab in the GUI now has a button to select the desired fluid type when you have multiple mods providing experience fluids (the old method of inserting some fluid to set the accepted type no longer works)
* Added two ComputerCraft methods for Elevators: getCurrentHeight() and getTargetHeight()
#### Updates
* Drone death messages to the owner are now more informative (include reason why drone died)
* Minigun tracers and Vortex entities no longer look so weird (bad X offset) when fired from the left hand
* IGW updates to Aerial Interface and Programmer wiki pages
#### Fixes
* Fixed client being kicked when trying to load very large drone programs from pastebin (exceeding 32K client->server packet limit; now sent in multiple packets - 1.7.10 had a higher packet size limit)
* Fixed client crash when using variables to define areas (related to changes in 0.3.0 regarding size limit validation)
* Fixed client crash in Programmer when zooming all the way out, exiting Programmer and then re-opening it
* Fixed some XP calculation inaccuracies in the Aerial Interface; pumping XP in & out should not cause any unexpected XP loss or gain now
* Fixed missing textures in the Area wiki page

### 0.3.1-125 (18 Jan 2018)
#### Fixes
* Fix AbstractMethodError crashes introduced in 0.3.0.

### 0.3.0-124 (17 Jan 2018)
#### New
* Added Transfer Widget, an early game item/fluid transfer item that can be placed _between_ blocks.
* Added Spawner Agitator, which keeps Mob Spawners active even if players are not around.
* Added Crop Support, an item which when placed on a crop improves growth speed.
#### Updates
* Network Data Storage item now shows required puzzle pieces in its item tooltip when holding Shift
* Big performance improvement when searching in the item search GUI
* Better default orientation of the Omnidirection Hopper when it's placed: output now faces the clicked block, and input faces the player.
* The size limit for programming puzzle piece areas has been increased from 100,000 to 250,000 and can now be adjusted in config (see I:maxProgrammingArea)
* The size limit for programming puzzle piece areas is now validated in the Programmer before the drone is programmed, instead of forcing a drone with an over-large area to suicide after it's placed.
* Performance improvement: the tile entities for Pressure Chamber Wall/Glass & Elevator Frames no longer tick. Pressure Chamber Glass should be fine to use in small-scale decorative builds, or for its excellent blast-resistant properties. 
* Lots of work (mainly cleanup) on the In-Game-Wiki docs.
#### Fixes
* Fixed (hopefully) reflection-based crashes on startup when running with certain ASM-using coremods
* Fixed NPE's when breaking certain inventories or tanks with a Logistics Frame attached
* Fixed item dupe when Logistics Drone imported from inventories under certain circumstances
* Breaking any PneumaticCraft: Repressurized inventory in creative mode now drops the inventory contents instead of voiding them
* Fixed clientside crash when selecting the "Check For Air" option in "Condition: Block" programming widget
* Fixed Elevator Callers not working above the level of Elevator Frames - frames can now stop two blocks below the top Elevator Caller (thanks TeamSpen210)

### 0.2.2-102 (25 Dec 2017)
#### Fixes
* Fixed crash when removing an opened Pneumatic Door in survival mode.

### 0.2.1-101 (25 Dec 2017)
#### New
* Added CraftTweaker support for liquid fuels used in the Liquid Compressors and Kerosene Lamp.
#### Updates
* Vortex tube tube now briefly shows its hot and cold sides when placed or rotated
* Zoom scrollbar in Programmer GUI can now be dragged with mouse (you can also zoom in/out with mouse wheel)
* A sound effect now plays when writing a program to a Drone.
* Changed default keybinding for opening the Pneumatic Helmet from 'F' to 'U' (as 'F' is the 'switch to offhand' button).
#### Fixes
* Fixed the Heat Sink looking like it is facing up when actually facing down.
* Fixed CraftTweaker 'RemoveAllRecipes' function not working for all recipe handlers.
* Fixed item dupe when breaking charging stations holding items (helmet/drone...) with installed upgrades 
* Fixed fully UV Lightbox-ed Empty PCB's stacking with new Empty PCB's
* Fixed explosion-handling event related server crash after killing the wither (and potentially in other situations)
* Fixed keypresses in Programmer GUI "leaking" when the naming textfield was focused (e.g. pressing 'z' would do an Undo action)
* Zoom behaviour in Programmer GUI is now more consistent (zoom in & back out and you will be at the same place in the view)
* Fixed NPE when stacking Elevator Base blocks vertically
* Added missing translations for Pneumatic Helmet keybindings.
* Fixed Drones causing a crash in very specific situations in combination with FTB Utiltities (having to do with authorization).
* Bugfix: Forestry ethanol does not get registered as a fuel and posts a stacktrace in the log.

### 0.2.0-85 (12 Dec 2017)
#### New
* IC2 machines are back: Pneumatic Generator and Electric Compressor (but lacking the pretty models they had in 1.7.10, sorry)
* Build artifacts (including API) are now available at https://modmaven.k-4u.nl/me/desht/pneumaticcraft/pneumaticcraft-repressurized/
#### Updates
* Air Compressor & Advanced Air Compressor no longer take fuel buckets
* Furnace burn time of fuel buckets is now configurable, and 10x shorter by default than before
* Empty PCB's now stack
* Recipes are now pretty much all done with JSON
#### Fixes
* Fixed Pneumatic Helmet block tracker client crash with unopened loot chests
* Fixed Pneumatic Helmet entity tracker client crash when targeting other players
* Fixed Pressure Chamber Interface sometimes becoming an infinite source of items
* Fixed occasional client crash with Gas Lift
* Drone pathfinding is a lot better now
* Drone "Right Click Block" now works a lot better
* Drones now render their held item (can be disabled in config)
* Many, many, other minor bugfixes and overall polishing (see https://github.com/TeamPneumatic/pnc-repressurized/commits/master)

### 0.1.0-47 (19 Nov 2017)
#### Updates
* Minor GUI tweaks for Programmer and Remote
#### Fixes
* Hotfix: client crash when holding newly crafted GPS Tool or Amadron Tablet

### 0.1.0-45 (18 Nov 2017)
* Initial alpha1 release
