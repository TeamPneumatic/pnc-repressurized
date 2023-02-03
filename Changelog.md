# PneumaticCraft: Repressurized - Changelog

This is an overview of significant new features and fixes by release.  See https://github.com/TeamPneumatic/pnc-repressurized/commits/master for a detailed list of all changes.

Changes are in reverse chronological order; newest changes at the top.

# Minecraft 1.18.2

* PNC:R 3.2.3 and later *require* Forge 40.1.20 or later
* PNC:R 3.2.0 and later *require* Forge 40.1.0 or later and JEI 9.7.0 or later

## 3.5.1
* Fixed a Minigun ammo dupe

## 3.5.0

### Updates
* Charging Modules now run 4x faster when on Advanced Pressure Tubes
* Now supports Holding enchantment from latest CoFH Core releases (thanks @Daniel0Widing)
  * Older versions of CoFH will work with this release of PNC:R, minus Holding enchantment compat
* New functionality for the Pneumatic Chestplate Item Launcher (thanks @Daniel0Widing)
  * Support for Botania Minecarts
  * Support for Thermal Foundation TNT and Grenades
  * Support for Thermal Locomotion Minecarts
  * Fixed handling of vanilla boat launching
* Added Create wrench to third-party wrench compat

### Fixes
* Fixed some Omnidirectional Hopper item transfer logic
* Fixed Minigun not rendering when game in left-handed mode (thanks @Daniel0Widing)
* Fixed Drone Interface drone conditions not evaluating correctly
* Hopefully fixed locale-related issue with modifying pressure values in tube module GUI's
* Fixed lockup when shift-clicking bad items into the Security Station GUI
* Fixed Pneumatic Door Base block model allowing X-ray vision past it
* Several pneumatic items now no longer use up air when used when player is in creative mode
* Fixed Heat Frame becoming disconnected from heat handling block entities on world reload

## 3.4.4

### Updates
* Entities spawned by a Pressurized Spawner now have a `pneumaticcraft:pressurized_spawner` persistent entity tag
  * May be useful to identify them with mods like KubeJS

### Fixes
* Jetboots sounds and particles now play properly to nearby players on SMP
* Fixed potentially severe server TPS hit in Security Station checking for blocks in unloaded chunks
  * Unlikely to occur as part of normal player interaction, but automation mods like Modular Routers and others can trigger it

## 3.4.3

### Fixes
* Fixed potential crash when calculating loot tables for certain modded blocks when dug by drones
* Hopefully fixed some chunk corruption issues related to using Kerosene Lamps with unloaded chunks nearby
* Fixed Logistics Modules not working correctly on Reinforced & Advanced Pressure Tubes

## 3.4.2-137 (14 Sep 2022)

### Updates
* A few new pieces of API:
  * `IMiscHelpers#airParticle`
  * Exposed some blockstate properties in `PNCBlockStateProperties`
  * Added `ITubeNetworkConnector` interface to support using blocks other than Pressure Tubes to connect tube modules (Logistics and Redstone)
  * Mainly to support new Compressed Creativity functionality
  * Compressed Creativity is a PNC:R/Create integration addon mod - it's cool, check it out!

### Fixes
* Fixed a problem where XP fluids shown in the Aerial Interface GUI could get out of sync with server-known fluids
* Fixed issue where right-clicking a formed Pressure Chamber glass block would activate the held object too
  * E.g. right-clicking with a water bucket would both open the GUI and pour out the water
* Fixed potential performance issue with Heat Frame on inventories full of items, particularly on servers with many known smelting recipes

## 3.4.1-131 (31 Aug 2022)

### Updates
* Added clientside config setting `pressure_chamber_particles` to control whether air particles are displayed inside a pressurized Pressure Chamber
  * Note: Some optimiztion mods like Oculus or Rubidium have been reported to have compatibilty issues with these particles; setting this to false is a workaround
  * https://github.com/TeamPneumatic/pnc-repressurized/issues/1079

### Fixes
* Fixed an issue where inventory scanning by the Pneumatic Armor Block Tracker did not always pick up changes to inventories
* Spawner Agitators have reduced functionality when Apotheosis is installed
  * Item tooltips and Patchouli manual page for Agitator have been updated accordingly
  * Apotheosis makes significant changes to vanilla Spawners which the Agitator does not work well with
  * In addition, Apotheosis provides much more sophisticated spawner control than the Agitator does
  * Mobs spawned when an Agitator attached are still protected from despawning when no players are near
* Fixed Vacuum Trap being able to absorb mobs spawned by a Mob Spawner with a Spawner Agitator attached
* Fixed potential enchantment duping issue related to Drones and Astral Sorcery perks
* Fixed Jetboots continuing to fire if they're switched off while the thrust key is currently held down
* Removed obsolete block heat properties data for Quark Brimstone blocks (no longer in Quark in 1.18+)
  * Was causing annoying but harmless error messages in server log

## 3.4.0-124 (3 Aug 2022)

### Updates
* It is now possible to filter on entities only from a certain mod with the `(mod=<modname>)` modifier
  * E.g. filtering on `@mob(mod=minecraft)` will match hostile mobs only from vanilla and not any other mod
  * This feature was backported from the 1.19 release
* Seismic Sensor now tells you the depth of both the top and bottom fluid blocks of lakes that it finds
  * Makes it easier to judge the deepest part of the lake if placing a Gas Lift to pump it
* Drones now know how to use Micromissiles
  * If Drone is carrying a Micromissile when the Entity Attack widget executes, it will fire a missile at its current target
  * Entity filters are supported; the Drone will copy the Entity Attack widget's filter to the missile (if a filter exists)
* Explosives from Thermal Foundation (Ice/Earth/Lightning charges) can now be launched as weaponry
  * From the Air Cannon (Dispenser upgrade)  
  * From the Pneumatic Chestplate item launcher (Dispenser upgrade)
* Made Micromissile overall lifetime configurable (missile entities will be removed from server after this if they haven't exploded yet)
  * Adjustable in config via `max_lifetime` setting in common config, Micromissiles section
  * Default is 600 ticks, which is twice the default fueled flight duration (`lifetime`)

### Fixes
* Fixed Redstone Modules not syncing settings properly on Reinforced or Advanced Pressure Tubes (only worked on basic tubes)
* Fixed Pneumatic Armor not being repairable in an Anvil (with Compressed Iron Ingots)
  * Also added a page in the Patchouli book overview listing the ways to repair Pneumatic Armor
* Fixed potential client crash when teleporting into an area with currently-leaking tubes or machines
* Fixed Transfer Gadgets not being placeable on blocks which offer only a fluid handler capability, e.g. Refineries
* Fixed broken elevator image in Patchouli manual
* Fixed Pressure Chambers behaving weirdly if constructed or deconstructed by Modular Routers Extruder Modules
  * Probably also applies to other automated block placing/breaking systems
* Fixed Drones not being able to use the Right Click action on an item with cooldowns more than once
* Fixed a few issues with Pneumatic Helmet Block Tracker:
  * Fixed blocks not being trackable below Y=0 or above Y=255
  * Fixed PNC chest loot not being generated when inspected with block tracker inventory mode
  * Loot generation no longer occurs for players in spectator mode
  * Vanilla-style locked chests (requiring a named item to be held) are now honoured by the block tracker
* Compressed Iron Gears are now included again in the `forge:gears` and `forge:gears/compressed_iron` item tags
* Fixed (non-fatal) exception logged by Amadrones despawning

## 3.3.1-110 (4 July 2022)

### Updates
* Added new `getDronePositionVec()` Lua method to the Drone Interface
  * Returns drone's position as a vector, easier to extract individual x/y/z values (e.g. `x = getDronePositionVec().x`)
  * Existing `getDronePosition()` method is unchanged, for backwards compatibility

### Fixes
* Fixed Patchouli book breaking if certain recipes are removed/altered
* Liquid Compressor (and Advanced Liquid Compressor) now have a boolean blockstate property "on" to indicate if they're running
  * Intended to make it easier for mods like AdPother to check their status
* Fixed Spawner Agitator not working (even when chunkloaded) if there are no players in the dimension it's in
* Fixed a misleading item tooltip for tiered upgrades (Jet Boots Tier 5 showed up as "5 x Jet Boots Tier 1")
* Fixed a visual bug which sometimes occured when wrenching tubes (tubes appearing connected but leaking or vice versa)
* Villagers are now blacklisted from being extracted by the Omnidirectional Hopper & Entity Tracker Upgrade
  * This was done because a) it's a bit exploitable and b) can lead to buggy behaviour with villager trades 
  * Other entities can also be blacklisted by adding them to the `pneumaticcraft:omnihopper_blacklisted` entity type tag
* Fixed `$deploy_pos` special Drone variable not persisting properly across world restarts
* Jet Boots: when already Elytra-gliding, transition to powered Jet Boots flight is now smoother (doesn't cause a brief stall in velocity anymore)

## 3.3.0-99 (18 Jun 2022)

### New
* Added the Elytra Upgrade for the Pneumatic Chestplate
  * Allows full vanilla-style Elytra gliding while wearing the chestplate
  * No durability damage, but a minor air cost while actively gliding
  * Toggleable upgrade, can be switched on & off like any other upgrade
  * Works well in conjunction with Jet Boots and the Smart Hover mode (glide with Elytra, use Jet Boots when needed to gain speed/altitude)

### Updates
* Heat Sink cold damage is now more vanilla-like, using the same effect as Powder Snow
  * Wearing Compressed and/or Pneumatic Armor negates cold damage between -30C and -60C, and reduces damage taken below -60C
  * Any armor in `minecraft:freeze_immune_wearables` item tag will reduce cold damage taken
* Related: Minigun Freezing Ammo now applies vanilla freeze effects to mobs when hit
  * Now checks for entities in the vanilla `minecraft:freeze_hurts_extra_types` entity tag 
  * Remains extremely effective against Nether mobs
* Gas Lift minimum pressure requirement now rises more slowly with drill depth
  * Allows deeper drilling (max was 90 blocks, now 180 blocks) - suitable for increased 1.18 world heights
* Creative Upgrade can now be installed in Pneumatic Armor pieces
  * Prevents all item damage and negates all air usage
  * Particularly useful in Chestplate in conjunction with Charging Upgrades

### Fixes
* Fixed Harvesting Drones ignoring IE Hemp
* Fixed very tall Elevators sometimes not rendering when extended
* A couple of fixes to PNC fuels when used in a non-bucket container as vanilla Furnace fuels
  * Tanks which would get voided (e.g. Mekanism tanks) will no longer go in the Furnace fuel slot
  * Furnace is only fueled proportional to the amount of fluid in the tank instead of always 1000mB worth of fuel
* A couple of fixes to Drone auto-charging behaviour
  * In particular, Drones which are completely out of air will no longer attempt to claim a Charging Station, locking all other Drones out of it
* Fixed UV Lightbox losing the contents of the output slot on world reload

## 3.2.4-93 (3 Jun 2022)

### Updates
* Gamestages integration has been re-added
  * As in 1.16.5, Amadron trades can be limited by the gamestages a player has
  * See https://github.com/TeamPneumatic/pnc-repressurized/wiki/Amadron-and-Datapacks#player-filtering for more info

### Fixes
* Fixed server crash with Assembly IO Unit under some circumstances
* Drones are now able to descend through Scaffolding
* Fixed the `/give` command creating uninteractable item entities when used to give Empty, Unassembled or Failed PCB's
* Fix Network Node items not being able to be placed in a Security Station
* Fixed Vortex Tube model sometimes wrongly showing tube connectors on the heat-handling faces (cosmetic issue)
* Fixed Drone Condition: Upgrade causing a server crash (ArrayIndexOutOfBoundsException)
* Fixed mistake in example Lua program in the manual page for the Drone Interface

## 3.2.3-84 (19 May 2022)

### Updates
* CoFH integration has been re-added
  * Holding enchantment is supported on any pressurizable item, and increases item volume by (1 + enchantment_level)
  * CoFH fuel compat for Creosote, Refined Fuel, Refined Biofuel and Tree Oil
  * CoFH Crude Oil should work in the Refinery
* CraftTweaker integration should now work correctly for Fluid ingredients using fluid tags (updated for 1.18.2 `TagKey` support)
* Pufferfish can now be hacked (forces them to puff up)
* Squid can now be hacked (converts them to Glow Squid)
* Pressure Tube QoL feature: if wrenching shut a straight section of tube, also close the adjacent tube section if it would start to leak

### Fixes
* Fix some mobs (e.g. Zoglins) being able to control drones which have imported them
  * This affects mobs added in newer Minecraft versions (1.16+) which have a very different AI implementation from older mobs
* Fixed Drones getting stuck on Amethyst clusters
  * Actually a vanilla bug (MC-181565) but worked around in PNC for now
* Fixed pastebin import of Coordinate and Area widgets in legacy (pre-1.18) Drone programs
  * X/Y/Z and variable information was getting lost
* Pneumatic Boots step assist now uses the Forge STEP_HEIGHT entity attribute rather than modifying the player's step height directly
  * Much better inter-mod compat this way

## 3.2.2-79 (8 May 2022)

### Updates
* API: better exposure of methods for registering custom upgrades (primarily around new `IUpgradeRegistry` API class)
  * Also deprecated `IUpgradeAcceptor` interface, which is no longer needed

### Fixes
* Fixed buckets not having fluid handler functionality (noticeable when trying to cool a bucket of Molten Plastic in a heat-framed inventory)
* Fixed fluid tag handling bug in recipes which prevented Mekanism fuels being recognised as usable in PNC machines
* Fixed harmless-but-annoying "Could not serialize" errors the log when a player logs into dedicated server

## 3.2.1-75 (30 Apr 2022)

### Fixes
* Hotfix: fix client startup failure if JEI isn't installed

## 3.2.0-74 (28 Apr 2022)

### Updates
* Re-added Mekanism integration
  * Same functionality as in 1.16.5: heat compatibility, Harvesting drones can use Paxels, Radiation Shielding upgrade for Pneumatic Armor
* Re-added Create integration
  * Same functionality as in 1.16.5: Pneumatic Wrench can be used on Create belts like a Create wrench can be
* Added some Programmer/JEI integration functionality:
  * Opening a JEI window while the Programmer GUI is open will now show a "+" (Move Items) button in JEI
  * Clicking this will create Item Filter widgets for each input item in the displayed recipe
  * If a Crafting widget with no filters is in the programming area, it will auto-add filters to quickly set up a recipe for the Crafting widget
* Added a 'nbt_to_client_modification' config setting, default true
  * When true (default and previous behaviour), PNC will modify NBT in items sync'd to client to reduce network traffic
  * However this can under some circumstances (which are not entirely clear at this time) cause players to be kicked
  * If you see this problem, set 'nbt_to_client_modification' to false. Note this will increase network traffic from server to client, particularly for items where NBT changes rapidly, e.g. firing Minigun or using Pneumatic Armor Jet Boots
  * This should be considered a workaround until a proper fix is determined

### Fixes
* Fixed Elevators not working below Y=0 (new 1.18 world height limits)
* Fixed some NBT <-> JSON conversion behaviour when exporting Drone programs to clipboard or pastebin
  * Specifically this caused Tag Filter tag information to be lost in conversion
* Fixed crash with Drone Condition: Pressure widget when used in Programmable Controller
* Fixed Pneumatic Armor feature-enable config not getting read properly on dedicated server (i.e. game didn't remember which armor features were switched on)

## 3.1.5-70 (11 Apr 2022)

### Updates
* Added whitelisting config settings for oil lakes, in addition to existing blacklisting settings
  * See new `oil_world_gen_whitelist`, `oil_world_gen_category_whitelist` and `oil_world_gen_dimension_whitelist` settings in `pneumaticcraft-common.toml`
  * Non-empty whitelists take priority over blacklists

### Fixes
* Fixed tube modules not always properly marking their pressure tube as needed save
  * Caused intermittent issues with modules disappearing or losing their settings after world restart
* Fixed CME under some circumstances when unloading a chunk containing a Spawner Agitator
* Fixed crash with invalid entity filters on an upgraded Air Grate Module

## 3.1.4-68 (30 Mar 2022)

**This version requires a Forge release of 40.0.32 or later**

### Updates
* Added a "check sight" checkbox to the Entity Attack programming widget
  * If checked, drones won't attack entities they can't directly see
* Pneumatic Boots with the Flippers upgrade now allow walking on Powder Snow in the same way Leather Boots do

### Fixes
* Fixed Security Station allowing invalid items to be piped in, causing a crash
* Fixed Refinery has-work output not working for some custom recipes
* Two computer control (CC:Tweaked) fixes:
  * Fixed crash when using setSides() Lua call in conjunction with setAction("block_right_click")
  * Fixed condition actions, e.g. setAction("condition_redstone"), always return false when evaluated in a Lua program
  
## 3.1.3-67 (25 Mar 2022)

### Updates
* Reworked dungeon loot system
  * Loot is not hardcoded anymore, but instead loaded from datapack: see `data/pneumaticcraft/loot_tables/custom/{common,uncommon,rare}_dungeon_loot.json`
  * `data/pneumaticcraft/loot_modifiers/dungeon_loot.json` still controls which loot tables those three custom loot tables are added to
  * config setting `enable_dungeon_loot` works again as a "master switch" to completely disable adding custom loot to any pool

### Fixes
* Fixed Air Grate causing server NPE
* Fixed adding a Module Expansion Card to a tube module not always marking the tube as needs-save (causing expansion card to be lost on world reload)
* Fixed combobox widgets (e.g. Redstone Module gui) not displaying their choices correctly
* Fixed Logistics Module rendering causing client crash

## 3.1.2-64 (21 Mar 2022)

**This release requires a Forge release of 40.0.18 or later**

### Updates
* The vortices fired by the Vortex Cannon are now much more effective at breaking vegetation

### Fixes
* Fixed Redstone Module rendering causing client crash
* Fixed Charging Station wrongly transferring upgrades from one item to another under some circumstances
* Fixed semiblock (logistic frames, crop supports etc.) chunk load/unload behaviour
  * Unloading and reloading chunks with semiblocks was causing some desyncs

## 3.1.1-61 (18 Mar 2022)

**This release requires a Forge release between 40.0.12 & 40.0.17 inclusive.  40.0.18 is a breaking change for many mods and will not work for this release.**

### Updates
* Drone debugging now works for External Program widgets; the visible debugged widgets will update as the Drone enters/leaves sub-programs
* Added `pneumaticcraft:no_oil_lakes` configured structure feature tag
  * Configured structure feature ID's can be added to this tag to prevent oil lakes from generating within those structures
  * Default is to include `#minecraft:village` so oil lakes will never generate within a village
  * Can be modified via datapack: `data/pneumaticcraft/tags/worldgen/configured_structure_feature/no_oil_lakes.json`
* Shift-clicking on buttons in side configuration GUI tabs now cycles backwards through the possible values
  * Also applies to Smart Chest side-based push/pull setup
* Minor improvement to drone Dispenser behaviour
  * All drone types (Collector/Guard/Logistics/Harvesting) can now be deployed via Dispenser
  * Dispense behaviour now looks for a block 2 blocks in front of the dispensing direction and uses that to "click" the drone against
  * Note that drones deployed via Dispenser do not have an "owner", which may be problematic if terrain protection mods are active

### Fixes
* Fixed Foreach widgets (Coordinate and Item) not functioning properly when running in a subprogram called by an External Program widget
  * External Program wrongly interpreted the end of the foreach subroutine as end of the entire program
* Added Huge Tank to the `pneumaticcraft:tanks` block & item tags
* When breaking a Spawner with a Spawner Agitator attached, Spawner player activation range is now reset
  * This isn't normally important, but when mods are present which allow dropping Spawners as items (e.g. Apotheosis), it becomes important
* Fixed Foreach widget loops not working properly in external programs (i.e. run by External Program widget)
* Fixed External Program widget running forever
  * Now it will run each program in the target inventory once (in order), then move to the next widget in the program
* Fixed Item Filter order mattering in `Condition: Item Filter` when using a Tag Filter
  * Previously the Tag Filter had to be in the second slot, now it can be in either
* Fixed Sentry Turret idle yaw position not matching placing player's yaw
* Fixed Smart Chest not saving its upgrade between world reloads
* Fixed Drones not remembering their upgrades properly when dropped as an item (when killed or wrenched)
* Fixed a possible server-side crash related to placing heat-using blocks
  * Not one I could reproduce myself, but extra defensive coding has been added to ensure a crash is impossible in these circumstances
  * NOTE: this does require an API break in the `HeatBehaviour` API class; generics are no longer used or required here

## 3.1.0-56 (9 Mar 2022)

This release is just a straight port of 3.0.0 (1.18.1) to Minecraft 1.18.2. No player-visible changes.

# Minecraft 1.18.1

## 3.0.0-53 (7 Mar 2022)

This release is based on the latest 1.16.5 code, with the following changes and additions:

### New and Updated Gameplay Features
* New armor upgrades:
  * Ender Visor: Pneumatic Helmet upgrade (toggleable) to prevent Enderman aggro
  * Gilded: Pneumatic Armor upgrade (any slot) to prevent Piglin aggro
  * Stomp: Pneumatic Boots upgrade: when active, if you hit the ground hard enough to engage the normal fall damage reduction, you also damage nearby mobs proportional to how fast you were moving (uses extra air per mob damaged)
* New Tube Junction block, allows two separate lines of Pressure Tubes to cross without connecting to each other
  * Tube Junction handles any tier of pressure tube
* New Tube Module: the Vacuum Module
  * It's basically the Vacuum Pump in module form - useful for inserting onto a vertical tube section (where a full-size pump wouldn't fit)
  * Not as upgradable as the Vacuum Pump in terms of speed or volume, but a Module Expansion Card (formerly Advanced PCB) does boost its speed somewhat
* There are now two types of global variable (as used by Drones, GPS Tools, Universal Sensors & Remotes)
  * Player-global variables, prefixed with a "#", are individual to each player on a server
  * Server-global variable, prefixed with a "%", are common to *all* players (this is how global variables worked in 1.16.5)
* Drone programming: Foreach Coordinate widget no longer uses the special pos (0,0,0) to break the loop
  * This was always dubious, since (0,0,0) could be valid in a void world, but even more so now in 1.18+ with the new build height limits
  * Now, setting the control variable to any position outside the current world's build height breaks out of the foreach loop
* There is a new pressure tier, informally known as "tier 1.5"
  * It's intended to be optional, in that it's not required for progression to tier 2 technology but can be useful to unlock a few features before tier 2
  * Danger air pressure of 10 bar, hard maximum of 12 bar
  * Available early on, but Reinforced Pressure Tubes need plastic to make
  * The Thermal Compressor is now a tier 1.5 compressor
  * The Thermopneumatic Processing Plant is now a tier 1.5 machine (but of course still works fine with tier 1 compressors)
  * Note that the Charging Station is still a tier 2 machine
    * Which means with a tier 1.5 Thermal Compressor, it is now possible to charge items to 10 bar before tier 2 is unlocked
    * Charging Machine now accepts pressure from below, so it can be placed on top of a Thermal Compressor (but using Reinforced Pressure Tubes will likely make for tidier builds)
  * Some default recipes have changed:
    * Diamond Drill Bit now needs 7.5 bar in the TPP (but can now mine 3x3, which previously required the Netherite Drill Bit, only available after Printed Circuit Boards)
    * Netherite Drill Bit now requires a Diamond Drill Bit to craft, in addition to a Netherite Ingot
* Minigun Item Life upgrades now restore ammo quite a bit faster (but air usage is increased to match)
  * If you add the Unbreaking (any level) enchantment to your ammo item, it will no longer disappear when used up, allowing it to be repaired
* New block: Smooth Plastic Construction Brick™
  * Comes in the usual 16 dye colors
  * Shapeless craft from standard Plastic Construction Brick™ (smelt to convert back to Plastic Sheets)
  * Good for building
  * Doesn't hurt to walk on; in fact, it increases your walk speed!

### QoL improvements
* "Used by" tooltip for upgrades now scrolls if over 12 lines
* Programmable Controller: minidrone now renders its held item
* The Advanced PCB is now known as the Module Expansion Card (because that's what it is)
* The Printed Circuit Board is now known as the Finished PCB
  * Done for consistency - searching for "PCB" in JEI now finds all PCB-related items
* Miniguns now use particles for their bullet tracers - no more ugly line drawing; looks way better
* Programmer has a new "90" button on left-hand side (when in Advanced difficulty mode)...
  * Intended for use when you have a base Coordinate widget that all your positions are relative to, as generated by the existing "Convert to Relative" button
  * Rotates all Coordinate widgets which look like offsets 90 degrees; useful if you've downloaded a program from pastebin and want to orient it a different way
  * "Looks like offset" means it's a coordinate that's being added to another coordinate with a variable that was set in the very first Coordinate of the program.
* Maximum screen roll when flying with Jet Boots is now configurable in client config
  * Default max is 35 degrees - configurable from 0 (no roll) up to 90 (bring your travel sickness pills)
* Villager houses now use Compressed Stone rather than Reinforced Stone in their construction
  * This makes them look more like they used to (before the 2.15.0 retexture)
* Wall Lamp blocks are now waterloggable

### Configuration and Management
* PneumaticCraft: Repressurized dev builds are now built with [GitHub Actions](https://github.com/TeamPneumatic/pnc-repressurized/actions) rather than Jenkins
* All PneumaticCraft commands are now under the common `/pncr` root:
  * `/pncr dump_nbt` to display held item NBT (needs admin privilege)
  * `/pncr amadrone_deliver` to force an Amadrone delivery (needs admin privilege)
  * `/pncr global_var {list|get|set|delete}` to manage global variables
* Config file has been reorganised and cleaned up a fair bit
  * New Drones section in `pneumaticcraft-common.toml`
  * Dropped `explosionCrafting` and `coalToDiamonds` - both of these can be achieved with recipe datapacks
  * Dropped `vacuum_trap_blacklist`
    * Use `pneumaticcraft:vacuum_trap_blacklisted` entity type tag now
  * Dropped `seismic_sensor_fluids` and `seismic_sensor_fluid_tags`
    * Use `pneumaticcraft:seismic_sensor_interesting` fluid tag now
    * Default remains the same: `#forge:crude_oil`, which include the `pneumaticcraft:crude_oil` fluid
  * Oil lake frequency values are now done slightly differently in config
    * Separate `underground_oil_lake_frequency` and `surface_oil_lake_frequency` settings
    * Frequencies are now "1 in x" values rather than a percentage as in 1.16.5
    * E.g. value of 25 means a 1 in 25 chance of trying to generate a lake in any given chunk
* Electrostatic Compressor now looks at the `pneumaticcraft:electrostatic_grid` block tag to determine what blocks can be used for the grid
  * Default is just `minecraft:iron_bars`
* Patchouli manual is now a [resource-pack-based book](https://vazkiimods.github.io/Patchouli/docs/upgrading/upgrade-guide-117#resource-pack-based-books)
  * This means modpack & resource pack makers can modify it more easily...
* Vanilla loot chest modification is now done properly with Global Loot Modifiers
  * The actual loot item list is fixed, but amounts are adjustable in datapacks, see `data/pneumaticcraft/loot_modifiers/dungeon_loot`
  * Loot item list is larger than it used to be...

# Minecraft 1.16.5

## Dependencies

* PNC:R 2.11.0 and later *require* Forge 36.0.42 or later.
* PNC:R 2.15.1 and later *require* Forge 36.2.0 or later, **and** Patchouli 1.16.4-50 or later.

## 2.15.2-303 (4th Feb 2022)

### Updates
* Amadrone spawn-in location is now adjustable in config (`pneumaticcraft-common.toml`)
  * See `amadrone_spawn_location` and `amadrone_spawn_location_relative_to_ground_level` settings
  * May be useful in stoneblock-style worlds, where Amadrones can have trouble finding a place to spawn with default settings
* PneumaticCraft Diesel & Biodiesel are no longer registered (in code) with Immersive Engineering as generator fuels
  *  IE is moving away from code-based registration, since this is better done with datapacks
* Compressed Iron Gears are now item-tagged as `forge:gears` and `forge:gears/compressed_iron`
* Basic drones can now also be dyed via crafting recipe, same as standard Drones can be
  * Dyeing in entity form (right-clicking drone with dye) also still works
  * All drones now also show their dye color when in item form
  * All basic drones now have a default dye color matching their main body (logistics = red, guard = blue, harvester = green, collector = yellow)

### Fixes
* Logistics Frames marked as invisible now fade in & out as they used to
* Fixed Universal Sensors getting entity filters mixed up when multiple sensors use an "Entities In Range" sensor setting
* Fixed Radiation Shielding Upgrades not protecting against Mekanism radiation (was built against Mek 10.0.x API)
  * PNC:R now *requires* Mekanism 10.1.x (assuming it's installed) - it will no longer work with Mekanism 10.0.x
* Fixed a few machines (Charging Station, Creative Compressor & Creative Compressed Iron Block) not always persisting their data across world reloads
* Hopefully fix a CME causing player kicks when drones are deployed under certain circumstances (possibly related to carried equipment?)
  * I could not reproduce this one myself, so hard to know for sure if it's fixed, but some extra-defensive coding has been added

## 2.15.1-297 (19th Jan 2022)

### Updates
* Patchouli is now a mandatory dependency!
* CoFH Core Holding enchantment is now supported even when Thermal Expansion isn't installed
* A bit more retexturing work, thanks Rid:
  * Pressure Mechanic Villagers (and zombies!) have acquired stylish new costumes
  * GUI's and GUI icons have had a facelift
  * Updated Reinforced Stone (and associated other blocks) texture
* QoL: when in Jet Boots builder mode, the Jump key now also fires the jet boots, causing vertical upward movement
  * Useful if the Thrust key has been rebound (from the default - Space Bar), allowing more creative-like height control when in builder mode
* Camouflage Applicator can now draw blocks from a Dank Storage
  * Will work for any item which offers a standard Forge item handler capability
  * Does not include Shulker Box items, or Botania's Black Hole Talisman, sorry

### Fixes
* Fixed upgrade management button not working for Amadron Tablet in the Charging Station
* Fixed AE2 Requester integration not persisting properly across world restarts
  * Also fixed overflowing text in the GUI side tab for AE2 Requestercg integration
* Fixed Omnidirectional Hopper & Liquid Hopper visual appearance when Creative Supply Upgrade installed
  * Creative-upgraded hoppers also got a visual makeover, thanks Rid
* Fixed missing place-sound effect when any blocks are placed against Pressure Chamber walls

## 2.15.0-289 (10th Jan 2022)

### New
* A complete retexture of the entire mod, courtesy of Ridanisaurus
  * No functional (gameplay) changes in this release, just a whole lot prettier all round!
  * The CTM mod is now an optional dependency, and needed if you want texture-connected Pressure Chamber Glass
  * The texture of the various Reinforced Stone blocks has changed; if you want to use the old textures, look at the new Compressed Stone blocks

## 2.14.7-283 (1st Jan 2022)

### Updates
* Security Station no longer exempts creative-mode players from block protection by default
  * Only server ops (command permission level >= 2) are now fully exempt
  * Old behaviour can be restored by setting `security_station_creative_players_exempt` to true in config

### Fixes
* Fixed NPE with Security Station and fakeplayers with incomplete game profiles on integrated servers
* Wrenching blocks (belts, shafts) from the Create mod with the Pneumatic wrench now works properly, disconnecting belts/shafts as expected
* Fixed Air Grate in vacuum mode (negative pressure) lowering the pressure when it used air instead of raising it (i.e. moving toward 0)
* Fixed client crash when clearing Pneumatic Armor keybindings via armor GUI, under some circumstances
* Fixed Logistics Requester Frame integration with AE2 not functioning correctly
* Fixed a server-side crash related to invalid villager trades
  * Seems like some mod is possibly adding trades with empty cost or result?  in any case, PNC:R now ignores these for Amadron purposes

## 2.14.6-277 (24th Nov 2021)

### Fixes
* Fixed Aerial Interface allowing Charging Module to charge items through it when its pressure is under 10bar
* Some fixes around inline tube module (Regulator & Flow Detector) behaviour:
  * Fixed inline tube modules being placeable on the side of a tube which is already connected to something
  * Fixed Flow Detector preview not rendering transparent
  * Stopped rendering previews for modules not placeable in the focused position
  * Made clearer in the manual (Tube Modules page) that inline modules may only be placed on the open end of a tube
* Fixed problems with Pressure Chamber valves exploding on multiblock break/reform when there are Volume Modules installed
* Fixed clientside block shapes (Pressure Tubes in particular) not properly updating on block rotation and pressure tube side toggling
* Fixed manual page for Pneumatic Armor stating that Item Life Upgrade max was 6 (it is in fact 5)
* Fixed empty fluid ingredients (`<fluid:minecraft:empty>`) not working properly in CraftTweaker recipes
  * While such recipes were being added OK, they did not show up in JEI
* Right-clicking a formed pressure chamber with a placeable block in hand no longer briefly shows the block being placed before opening the GUI
  * Cosmetic problem only, but irritating nonetheless...

### API and Development
* Added a couple of API methods (both for fairly specific usages):
  * Added `IPneumaticCraftInterface#deserializeSmartChest()`, intended to make it easier to include Smart Chests in Create contraptions
  * Added `forceClientShapeRecalculation()` which can be called by addon mods if they rotate/break a block (on the server) which can connect to Pressure Tubes - this notifies clients to update neighbouring Pressure Tube block shapes (the client doesn't normally get notified about block updates like this)
* All Java source files now include license headers (GPLv3 for the main mod, LGPLv3 for the API)
* Included a copy of COPYING.LESSER (LGPLv3 licence) in the source distribution

## 2.14.5-272 (10th Nov 2021)

### Updates
* The Amadron Tablet can now accept Volume Upgrades (place it in a Charging Station to upgrade)
* Amadron Tablet filtering for custom recipes now also supports Gamestages (if the Gamestages mod is installed)
  * See https://github.com/TeamPneumatic/pnc-repressurized/wiki/Amadron-and-Datapacks (Player Filtering section) for more info
* The Vacuum Trap can now blacklist entities by entity type tags
  * Tags are specified in `vacuum_trap_blacklist` same as entity types, but start with a "#"
* Oil worldgen can now be blacklisted by dimension ID as well as biome type
  * See the `oil_world_gen_dimension_blacklist` config setting
  * Also supported is wildcarded dimension ID's, e.g. adding "somemodname:*" will prevent oil generation in *all* dimensions added by the mod "somemodname"

### Fixes
* Fixed several client-side config settings (e.g. Programmer widget difficulty) not properly "taking" when modified from client GUI's
* Fixed problem where inserting fluids very slowly into Refinery or TPP would not always trigger a new recipe search
* Fixed NPE related to flowing fluids coming into contact with Heat Sinks or other heat-handling blocks
* Fixed Charging Station continuing to emit redstone in "Item Inserted and Idle" mode, if an item is later removed from the station

## 2.14.4-258 (24th Sep 2021)

### Fixes

* Hotfix: fix server crash related to changes made in 2.14.3 with Amadron villager trade scanning
  * Appears to be an unexpected interaction between Quark and/or Terraforged

## 2.14.3-257 (23rd Sep 2021)

### Updates
* Added the ability to filter Amadron offers by the player's location, intended for modpack makers
  * This allows for certain offers only to be available in certain places
  * Filtering can be by whitelist or blacklist, and by dimension ID's and/or biome categories
  * No existing offers are filtered by default
  * See https://github.com/TeamPneumatic/pnc-repressurized/wiki/Amadron-and-Datapacks for more info
* Added `drones_can_be_picked_up` config setting (default is true, following previous behaviour)
  * If set to false, nothing will be able to pick drones up (boats, minecarts or any other entity)
* Added an "Interpolate Threshold" redstone mode for the UV Lightbox
  * In this mode, the completion threshold percentage is 25 + (signal_level * 5)
  * Threshold cannot be manually adjusted in this mode
  * A signal of 0 in this mode disables processing (so effective range 30% to 100% in increments of 5)
* Added Stonecutter recipes for the various Reinforced Stone blocks
* Added `$deploy_pos` special Drone variable
  * This returns the blockpos at which the drone was deployed
  * For Programmable Controller, it returns the blockpos at which the controller was placed

### Fixes
* Fixed slider widgets holding onto a mouse drag if mouse button released while not over the widget in question
* Prevent machine pressures rising to silly levels if machine has a Security Upgrade but is receiving air faster than it can vent it
  * Pressure now clamped to machine's critical level (7 or 25 bar)
* Fixed GPS Tool '+' and '-' buttons messing up the numeric value
  * Another artifact of the switch to Mojang mappings in 2.14.0 (a method name got mixed up)
* Fixed server crash when Smart Chest tries to push items into a Blood Altar under certain circumstances
* Fixed villager house generation ignoring the `addMechanicHouse` config setting
* Fixed Pneumatic Door in "wooden door" or "iron door" mode not opening on world load even if signal present (until block update received)
* Fixed Pneumatic Door in "nearby and looking" mode requiring *all* nearby players to be looking at the door
  * Only one player needs to be looking at the door

## 2.14.2-247 (6 Sep 2021)

### New
* Added new Display Shelf block, which is basically a half-height Display Table
  * Same functionality as the Display Table
  * Unlike Display Table, will hide legs completely if no solid block beneath
  
### Updates
* Amadron Tablet allows searching of offers by mod; use `@string` syntax, same as JEI or AE2
* Slightly tweaked the Amadron Tablet GUI display of unaffordable offers for a better appearance

### Fixes
* Fixed crash when opening machine GUI's under some circumstances
  * Tag-related bug introduced in 2.14.1 if the `forge:tools/wrench` item tag wasn't already defined by another mod
  * Related: added `pneumaticcraft:pneumatic_wrench` to `forge:tools/wrench`
* Fixed Amadron tablet allowing fluid orders > 576000mB (taking full payment but only deliverying 576000mB)
  * 576000 is a hard maximum for a single fluid order (the most a fully-upgraded drone can carry)
* Fixed Amadrones failing to import fluid from any side of a tank other than the top
  * Issue is noticeable e.g. with Mekanism Dynamic Tanks and a valve on the side
* Villager Amadron offers now actually show a vendor name of "Villagers" on dedicated server

## 2.14.1-245 (31 Aug 2021)

### Fixes

* Fixed non-fatal client errors from JEI regarding missing item for Fire->Air heat transition recipe
* Manometer now shows correct block when block has no associated item, e.g. Fire
* Manometer now shows extracted heat for block with a tile entity, e.g. Campfires
* Fixed Patchouli manual not working if Tough As Nails is installed 
  * TAN page was written back in 1.12.2 days, and TAN 1.16 does not have all the features that it did in 1.12
* Fixed build artifacts not being published to modmaven.dev after build system overhaul in 2.14.0
  
## 2.14.0-227 (27 Aug 2021)

Note: despite the major version bump, only minor player-visible changes related to 2.13.5 in this release.
This is an alpha intended for testing API updates, and a heavily-overhauled build system.

### Build System Updates
* Now using Gradle 7 and ForgeGradle 5.1+
* Now using official Mojang mappings plus parchment mappings
* Now outputting Javadoc JAR as part of build

### API Updates
* The API JAR is now properly reobfuscated, like the main JAR. To use the API JAR as a dependency, do something like:
  * `compileOnly fg.deobf ("me.desht:pneumaticcraft-repressurized:${pnc_version}:api")`
* The API has been moved into its own proper sourceset (`src/api/java/.../api`) instead of living under `src/main/java/.../api`
* API updates intended to make it easier to add your own pressurizable items 
  * New interface `IPressurizableItem`
  * Added `IAirHandler.Provider` abstract class, and `IItemRegistry#makeItemAirHandlerProvider()` method to get an instance
  * This intended to be returned from `Item#initCapabilities()`
  * Added `PressureHelper` API class, although only method so far is for volume upgrade calculation
* Various Pneumatic Armor API cleanup
  * Internally, "trigger" key handling for upgrades (e.g. hack, kick, launch item) is moved into upgrade handler code, out of core
  * See `IArmorUpgradeClientHandler#getTriggerKeyBinding()` and `IArmorUpgradeClientHandler#onTriggered()`
  * Added `IPneumaticHelmetRegistry#makeKeybindingCheckBox()` method for getting a checkbox widget for upgrade toggling
  * All part of effort to make it easier to add custom upgrades, although the API for this isn't yet complete...

## Updates

* Third party modded wrench detection now also checks for the item being in the `forge:tools/wrench` item tag

## Fixes

* Fixed CraftTweaker removeRecipe() (to remove by output item) not working for Pressure Chamber and Explosion Crafting recipes
* Fixed mobs spawned by Pressurized Spawner despawning when players are too far away

## 2.13.5-218 (25 Aug 2021)

### Fixes
* Hotfix: server crash when Smart Chest tries to pull from Hopper Botany pot above

## 2.13.4-215 (20 Aug 2021)
  
### Updates
* Entity Attack widget can now take a max entity count
  * Allows drone to stop attacking and proceed with next widget in program, even if there are more targets to attack
* Improvements to Manometer functionality
  * Right-clicking any block now shows its name
  * Right-clicking air now shows the ambient temperature for the area
  * Right-clicking non-tile entity heat blocks (e.g. Magma) now shows the heat extraction/absorption percentage if applicable (e.g. if used to heat or cool a machine)
  
### API Updates
* Pressure Chamber custom recipe enhancement: added item-aware `getCraftingPressure(IItemHandler chamberHandler, List<Integer> ingredientSlots)` method variant in `PressureChamberRecipe` API class
  * Allows for item-sensitive pressure requirements in custom recipes (which extend the API class `PressureChamberRecipe`)
  * Old 0-arg `getCraftingPressure()` method is deprecated and will be removed in next major Minecraft release
* Added new interface `IPneumaticCraftProbebable` and new block tag `pneumaticcraft:probe_target`
  * Blocks which inherit the interface or are added to the block tag will be treated like PNC blocks for the purposes of TOP & WAILA display
  * Previously this required extending the non-API `BlockPneumaticCraft` class, which is not recommended
  * Useful for blocks added by PNC addon mods
* Added new interface `ISpawnerCoreStats` to manage Spawner Core items
* Recipe types are now registered with the recipe interface type (previously mistakenly registered with the recipe implementation class)
  * This means mods adding custom recipe types don't need to extend the implementation class, which was never intended

### Fixes
* Fixed Pressure Chamber (4x4x4 and 5x5x5 sizes) miscalculating base volume if broken and rebuilt - thanks @s-l-lee
* Fixed Pressurized Spawner ignoring Speed Upgrades on initial spawn countdown when reloading world
* Documented Drone XP Orb importing functionality in the manual (see Entity Import Widget page)

## 2.13.3-211 (3 Aug 2021)

### Updates
* A few updates to ru_ru translations (thanks @shikhtv)

### Fixes
* Fixed machines sometimes losing air on world reload (under certain circumstances, based on pressure & Volume Upgrade count)
* Village house generation has been redone to be properly datapack-friendly
  * No player-visible change, but previous generation could break datapack-based worldgen due to modifying world registries instead of dynamic registries
* Fixed some block highlighting (GPS tools etc.) not showing the highlight for air blocks
* Fixed possible server crash if an Air Compressor explodes (due to overpressure) on the same tick as it toggles on/off
* Fixed up some hardcoded messages (added translations) and made some other messages more translation-friendly
* Several ComputerCraft fixes:
  * `getDroneName()` Lua call now returns the drone's name instead of `nil`
  * `getOwnerID()` Lua call now returns the drone owner's UUID instead of `nil`
  * `setArea()` Lua call (7-param version) now properly accepts all the area types listed by `getAreaTypes()`
  * The area shown by `showArea()` will now disappear as soon as the drone dies (wrenched, killed, etc.) instead of hanging around until the next `hideArea()` call

## 2.13.2-205 (19 Jul 2021)

### New
* New block: Creative Compressed Iron Block
  * This creative-only block maintains a constant temperature, similar to how the Creative Compressor maintains a constant pressure
  
### Updates
* Significant internal refactoring of Pneumatic Armor upgrade handling
  * No player-visible updates, but a fair bit of code cleanup & optimisation has been done
* Entity filter: `@mob` will no longer match tamed mobs
  * While there aren't any tameable mobs in vanilla, other mods can add them, e.g. tamed Quark's Foxhounds are now safe from your Sentry Turrets & Guard Drones

### Fixes
* Fixed bug where PNC Fluid Tanks would push any fluid into a Memory Stick, treating it as XP
* Items in a Pressure Chamber no longer render too dark
* Fixed Air Compressors running with no fuel when Advent of Ascension is also installed
  * This is actually an AoA bug where empty itemstacks end up with a fuel value, but I've added extra checks in PNC to prevent this happening
* Fixed Programmer only using/returning Puzzle Pieces for a single item when writing a program to a stack of multiple Network API items
* Fixed right-clicking slots to split stacks not working in the Programmer GUI

## 2.13.1-202 (6 Jul 2021)

### New
* Added a new Jet Boots Smart Hover mode
  * Toggle it off & on in the Jet Boots Armor GUI screen (no specific upgrade tier needed)
  * When off, Jet Boots behave exactly as before
  * When on, hovering only engages when the thrust key is pressed. Stepping off an edge or jumping will *not* enable hovering.
* Added new keybinding for Jet Boots thrust
  * This used to be hardcoded to the vanilla Jump key binding (Space by default)
  * Default for new keybinding is Space, same as before
  * Can be set via armor GUI (Jet Boots page) as well as vanilla Options -> Controls screen  
  * Changing this to something other than the vanilla Jump keybinding is particularly useful in conjunction with Smart Hover Mode

### Updates
* Jackhammer now highlights to-be-broken blocks when in any multibreak mode
  * Should reduce the risk of accidentally breaking half your house...
* Charging Station GUI now has a button to toggle "upgrade only" mode
  * In this mode, no transfer of air to/from items happens, and no air leaks for unconnected stations occur
  * Useful if you want to carry a Charging Station around for on-the-go management of item upgrades
* CraftTweaker docs are now available on the official CraftTweaker documentation site: https://docs.blamejared.com/1.16/en/index/, Mods -> PneumaticCraft: Repressurized
* Better block shape behaviour for invisible Aphorism Tiles
  * Invisible tiles now have no block shape at all, unless you sneak
  * Makes them more attractive as chest labels (especially when displaying items...)

### Fixes
* Fixed in-world Yeast crafting destroying blocks if trying to spread to a waterlogged block
* Fixed voiding of Mekanism fluid tanks when trying to craft Speed Upgrades with them
  * Mekanism fluid tanks can no longer be used in PNC:R fluid crafting recipes
* Heat Frames can no longer be placed on PNC:R fluid tanks (or any block with a fluid capability)  
  * This was causing too much confusion: Heat Frames are intended for item inventories, not fluid tanks
  * It only went on the PNC:R fluid tank because the tank has an item capability for the bucket slot
* Fixed the fireballs launched by the Chestplate Item Launcher (from Fire Charges) hanging around forever
* Fixed Pneumatic Armor Jump upgrade only allowing forward jumps, even if moving backwards or sideways
  * Also slightly increased the horizontal velocity of the forward jump, when already sprinting
* Fixed Refinery GUI sometimes wrongly reporting missing Refinery Outputs (outputs were found by server OK but client missed them)
* Fixed Minigun rendering weirdly when in offhand
  * Note that Minigun still needs to be in main hand to fire, but at least looks right when carried in offhand now
  * Also fixed odd rendering rotation when player inventory is open
* Fixed Minigun not spinning up or firing if multiple Miniguns are being carried

## 2.13.0-199 (24 Jun 2021)

### New

* Added back CraftTweaker support!
  * Docs will be added to https://docs.blamejared.com/ but in the short term can be browsed at https://github.com/TeamPneumatic/CraftTweaker-Documentation/tree/pncr-docs-1.16/docs/mods/PneumaticCraft-Repressurized
  
### Updates
* Air Grate Module is now able to absorb XP orbs
  * If there's an adjacent fluid tank, orbs will be converted to Memory Essence and stored in the tank (20mB fluid per XP point)
* Updated ru_ru translations
* Added config setting pneumaticcraft-common.toml -> Heat -> `addDefaultFluidEntries` which controls whether heat properties for modded fluids will be automatically added
  * Note that vanilla water and lava are still always auto-added; this setting is just for modded fluids
  * If set to no, the intention is that heat properties for modded fluids should be added by the modpack maker as required (see https://github.com/TeamPneumatic/pnc-repressurized/wiki/Block-Heat-Properties)
* Elevator now has a `getVelocity()` Lua method for ComputerCraft purpose
  * Returns current velocity in blocks/tick; negative values indicate elevator is descending
* Blocks found by the Pneumatic Helmet Block Tracker misc mode can now be controlled by block tag
  * Use the `pneumaticcraft:block_tracker_misc_blocks` tag
  * Defaults are the same as before: TNT, Tripwires, Bee Nests and all silverfish-infested stone blocks
  * **Important**: resist the temptation to add very common blocks to this (e.g. `#forge:ores`) since excessive results can cause severe client-side FPS drops
* Crop Supports now pass player right-clicks along to the enclosed block more intelligently
  * Makes planting and bone-mealing crops in a Crop Support far easier
* Right-clicking an Aphorism Tile which is invisible now passes the right-click to the block behind
  * This makes invisible Aphorism Tiles useful for labelling chests etc. with text or displayed items; the chest behind can now easily be opened with a right-click
  * Sneak+right-click an invisible Aphorism Tile with an empty hand to open the editor GUI
* All minecart types are now tracked with the Pneumatic Helmet Entity Tracker
  * Note that Chest & Hopper Minecart inventories are *not* displayed
  * Note that `@minecart` can be useful here as an entity filter to match all minecarts
* Significantly reduced Pneumatic Helmet Block Tracker network chatter, especially in areas with many inventories
  * Was sync'ing more inventory data from server to client than really necessary
* Pneumatic Helmet Entity Tracker mob targeting warnings are now coalesced by mob type, where feasible
  * Reduces HUD message spam when there are many hostiles about
  * Also got rid of the "Stopped spam on Entity Tracker!" message since it's not very useful to players

### Fixes
* Fixed another dedicated server crash related to mods which query fuels before world is available
  * It's a workaround; mods which query fuels very early (e.g. Minecolonies & Simple Generators) just won't see PNC:R fuel buckets, but no crash!
* Fixed Pneumatic Helmet night vision occasionally failing to deactivate properly
  * Note that when a Helmet with an installed Night Vision Upgrade is equipped, other sources of night vision (e.g. potions) will not function 
* Fixed client crash in Aphorism Tile GUI when typing alt-characters outside normal Minecraft format code ranges (0-9, a-f, l, m, n, o, r)  
* Fixed Air Grate Module ignoring item entities
  * Also fixed a slight bounding box misalignment for grates facing west & north which caused entities very near the grate to be ignored
* Fixed machines not always properly recalculating their incoming redstone signal on world reload
  * Caused problems with machines running when they shouldn't and vice versa
* Work around a client crash when Pressure Glass is part of a Create schematicannon preview
  * Actually a Create issue which will be fixed in next Create release, but this is a bandaid fix for now
* Fixed pressure module (Pressure Gauge/Regulator/Safety Valve) GUIs flicking from large to small on open when the module has an Advanced PCB installed
* Fixed player pose not being reset when flying with Jet Boots and running out of air
* Fixed client crash when closing inventory search window from GPS Area Tool GUI

## 2.12.5-190 (25 May 2021)

### Updates
* Added new `$owner_look` drone special variable
  * Returns a vector where X/Y/Z can be any of -1, 0 or 1 based on the direction the drone's owner is currently facing
* Programmer GUI: better tooltip data on the "Export Program" button
  * Tooltip now shows both required and available puzzle pieces for the current program, and greys out the button if insufficient pieces

### Fixes
* Fixed Remote layouts exported from older PNC versions not importing
* Fixed negative numbers not working in Coordinate widget GUI
* Fixed server reload crash when Minecolonies also present
  * Related to timing issue of Minecolonies furnace fuel discovery triggering PNC fuel recipe searching too early
* Fixed fluids not rendering in JEI heat properties views
  * Also sorted heat properties output (when viewing all properties) by temperature then display name
* Fixed typo'd API class: `IHeatExhangerAdapter` -> `IHeatExchangerAdapter`
  * Class was only added in 2.12.4, getting in quick...

## 2.12.4-188 (18 May 2021)

### Fixes
* Fixed dumb GUI bug making it impossible to enter negative numbers in numeric textfields
  * Caused problems with GPS Tool coordinates in particular

## 2.12.3-187 (17 May 2021)

### Fixes
* Fixed mod compat problem causing crashes with new TheOneProbe (3.1.x) versions
* Fixed potential item dupes with Thermopneumatic Processing Plant in conjunction with the logistics system
* Fixed a couple of broken links in the Patchouli manual

## 2.12.2-186 (13 May 2021)

### Updates
* Clarified Chestplate & Security Upgrade fire protection behaviour in Charging Station side tab & manual
  * Note that the Chestplate Security upgrade only dumps air (to extinguish fire & solidify lava) if the player doesn't already have fire protection
  
### Fixes
* Fixed client crash when opening Pneumatic Armor GUI and there are no armor pieces with an upgrade GUI available
  * E.g. wearing only chestplate with no upgrades
  * Now the Core Components page is always visible, regardless of which armor pieces & upgrades are in use
* Fixed dumb bug introduced in last update which broke the pressure textfield in the Pressure Gauge GUI (Advanced PCB)

## 2.12.1-184 (3 May 2021)

### Updates
* Added config setting `in_world_plastic_solidification` (default: true) to control in-world conversion of Molten Plastic to Plastic Sheets
* Added config setting `in_world_yeast_crafting` (default: true) to control in-world creation of Yeast Culture
* Both of the above settings are intended to be useful to modpack makers who may wish to control how these things are produced
* A couple of entity filter additions:
  * Added `holding=` and `holding_offhand=` modifiers to require an entity to be holding a specific item
  * E.g. `@player(holding=pneumaticcraft:minigun)` will only match players holding a Minigun
  * Also added negations for modifiers, e.g. `@player(holding!=stick)` will only match players *not* holding a Stick
  * The item is an item ID in the usual form; the mod prefix can be omitted if it's `minecraft:`
* The Aerial Interface now renders the owning player's actual head on the front of its block, when the player is online
* Player left-clicks on **invisible** Logistics Frames are now passed through to the framed blocks
  * This means left-clicking blocks like Storage Drawers or Mekanism Bins now works as expected when there's a frame on the front of the block
  * When the frame is **visible**, left-clicks cause the player to punch the frame (eventually knocking it off in the same way that punching a boat breaks it)
* Hwyla & The One Probe now show the framed inventory name when looking at Logistics Frames
* The recipe for Speed Upgrades now accepts Lubricant by fluid tag (`forge:lubricant`) instead of specifically PneumaticCraft's Lubricant
  * This makes for better cross-mod compat, e.g. Immersive Petroleum's Lubricant can be used
* Air Grates now use a small amount of air to cool Heat Sinks
  * It was previously free, which was a mistake; air is supposed to be used up for this
  * Usage is 5mL/t + 3mL/t (rounded down) for every three Heat Sinks cooled

### Fixes
* Fixed Redstone Module comparator input mode only working for measuring items in the adjacent inventory
  * Now any & all comparator conditions are properly measured (fluid levels, refinery "has work" etc.)
* Fixed comparators on Refinery Output blocks not being notified when the Refinery's "has work" condition changed
  * Only the Refinery Controller was properly notifying its attached comparator
* Fixed Drone & Sentry Turret's Minigun spin-down sound being played at player's location instead of at Drone or Sentry Turret
  * That's why it sounded much too loud...
* Fixed the Assembly IO Unit (input) not handling split stacks in the input inventory in cases where the recipe requires a stack of multiple input items
* Fixed some drone Right Click behaviour, around actions that modify the drone's inventory
  * Specifically, drones which right click beehives with a stack of bottles no longer void the bottles but instead correctly harvest the honey
* Fixed a couple of pneumatic armor inconsistencies relating to removing upgrades from armor when the upgrade is still switched on
* Fixed Fluid Tanks not allowing an empty bucket (or other fluid container item) to be inserted via the GUI
  * It has always worked to pipe a bucket/tank in, but the GUI slot wasn't configured to allow empty containers
* Fixed Universal Sensor not always correctly calculating its range on world reload
  
## 2.12.0-177 (16 Apr 2021)

### Updates
* Logistics Frames are now sided, meaning multiple frames can be placed on one block!
  * Greatly reduces the need for auxiliary input/output inventories and tanks when using logistics with machines
  * The side can no longer be changed via the GUI; you will need to detach and replace a frame to change its side
  * Rendering has been altered to show a frame only on the side that the frame was placed on
* Logistics Frames now have independently-settable item and fluid white/blacklisting
  * In addition, the button to switch white/blacklisting is now on the main GUI, not the Filter side tab
* Logistics Frames GUI: clicking a fluid-containing item (e.g. bucket or tank) on a liquid filter slot will now copy the contained fluid into that slot
* Logistics Drones will now completely ignore any frames whose facing side is obstructed by any solid (non-pathfindable) block
  * This means that drones won't attempt to handle any frame which already has a Logistics Module on it, so drones and modules now co-exist better
* Breaking machines with a pickaxe now "rewards" the player a new advancement (hinting that using a wrench is preferable)
  * Pickaxing pressurized machines now causes particles and a sound to played, indicating that air has been lost
* Thermopneumatic Processing Plant now consumes air and/or heat as it runs, not once when the current recipe run has completed
  * Overall air/heat usage has not been changed 
* In the Programmer GUI, clicking with an item (other than GPS tools) on the background area now creates an Item Filter widget for that item
  * Clicking an existing Item Filter widget with an item updates the item for that widget
* All numeric-entry textfields in all GUI's can now be modified by scrolling the mouse wheel
  * Hold Shift while scrolling for a faster adjustment
  * The actual modification depends on the context but should make sense for each situation
* Drones will now repair their held item with imported experience orbs if the item is enchanted with Mending
  * Reminder: use an Import Entity widget with an `@orb` filter to import experience orbs (excess Memory Essence fluid can be put in a tank with the Export Fluid widget)
* Changes to rendered fluid levels in fluid-containing tile entities are now interpolated smoothly on the client (for a nicer visual appearance)

### Fixes
* Fixed Blood Magic Lava Crystals (and other container-like items) being consumed by the (Advanced) Air Compressor
  * Note that Lava Buckets are still not accepted by Air Compressors (use Liquid Compressors for lava fuel)
* Fixed north & west temperature widgets being swapped in the Thermal Compressor GUI
* Fixed Amadron player-player trading not paying the seller
* Fixed NPE when placing down Pressure Chamber blocks with a Building Gadget
  * Fix actually applies to any situation where multiple PNC tile entities are placed in a single tick
* Fixed fluid-containing tile entities not rendering their fluid when any GUI is open  
* Placing an item on a Display Table in creative mode no longer removes the item from the player

## 2.11.4-170 (31 Mar 2021)

### Fixes
* Fix infinite recursion crash when breaking Refinery Output blocks
* Fix potion-crafted ammo causing player kick on dedicated server

## 2.11.3-168 (26 Mar 2021)

### Fixes
* Hotfix: drone pathfinding NPE introduced in 2.11.2

## 2.11.2-167 (26 Mar 2021)

### Updates
* Memory Essence fluid is now tagged as `forge:experience` for more cross-mod compatibility
  * Vacuum Trap will now accept any fluids with this tag
* The max teleport distance for Drones is now configurable
  * Set `max_drone_teleport_range` in `config/pneumaticcraft-common.toml`
  * Default is 0, meaning unlimited range - same as previous behaviour
  * Amadrones and Programmer Controller are not affected by this
  * Primarily intended to avoid abuse on PvP servers, but may have other applications
  * Be careful about setting this to very low values; it could mess up drone programs where the drone can't always find a path
* Added another JEI entry for Memory Essence clarifying that Drones with an Entity Import widget can import XP orbs -> Memory Essence

### Fixes
* Fixed Smart Chest in push mode sometimes getting wedged and not pushing items even when it can
* Fixed (cosmetic) face culling issue for Thermopneumatic Processing Plant.  Also gave it a better block shape (matches model more nicely now).
* Fixed the new Huge Tank not accepting any upgrades
* Fixed rebuilding a Pressure Chamber (which already had some pressure) sometimes getting the new pressure very wrong
* Some more Elevator work: hoping once again that the player-falling-off bug is *really* fixed (looking good so far, but needs testing and feedback please!)
* Fixed Air Grate range preview not being removed if the Air Grate module is removed
  * Also, Air Grates no longer show their range when initially placed (use the GUI to toggle range preview)

## 2.11.1-163 (22 Mar 2021)

### Updates
* API note: the PneumaticCraft API is now licensed under the LGPLv3 (previous GPLv3)
  * Intention is to allow any mod to link against the API and not worry about needing to license under the GPL. All open source mods welcome!
* Charging Station now gets a boost in charging speed when pressure > 15 bar
  * Up to 2x faster at 20 bar
  * This is over and above any Speed Upgrade speed boosts
* Slight change in Pneumatic Armor behaviour: all upgrade features now require a minimum of 0.1 bar pressure to operate
  * Armor itself still provides protection when out of pressure
* The methods of obtaining Memory Essence (Memory Stick / Aerial Interface) are now shown in JEI  
* The Redstone Module can now act like a vanilla Comparator when in input mode, measuring the contents of the inventory it faces
  * Advanced PCB required for this; select "Comparator Mode" via the module GUI

### Fixes
* Fixed startup crash when CoFH Core (Thermal series) 1.2.0 is installed
  * Will start up when older CoFH Core (1.1.6), but 1.2.0 is required for the Holding enchantment to boost the volume of pressurizable items
* Fixed not being able to toggle off Pneumatic Armor upgrades when out of air
  * Can now toggle upgrades off (but not back on) when the armor piece has insufficient pressure
  * Fixes problem of running out of air with Jet Boots and then being unable to jump without taking boots off
* Fixed some messy text formatting in Universal Sensor GUI, sensor info tab
* Fixed stacked Air Canisters acting as an infinite air source under some circumstances
  * Air Canisters may now only stack if completely empty (no NBT), for ease of use as a crafting component
* Fixed Refinery GUI temperature tooltip showing nonsense ranges when there's no oil in the Refinery
* Fixed neighbouring Pressure Tube block shapes not getting updated client-side when a tube explodes due to overpressure
* Fixed Electrostatic Compressor not always finding all connected compressors if the grid is large

## 2.11.0-155 (17 Mar 2021)

### New
* The Programmable Controller now has some optional chunkloading functionality
  * You can choose in the GUI whether to load the PC's own chunk and/or the current working chunk (where the minidrone is) and/or a 3x3 area around the working chunk
  * This comes with an extra air cost if enabled: 10 mL/t to load itself, and another 10mL/t to load the working area, or 30mL/t to load a 3x3 area
* Added Jet Boots Flight Stabilizers to bring you to an instant halt when releasing the thrust key
  * With Jet Boots Tier IV or Tier V, the Jet Boots armor GUI gets a checkbox to toggle Flight Stabilizers
  * Can also be bound to a hotkey for quick enabling/disabling
  * No extra upgrade needed, just Jet Boots Upgrade Tier IV or V
* Added Radiation Shielding Upgrade for Pneumatic Armor to protect against Mekanism radiation
  * Needs a Mekanism Radiation Shielding Unit to craft
  * One upgrade is needed for each armor piece for effective protection (you need all 4 to be safe)
  * This protection may extend to other mods which add radiation mechanics in future as they become available on 1.16
  * Note: protects against initial irradiation; if you're already irradiated, this won't help!
* Added JEI support for displaying the PneumaticCraft heat properties of any blocks/fluids which have been assigned custom heat properties
  * Displays temperature and thermal resistance for all blocks & fluids
  * For blocks which have a transition (e.g. Lava -> Netherrack) also shows the hot/cold transitions, and the heat capacity of the block/fluids
* As part of the above change, all custom heat properties are now added via the vanilla recipe system
  * This means that data files under `data/pneumaticcraft/pneumaticraft/block_heat_properties` have moved to `data/pneumaticcraft/recipes/block_heat_properties`
  * The JSON files themselves have the same format as before, with the addition of a mandatory `"type": "pneumaticcraft:heat_properties"` field
* Added custom heat properties for a bunch of new modded blocks (thanks @MuteTiefling)
  * Better End (Forge): all the Emerald Ice blocks
  * Biomes You'll Go: Boric/Cryptic Campfires, Cryptic Magma, Magmatic Stone
  * Create: Blaze Burner (with different properties based on the Burner state: smouldering/kindled/seething/fading)
  * Decorative Blocks: Brazier & Soul Brazier
  * Minecraft: Soul Campfire
  * Occultism: Spirit Fire
  * Valhelsia Structures: Brazier
* All pressurizable items can now take the CoFH Holding enchantment to increase their air storage (over & above what Volume Upgrades add)
  * Each level of Holding acts as a straight volume multiplier: Holding I = 2x, Holding II = 3x, etc.
  * This only applies to items/tools, not pneumatic machines when in item form
  * Also works for Drones, which will remember the enchantment when switched between item and entity form
  
### Updates
* Drones which get stuck on a block (even when they are able to pathfind) will now teleport if stuck for more than 20 ticks
  * Note that such blocks are most commonly non-full-cube blocks with a movement-blocking hitbox added by mods which have forgotten to mark those blocks as non-pathable
  * If you see cases of drones getting stuck & teleporting around particular blocks, please raise a github issue so I can investigate
  * Stuck teleport timeout is configurable: see `Advanced` -> `stuck_drone_teleport_ticks` in `pneumaticcraft-common.toml`; if you set it to 0, teleportation is disabled, and drones will mostly likely sit at the stuck position indefinitely (as in previous versions)
* GUIs for Thermopneumatic Processing Plant, Fluid Mixer and Refinery now show the current recipe in the JEI recipe click area
  * If the machine has a current recipe based on its input items/fluids, then the planned output(s) will be shown in the area tooltip
  * If extended information is enabled (F3+H), the internal recipe ID will also be shown
* All PneumaticCraft Drones are now immune to Mekanism radiation
* Custom block heat properties now support multiple entries for the same block
  * Entries must be distinguished by blockstate predicates.  See the `create/blaze_burner_*.json` entries for examples.
* Disabled Aerial Interface / Curios inventory insertion and extraction
  * I've had reports of item duplication bugs, although I can't reproduce them myself
  * Other Curios integration (Aerial Interface FE-charging items in Curios slots and Memory Stick as a Curio) continue to work as before
  
### Fixes
* Fix Thermopneumatic Processing Plant choosing the wrong recipe under certain circumstances
  * Was an issue with similar recipes (e.g. Potato -> Vegetable Oil & Potato/Yeast -> Ethanol), exposed when extra recipes have been added
* Hopefully fix problems with players falling off/through Elevators which have a lot of speed upgrades (6+)
* Fix Air Cannon causing server crashes when launching TNT near a Security Station
* Fixed client crash when players hit drones (bug introduced in 2.10.2 related to drones being immune to their own area attacks)
* Fixed a server crash related to capturing Drones in a Mob Imprisonment Tool (Industrial Foregoing) and then releasing them

# Minecraft 1.16.3 / 1.16.4 / 1.16.5

## 2.10.3-149 (6 Mar 2021)

### Fixes
* Fix Pneumatic Armor bug where armor couldn't be switched on from cold (Core Components toggle button)
* Fix timing issue on SMP where Pneumatic Armor feature enablement packets sometimes got ignored by server

## 2.10.2-148 (4 Mar 2021)

### Updates
* Added a 4th tier of fluid tank: the Huge Fluid Tank, with 512000mB of storage (and an expensive recipe...)
* The Minigun's selected ammo slot can now be cycled with Sneak + Scroll Wheel (in addition to via the magazine GUI). 
  * Useful if you have multiple ammo types in the gun.
* The Minigun can now accept Volume Upgrades
* Drones will no longer injure or kill themselves with area attacks when using weapons like the Infinity Hammer from Industrial Foregoing
  * Technical info: drones are now immune to any damage from their own fake player object
* Updated the Jetboots looping sound effect
  * New effect is a lower-pitched rumble, (hopefully) much easier on the ears
* Aerial Interface now plays a "scuba" sound effect to the player when it supplies them with air underwater
* Added the ability to blacklist items from the Pressure Chamber disenchanting system
  * See new config setting `disenchanting_blacklist` in the Machines category of `pneumaticcraft-common.toml`
  * By default, Quark Ancient Tomes and all items from the Tetra mod are blacklisted; both use enchanting systems which allow enchantment duping in the Pressure Chamber since their enchantments can't be stripped by normal (vanilla) means
* Display Tables (and Tag Workbenches) will now form a visual "multiblock" if placed adjacent to each other and rotated in the same direction
  * Two adjacent tables with the same orientation will have their facing table legs hidden
* Mekanism Paxels may now be used as hoes (for replanting purposes) by Harvesting Drones (and the Harvest progwidget in general)
* Reinforced and Smart Chests now support loot tables (via `/setblock`) using the `LootTable` and `LootTableSeed` NBT tags, same as vanilla Chests do

### Fixes
* Fixed Minigun not hurting the Ender Dragon
* Fixed Minigun Freeze ammo not forming ice blocks on the surface of water
* Fixed crash when opening Minigun magazine inventory from the off-hand
* Fixed Amadron GUI bug where clicking fluids in the GUI left the tablet unusable until the player re-logs or does a `/reload`
* Fixed server NPE crash relating to heat capability discovery on the Thermal Compressor
* Fixed potential client crash if editing an Aphorism Tile when something (e.g. Drone) updates it server-side
* Fixed client (render) crash for blocks thrown by the Pneumatic Chestplate Launcher (Chestplate & Dispenser Upgrade)
* Fixed Tube Module bounding box targeting being a bit off on the Y axis when sneaking and on dedicated server
* Made Redstone Tube Module bounding box a bit larger, properly matching the model size

## 2.10.1-140 (23 Feb 2021)

### Updates
* Pressure Chamber disenchanting now also works for Enchanted Books
  * The Enchanted Book to remove enchantments from must have more than one enchantment
  * A random enchantment will be removed and added to a regular Book
  * Thanks to @BlueAgent for this
* COFH/Thermal fuels (Refined Fuel, Tree Oil & Creosote) are now supported in the Liquid Compressor & Advanced Liquid Compressor
  * Note that COFH Crude Oil is still not accepted by the Refinery by default, because it is not added to the `forge:crude_oil` fluid tag
* Pneumatic Door Base now has a GUI option to open its attached Pneumatic Door when it (the base) receives a redstone signal
  * Previously the Pneumatic Door itself required a redstone signal to open
  * Only applies in Wooden Door or Iron Door mode, of course
* Notify players who are holding an Amadron Tablet when new offers become available
  * Can be disabled by clientside config setting: `notify_amadron_offer_updates` in `pneumaticcraft-client.toml`
* The Seismic Sensor can now be configured to search for underground fluids other than PneumaticCraft Crude Oil
  * By default it will now find any fluid tagged as `forge:crude_oil` (which includes PneumaticCraft Crude Oil)
  * See the `seismic_sensor_fluid_tags` and `seismic_sensor_fluids` config settings in `pneumaticcraft-common.toml`; these take a list of fluid tags and a list of fluid ID's, respectively
  * Prefer `seismic_sensor_fluid_tags` where possible to add fluids by tag (e.g. `minecraft:water` or `minecraft:lava`); use `seismic_sensor_fluids` when you don't have a fluid tag: this takes a direct fluid ID
  * Both config settings can be used together; the permitted fluids will be combined as a set union
* Vacuum Trap GUI now has a 'R' range preview button to show mob absorption range

### Fixes
* Fixed heat handling tile entities losing their heat information when moved by Quark (and possibly other mods like Create)
* Fixed a problem with Drones not always being able to place fluids in-world (in particular where waterlogged replaceable blocks like Sea Grass existed)
* Fixed Pneumatic Armor GUI being able to move stat panels completely off-screen (by switching opening direction when panel is on the edge)
* Fixed Security Station failing to prevent block breaking by certain fake players (not including Drones)
* Fixed problem where Drones wouldn't always move into fluids even with a Security Upgrade equipped
  * Drones will now move into any cool fluid, but not fluids with a temperature of over 373K (100C)
  * Note the Programmable Controller fake drone doesn't care about temperatures and will happily move through anything
* Clarified docs for Right Click Widget when in Item Mode: the filter applies to the held item, not the clicked block
  * Note that this widget can't filter by clicked block when in Item Mode; this is likely to be remedied in a later release, but will probably require a separate progwidget
* Pneumatic Chestplate Security Upgrade will no longer attempt to extinguish fires or solidify lava when player is in creative or spectator mode

## 2.10.0-134 (6 Feb 2021)

### New
* Pneumatic Armor has had a little re-texture (no fancy models, sorry!)
  * It is now possible to dynamically re-colour primary and secondary colours for each armor piece, independently
  * Eyepiece can also be independently coloured, and this colour is also used to recolour the HUD overlay panels from the default green  
  * Access this through the armor GUI, "Core Components" page, new "Colors..." button
  * Builtin behaviour, no special upgrades needed for this
  * Also added a config option (accessible via the Colors gui) to disable showing the enchantment glint for enchanted Pneumatic Armor pieces
  * Only applies to Pneumatic Armor, not other armor pieces; use this if you don't want the glint to overwhelm your carefully-chosen colour scheme...
* Added Compressed Iron armor set - early game armor which is a little better than Iron armor
  * Same protection as Iron, but higher durability, toughness, and adds a little knockback resistance
  * Made with Compressed Iron Ingots and Leather armor pieces
  * Pneumatic Armor recipes now require the corresponding Compressed Iron Armor piece instead of Leather Armor piece
* Added Russian (ru_ru) translations.  Thanks to @shikhtv for these.

### Updates
* Air Grate needs a little less pressure to fan Heat Sinks now.
  * 0.6 bar is sufficient to fan Heat Sinks two blocks away (i.e. one block of space between Air Grate and Heat Sinks)
  * Air Grate range display is now handled via "R" range button in GUI instead of right-clicking the module
* When Amadron restocks a player offer that you created, it now will inform you of the fact, and how many of that offer you now have in stock
* Amadron stock levels are now reduced as soon as an order is placed (for orders with a max stock level)
  * Previously, stock levels weren't updated until the trade was complete
  * This could lead to orders being lost if multiple players placed an order at the same time, or one player placed multiple orders in rapid succession
  * Reduced stock levels are restored if the order fails for any reason (e.g. player places order then removes payment before Amadrone can collect it)
* Reduced the max stock for some high-value PneumaticCraft Villager trades (affects both direct villager trading and Amadron)
* Drone and Network API items which have a program saved on them no longer despawn when in item entity form in the world
  * Avoids loss of potentially valuable Drones with big programs on them
* The Programmable Controller is now allowed to use the Suicide progwidget in its programs
  * This ejects the Network API or Drone item from its programmable slot into an adjacent chest or (failing that) into the world as an item, terminating the program
  * Chests will be checked for on any face of the Programmable Controller marked as "Programmable Slot" in the side configuration tab

### Fixes
* Fixed drone Place Block progwidget sometimes placing blocks outside the specified area
* Fixed drone Block Right Click progwidget not honouring the "Side" setting
  * This is noticeable e.g. if you use the drone to sneak-right-click a Sign or Aphorism Tile onto the side of block
* Fixed drone's held item not syncing to the client when it becomes empty
* Fixed drone block placement not using air as it should (100mL per block placed)
* Fixed potential item dupe issue in Smart Chest with Magnet upgrade
  * Smart Chest magnet functionality now also respects Botania's Solegnolia and items on conveyor belts (e.g. Immersive Engineering)
* Fixed larger (4x4x4 and 5x5x5) pressure chambers losing almost all their pressure if briefly broken and reformed
  * A little pressure is still lost due to normal leakage, but reforming the chamber quickly now preserves most of the pressure
* Aerial Interface and Omnidirectional Hoppers with Entity Tracker now respect items with Curse of Binding enchant on them
  * No more easy removal of cursed items, sorry!
* Fixed potential Amadron exploit by badly-behaved clients being able to order more of an item than currently in stock

## 2.9.5-128 (26 Jan 2021)

### Fixes
* Hotfix: fixed crash on dedicated server with GPS and GPS Area Tools due to bug introduced in 2.9.4

## 2.9.4-126 (26 Jan 2021)

### Updates
* Harvesting Drones (and the Harvesting progwidget) now know about Sweet Berries and Kelp
  * For Sweet Berry Harvesting Drones, Item Life upgrades are strongly recommended due to the thorny nature of the bushes
  * For Kelp Harvesting Drones, one (and only one) Security Upgrade is vital for the Drone to operate underwater
  * Same applies to any Collector Drones you might use in conjunction with Harvesting Drones  
  * Note: avoid using Lily Pads around Drones which need to fly in and out of water; it confuses their pathfinding badly
* Poisonous Potatoes and Sweet Berries can now be used in the Thermopneumatic Processing Plant to make Ethanol
* Drone debugger now highlights the planned path for a debugged Drone when it's moving, as a trail of particles
  * Can be disabled in config: `drone_debugger_path_particles` in pneumaticcraft-common.toml
* Amadron Tablet: the Order button now shows a summary of items/fluids in basket in its tooltip
* Reworked Jetboots flying animations/player pose a bit
  * Player now animates into & out of flying pose much more smoothly
  * Player has better vertical positioning and hitbox when flying (now possible, if not necessarily easy, to fly through 1-block high gaps)
  * Most noticeable in multiplayer, or singleplayer with external camera view
* GPS and GPS Area Tools tooltip now show the blockname(s) for the block(s) at the selected position(s)
  * Only if those positions are actually chunkloaded, and remember that the GPS/GPS Area Tools aren't dimension-aware, so it's the block in the current world, not the world in which the tool was originally clicked
* New food item: Salmon Tempura
  * Equivalent in food value to Cod n' Chips (i.e. very nutritious)
  * A good alternative if you're in an area with abundant Salmon but no Cod...
  * See JEI for how to make it!

### Fixes
* Fixed Block Tracker block highlight frames sometimes not rendering behind solid blocks
* Fixed Pneumatic Armor pressure display panel not fully opening when Pneumatic Helmet first equipped
* Fixed shaped recipe for Assembly IO Import Unit producing an Assembly IO Export Unit
* Fix enchantments getting lost when using Pressure Chamber to transfer enchantments from Enchanted Book to enchantable item
  * If the book had multiple enchantments, some of which were applicable to the item, and some not, the inapplicable enchantments would be lost forever
  * Now any such enchantments stay on the book
  * Also fixed Pressure Chamber enchanting being able to add conflicting enchantments to an item (e.g. both Sharpness and Smite)
* Fixed Amadron allowing orders too big for a single Amadrone to carry (>36 itemstacks or >576 buckets of fluid)
  * GUI now shows an error if you try to order too much at once; split your order into multiple orders
* Fixed Amadron tablet not working with Mekanism (and possibly other) fluid tanks
  * Technical detail: no longer checks for a fluid or item capability on the `null` face, but instead tries each face of the block to find a valid fluid or item capability
* Fixed Amadron "Add Trade" sub-gui only being openable once (until main Amadron GUI is closed)
* Fixed intermittent Aerial Interface CME crashes when changing dimension
  * Aerial Interface player tracking should be much more reliable overall now
* Fixed Nether Portal screen overlay (when player is standing in portal) rendering completely solid when Pneumatic Helmet equipped, effectively blinding the player  
* Fixed Pneumatic Armor initialisation progress bars not going away (and "low pressure" message spam) if player wearing any armor pieces with 0 pressure
* Fixed (hopefully) client crash when toggling Redstone Module input/output mode from GUI, when using Vivecraft (VR)

## 2.9.3-120 (17 Jan 2020)

### Updates
* Added Reinforced and Smart Chest Upgrade Kit items
  * Use these convenience items to upgrade a wooden chest to a Reinforced or Smart Chest, or a Reinforced Chest to Smart Chest
  * Operates in-place, no need to remove and re-add any items in the chest
* When extended tooltips are active (use F3+H), the Programmer GUI now shows the progwidget ID for programming widgets in their tooltips
  * This is useful if you plan to do any Drone programming with Computercraft and the Drone Interface
  * The progwidget ID corresponds directly to the action used in the Drone Interface's Lua setAction() method (the "pneumaticcraft:" prefix can be omitted)
* Added two new Lua methods to the Drone Interface
  * setCanSteal() - used by the "pickup_item" action, controls if the Drone may steal items off e.g. Immersive Engineering conveyors
  * setRightClickType() - used by the "block_right_click" action, controls if the Drone should be using an item or activating a block
* Jet Boots and Elytra now play more nicely together
  * When player is Elytra-flying, switched-on Jet Boots won't fire, or use any air, unless player is actively thrusting (holding down Space bar)
  * Allows players to glide with Elytra and fire Jet Boots for occasional thrust or altitude boost, a nice air saver
  * Jet Boots HUD now shows an informational Elytra icon if player is Elytra-flying
* Item Filter progwidget GUI improvements
  * Add radio boxes to choose filtering by item (the default) or by variable
  * Cleaner GUI layout: only show GUI controls relevant to the current mode (item or variable)

### Fixes
* Fixed PneumaticCraft blocks not remembering custom names assigned in an Anvil when the block is placed down & broken again
* Fixed Pressure Tube shape not always updating properly after a tube module is added or removed
* Fixed Redstone Module clock mode tick lengths > 127 not working
* Fixed Computer Control (Drone Interface) not working with several programming widgets (Block Right Click, Pickup Items, Place, Void Item, Void Liquid) 
  * Also cleaned up the Patchouli manual entry for the Drone Interface quite a bit
* Fixed Block Tracker causing client crash when tracking vanilla mob spawners with an Enderman (or any angerable creature) spawn egg loaded
  * Caused by https://bugs.mojang.com/browse/MC-189565 but I've added some extra validation to prevent an outright crash
* Hopefully fix worldgen-related crash caused by other worldgen intersecting PneumaticCraft villager houses
  * Couldn't reproduce myself, but some extra validation has been added
* Fixed logic error in Drone Dig widget item filtering causing blacklisted items to be ignored
* Fixed Programmer GUI inventory slots getting messed up after closing an item or inventory search window
  * e.g. right click an Item Filter widget then press "Search Items...", then go back to the main Programmer GUI
* Fixed Item Filter progwidget GUI not properly sync'ing variable names when GUI closed

## 2.9.2-116 (13 Jan 2021)

### Updates
* Drones (and Guard Drones, but not other basic drones) can now take up to 15 Armor Upgrades
  * Each Armor Upgrade gives the Drone one point of armor, so 15 is equivalent to a full suit of Iron armor
  * However, any Armor Upgrades over 6 will apply a small movement speed penalty to the Drone, so you will need to balance protection vs. movement

### Fixes
* Fixed changes to pressure levels in Pressure Gauge Module GUI not being properly sync'd to server
* Fixed mobs not properly targeting Drones for retaliation when attacked by them
* Fixed Drone's targeting laser rendering too low on the Y axis
* Fixed Vacuum Pump GUI showing wrong vacuum pressure when Volume Upgrades are installed (display bug only: actual pressure was OK)
* Fixed Liquid Hopper ignoring "leave 1000mB" mode when ejecting fluid via Dispenser Upgrade

## 2.9.1-115 (11 Jan 2021)

**IMPORTANT**: this release requires Forge 35.1.34 or later!

### Updates
* Security Station hacking is now back in game for the first time since 1.12.2!
  * Nuke Viruses now have a 3 second cooldown when used
  * General security station GUI cleanup
  * Entity Tracker upgrades now have diminishing returns in the Security Station for intrusion detection
  * Up to 12 Entity Tracker upgrades can be installed for 99% detection chance per node hacked
  * The Security Station no longer informs potential hackers how many Security Upgrades they need
  * The Security Station now deals a small amount of non-resistible damage to potential hackers who lack the correct number of Security Upgrades
  * Nuke Viruses and STOP! Worms are now level 5 villager trades, with fewer available, and cost more than they used to (they were too easily obtained before)
* Amadron offers can now have a maximum stock, i.e. the maximum number of times an offer can be bought before it becomes unavailable
  * This is shown as a blue number on the offer in the Amadron Tablet GUI (via the same mechanism that player->player trades use)
  * Random villager trades are now all limited by this maximum (to the same number they would be if you bought directly from the villager)
  * All core Amadron trades (e.g. PCB Blueprint or Emeralds to Lubricant) remain unlimited and can be bought as many times as you want
  * Limited trades are refreshed once per Minecraft day (when trades are reshuffled)
  * Custom trades (loaded from datapack) can take advantage of this with the "maxStock" JSON field. Set to -1 or omit entirely for unlimited trades.

### Fixes
* Pneumatic Kick upgrade in the Pneumatic Boots now works on players too. Have fun...
* Drone fake players now use the owner's UUID and name again, which should resolve a lot of issues with protection mods such as FTB Chunks (and the Security Station) preventing drones from operating in the protected area
* Fixed some non-fatal errors in the startup log related to Mekanism fuel entries, when Mekanism isn't installed
* Fixed global variables not being properly sync'd and updated for GPS Tool & GPS Area Tools
* Cull area rendering for GPS Area Tools with very huge areas (>10000 blockpos), to prevent killing client framerate
* Fixed Jet Boots HUD always showing 0 speed for the player
* Fixed client crash when trying to open "Condition: Item Filter" widget manual entry from the Programmer GUI
* Amadrones will now try all sides of the target inventory/tank when delivering goods, not just the top
  * Prevents loss of goods (fluids especially) if there's any block on top the target inventory/tank

## 2.9.0-111 (4 Jan 2021)

### New
* It is now possible to debug Programmable Controller drone programs in the same way that regular Drones can be debugged
  * Pneumatic Helmet with Entity Tracker and Dispenser Upgrades: target the Programmable Controller minidrone and press the debug hotkey (default: Y) 

### Updates
* Quality of Life: when attaching a tube module to a Pressure Tube, you can now sneak to attach to the opposite side of the tube
* Updated some Patchouli manual information, primarily to clarify drone auto-charging functionality
* Logistics Drones can now take Inventory Upgrades (this was actually already documented in the manual)
* Liquid Import progwidget can now have a definable block sort order (like dig/place widgets)
  * Previously always just used "closest" sort order; "top down" often makes more sense for importing fluids from the world
* Drone search area highlighting (when Entity Tracker enabled and Dispenser Upgrade inserted) now only displays when player is actually debugging the drone
  * Particles are now also colour-coded: brown for dig/place, blue for fluid import, etc.
  * Also greatly reduced the network packet size for sending these particles: less server->client chatter
* The Programmable Controller's "minidrone" is now able to path into lava
  * It was always able to path *through* lava blocks on the way to somewhere else, but can now path *into* them too
  * This is particularly useful for writing programs to suck up lava lakes from the world which were previously problematic to work with
  * Note that actual Drones still won't path into lava, only the Programmable Controller

### Fixes
* Fixed client crash on player login when ModernUI is installed
* Fixed Pneumatic Armor not reporting feature on/off status properly to server when armor newly equipped
  * Logging in with armor already equipped worked fine, which is why it took some time to spot this bug...
* Reset step assist height when Pneumatic Boots are removed
* Fixed certain blocks being wrongly ignored by drone's Dig progwidgets when "Requires Tool" is checked
  * Blocks which are harvestable with a bare hand (e.g. gravel, glowstone...) were being skipped when the drone was carrying a tool which wasn't faster than a bare hand for those blocks
* Fixed the various PneumaticCraft Reinforced Stone blocks being harvestable without a pickaxe
* Fixed hacking drones to call them to you not functioning
* Fixed drones not highlighting their block search area when Entity Tracker enabled and Dispenser Upgrade inserted
* Fixed bug which could sometimes cause the Chestplate Charging Upgrade to void air
* Fixed client crash when removing an Elevator Base with Elevator Frames above
* Fixed Programmable Controller fluid tank ignoring inventory upgrades on world reload
  * Each inventory upgrade now increases the tank's storage by 16000mB over the base 16000mB
* Fixed block shape inconsistency between basic and Advanced Liquid Compressors
  * Was possible to put redstone on top of a basic Liquid Compressor but not the Advanced version
* When pastebin-importing old drone programs from Minecraft 1.12.2 and earlier, ignore item meta being enabled on item filter widgets for items that don't have durability/metadata anymore

## 2.8.2-97 (28 Dec 2020)

### Updates
* The Coordinate Operator progwidget can now operate upon a specific subset of the X/Y/Z coordinate fields if desired
  * Choose which fields should be affected in the widget's GUI
  * Default is all of X/Y/Z, i.e. same behaviour as previously
* Programmable Controllers can no longer mine themselves up via the Dig progwidget
* Smart Chest filtering: Alt + Left Click with an item on the cursor now sets that item as a filter if possible
  * This can also be done on "closed" slots in the chest's GUI
  * Useful to be able to update a filter without needing to open up a closed slot (potentially allowing unwanted items in)
* Added Void Fluid progwidget, counterpart to the Void Item widget added back in 2.4.5
* The Programmable Controller can now charge the minidrone's held item (pressure or RF) significantly faster than before
  * This makes Jackhammers in vein+ mode highly suitable for use in a very fast quarry...
* It is now possible to configure the likelihood of Oil Lakes generating at the surface
  * See pneumaticcraft-common.toml -> General -> `surface_oil_generation_chance`
  * This is 25% by default (i.e. if an Oil Lake would generate at the surface, there is only a 25% chance that it will actually generate)
  * Can be set to 0 to disable surface Oil Lakes entirely
  * Note that raising/lowering this value also raises/lowers the overall number of Oil Lakes generated, so you may also wish to modify `oil_generation_chance` if you modify this setting and want to keep a similar number of lakes overall.

### Fixes
* Fixed Liquid Import progwidget losing any fluid it imported from a fluid block in the world
* Fixed oil generation being allowed in Nether Wastes biome
  * This should have been blacklisted before but was missed; oil should only appear in Basalt Deltas in the Nether by default
  * Config is not retroactively updated; add "minecraft:nether_wastes" to pneumaticcraft-common.toml -> General -> `oil_worldgen_blacklist` if you want to apply this change to an existing world.
* Memory Sticks are now unstackable
* Hopefully fixed problem where Memory Stick XP auto-absorb randomly stopped working
* Fixed higher-tier Mekanism pipes issue when extracting from UV Lightbox

## 2.8.1-96 (19 Dec 2020)

### Updates
* Further performance work on Pressure Tube shape calculation, with more aggressive caching of block shapes
  * Tests indicate a significant reduction in client-side CPU time used for Pressure Tube shape calculation
* Made 3D (in-world) pressure gauge rendering more CPU-friendly

### Fixes
* Fixed client crash on login when Minecolonies is installed
* Fixed toggling of Pneumatic Armor "Core Components" (the master switch) leaving some upgrades in an inconsistent state

## 2.8.0-95 (14 Dec 2020)

### Updates
* Aphorism Tiles have had a major boost in functionality
  * The editor is much more powerful now (supports horizontal cursor positioning, joining/splitting lines & more...)
  * The Aphorism Tile can now display one or more items instead of, or in addition to, text lines
  * Use `{item:<item-id>}`" on a line on its own to show an item, or use the friendly GUI item searcher to add an item
  * The string `{redstone}` anywhere on a line will be substituted with the redstone signal level for the block behind the tile
    * There's also a GUI button to insert this string
  * Tile margin width can be adjusted now, via GUI slider
  * The tile can be now made invisible via GUI checkbox, showing only the text and/or items on the tile
* Elevator pressure mechanics (for stacked elevators specifically) have been reworked
  * All lower Elevator Bases in a stack now have their own air storage, unlike previous versions where they just "proxied" the top elevator in the stack
  * The old system never worked well and was just too bug-prone
  * If you have stacked elevators, you will likely notice a one-time pressure drop after upgrading to this release, due to the extra air storage
  * On the plus side, you now effectively have a bunch of free volume upgrades!  Pressure will drop more slowly as air is used.
  * Also, raised the default elevator max height from 4 blocks per elevator base to 6 blocks per base
    * If you want to take advantage of this in an existing world, edit pneumaticcraft-common.toml -> Machines -> `elevator_base_blocks_per_base`
* All fuels (as used by the Liquid Compressors & Kerosene Lamp) are now defined in datapacks as a special recipe type "pneumaticcraft:fuel_quality"
  * This makes it very easy to override existing values or add new custom fuels
  * See https://github.com/TeamPneumatic/pnc-repressurized/tree/1.16.4/src/generated/resources/data/pneumaticcraft/recipes/pneumaticcraft_fuels/ for the default fuel JSONs
* Performance improvement for Pressure Tubes: made block shape calculation much more efficient
  * May help with FPS drops in chunks with a lot of tubes, and where a lot of block updates are happening
* Pneumatic Doors now have an "Iron Door" mode in addition to the existing 3 modes
  * Like vanilla Iron Doors, these will *only* open on a redstone signal
* Infinite water source blocks are no longer block-updated when absorbed by Drones/Gas Lifts/Liquid Hoppers
  * This intended primarily for performance (eliminating frequent block updates)
  * Can be disabled in config (pneumaticcraft-common.toml -> Advanced -> `dont_update_infinite_water_sources`) if you want to enforce no-infinite-water mechanics in your world
* Area preview has been reworked for several machines:
  * Pressurized Spawner, Security Station, Universal Sensor now have a common "R" GUI button (in bottom right of GUI) to toggle range preview
  * This is now a toggle instead of displaying for a few seconds and disappearing
  * Uses similar preview style (and same code internally) as GPS Tool / GPS Area Tool area previews
  * Air Grate Module also uses this new functionality, but right-click it with empty hand to toggle range preview
* Condition widgets can now record their last measured value in a definable variable
  * Use the Condition widget GUI's to define a "Measure" variable name
  * If defined, the measured value will be stored in that variable each time the condition executes
  * When a measure variable is defined, it's no longer an error to have no branch Text widgets attached to the Condition widget (i.e. the widget can be used purely for measurement, if desired)
* Drones will now no longer travel (fly or teleport) more than 80 blocks to a Charging Station when low on air
  * 80-block limit can be adjusted in config: pneumaticcraft-common.toml -> Advanced -> `max_drone_charging_station_search_range`
  * Note that very large limits can in theory make drones teleport thousands of blocks away to someone else's base if your own charging stations are unavailable for any reason
* Some Pneumatic Armor keybinding improvements
  * It's now possible to bind mouse buttons to toggle armor upgrades as well as keyboard keys
  * Fixed integration with vanilla Options -> Controls screen (changing a binding via armor GUI properly reflects in Controls screen and vice versa)
  * Options -> Controls screen divides keybinds into 3 categories: main PneumaticCraft keys, Pneumatic Armor toggles, and Block Tracker submodule toggles
* Made Pneumatic Kick upgrade more effective
  * Increased entity detection area around player's foot a bit (easier to target entities)
  * Reduced air usage somewhat, especially when 4 Dispenser Upgrades installed
  * Increased vertical velocity of kicked target (i.e. better at launching targets into the air!)
  
### Fixes
* Fixed elevator callers not emitting redstone when the elevator is at that floor
* Fixed elevator Charging Upgrades not being sync'd to client (on world reload)
  * This caused client & server to disagree on elevator descent rate, leading to jerky descent
* Fixed drone not being able to fill bottles with water from water source blocks
  * Note that filling water bottles from water tanks is still not possible (with or without a drone) - this is intended behaviour
* Fixed Air Grate entity filter setting (with Advanced PCB installed) not taking effect right away
* Fixed Security Station GUI rendering lines wrongly and eventually starving the client of memory
* Fixed camouflaged Elevators rendering unlit after a neighbour block update
* Fixed Transfer Gadgets not being placeable on a block if a Transfer Gadget already existed on the top face
* Fixed JEI init bug (causing no PNC recipes to show) if `explosion_crafting` is disabled in config
* Fixed Sentry Turret minigun firing sound not playing to clients
* Fixed Charging Station not extracting last tiny bit of air from items when discharging them
  * Related: clean up item NBT when all air has been extracted from an item (don't store air=0 amounts in NBT); mainly for benefit of stacking Air Canisters
* Fixed distance-sorting bug when drone searches for closest Charging Station for recharge
  * This could have caused the drone to fly (or teleport) to an unexpected Charging Station

## 2.7.2-86 (3 Dec 2020)

### Fixes
* Hotfix: avoid infinite recursion crash when Quark is also installed

## 2.7.1-84 (3 Dec 2020)

### Updates
* Omnidirectional and Liquid Hoppers can now take an Entity Tracker Upgrade
  * This allows them to transfer items and fluids to/from entities in front of the hopper's input or output
  * Tested successfully on: players, Drones, Chest Minecarts and Immersive Engineering's Barrel Minecarts
  * Should work on any entity which provides the appropriate item or fluid capability
  * For players, a horizontally aligned omnihopper will access the equipments slots (armor & offhand), and a vertically aligned omnihopper will access the main inventory
  * Note: this does not apply to vanilla-style absorption of item entities, which continues to work without needing an Entity Tracker Upgrade
* Transfer Gadget input/output mode can now be toggled by right-clicking with an empty hand
  * Logistics Configurator is no longer required, but still works
* TNT fired from an Air Cannon (with Dispenser Upgrade) now has a much longer fuse time, but explodes immediately on impact

### Fixes
* Fix startup crash (missing fluid tags) when Mekanism not installed
* Fixed Vortex Cannon bug making it less effective at breaking grass/crops/leaves etc. than it should be (vortices missing blocks when they should have hit)
* Fixed Transfer Gadgets not rendering properly
  * New entity model and item texture for the Transfer Gadget
* Fixed Transfer Gadgets in input mode pulling from the wrong side of sided inventories such as furnaces
* Fixed spurious error icon for filter showing on Sentry Turret GUI even when the filter is valid

## 2.7.0-83 (30 Nov 2020)

### New
* Added Wall Lamps!
  * These are just simple redstone-activated lamps, in the 16 dye colors
  * Can go on any solid surface, in any of the six orientations (will pop off if their attached block is broken)
  * Also provided inverted versions which go out when a signal is applied

### Updates
* The Jackhammer now takes a Magnet Upgrade, used in the two veinmining modes
  * When installed, all veinmined blocks will be dropped at the position of the block actually broken
  * This has a small extra air cost when used (only when in a veinmining mode)
* You can now quickly cycle the active Jackhammer dig mode with Sneak & Mouse Wheel
* It is now a compile error in the Programmer to use an Area widget with a type other than Box with those widgets which can only use a Box shape
  * These widgets are: Entity Import, Entity Attack, Entity Right Click, Condition: Entity, and Pickup Items
  * These have always required an area type of Box, but previously accepted other area types, silently interpreting as Box type
  * New behaviour is clearer and less misleading
  * Existing already-programmed drones will continue to function as before (it's a compile-time check)
* Programmable Controller now accepts up to 6 Magnet Upgrades to auto-pickup items (same as actual Drones)
* Magnet Upgrade in Drones and Programmable Controllers now only functions when the drone/PC actually has a program to run
* Added client-side config setting "programmer_gui_pauses" to pause the game in SSP when the Programmer is being used
  * Default "false", which is the same as previous behaviour
  * This has no effect in SMP, of course
* Smarter behaviour when using a GPS Area Tool to set a coordinate in the Area progwidget GUI (using the "Inventory Search" button)
  * Now left-clicking the tool selects P1, and right-clicking it selects P2
  * Previous behaviour of just selecting an arbitrary point in the tool's area was not very useful
* Shift-clicking a GPS Area Tool on the background in the Programmer GUI will now create two coordinate widgets, if possible
  * The two coordinates correspond to the P1 and P2 points of the GPS Area Tool
* Fluid tagging changes (mainly around fuels)
  * All fuels are now tagged in the "forge:XXX" namespace for maximum inter-mod compat
  * Immersive Engineering's Ethanol & Plant Oil are equivalent to PNC:R Ethanol & Vegetable Oil, respectively
  * Immersive Petroleum's Crude Oil, Lubricant & Gasoline are equivalent to their PNC:R counterparts
  * API break: `IFuelRegistry.registerFuel()` methods now take a `ITag<Fluid>` rather than a `Fluid`
  * PneumaticCraft "Oil" is now known as "Crude Oil" (translation change only; internal block/fluid ID has not changed)
  * Liquid Compressor GUI: fuels tab now shows the fluid's mod when there are multiple fluids with the same name
* Allow blacklisting of oil worldgen by biome category, in addition to (already existing) biome ID
  * See "oil_world_gen_category_blacklist" in `config/pneumaticcraft-common.toml`
* Flux Compressor now has an on/off texture on its front face to indicate if it's currently running
* Some performance improvements for the Thermopneumatic Processing Plant
  * Less searching of recipe lists required now
* Potatoes, melons and apples can now be used (with Yeast Culture) to make Ethanol in the Thermopneumatic Processing Plant
  * Sugar can still be used, and processes faster than it used to
  * Beware: potatoes without Yeast Culture will make Vegetable Oil if the TPP has enough pressure
  * So don't pressurize your TPP if you intend to make Ethanol with it!
* Mekanism Uranium Blocks can now be used as heat sources, same heat properties as Immersive Engineering Uranium Blocks (not very hot, but last for ages)
  * They will eventually turn into Mekanism Lead Blocks
* Pneumatic Helmet Block Tracker: "Miscellaneous" setting will now also find Bee Nests
  * Useful to find them in thick forests...
* Amadron changes
  * Added new config setting in `config/pneumaticcraft-common.toml`: "numVillagerOffers", in addition to the existing "numPeriodicOffers"
  * Periodic trades (from JSON files) are now shuffled into the active Amadron offer list separately from villager trades instead of all being in one big pool
  * This gives server admins more control over how many periodic trades will be shown on each shuffle
  * "numVillagerOffers" and "numPeriodicOffers" set the maximum number of each offer type which can be added when the active trades are shuffled
  * You can set either down to 0, to completely prevent any of that offer type being added
  * Also, "max_trades_per_player" can now be set to 0 (previous minimum 1) to disable player->player trades being added

### Fixes
* Fixed enablement of reach distance upgrade (added in 2.6.2) persisting across world changes in SSP
* Fixed Programmable Controller minidrones "cloning" themselves on the client when leaving and returning to an area
* Programmable Controller state is now properly reinitialized when the programmable item is changed
  * Behaviour is now much more like wrenching and re-deploying a Drone, in that all variable state is reset
* Fixed client NPE when mousing over unconfigured Coordinate progwidget in Programmer GUI
* Fixed pressure text on Pneumatic Helmet HUD wrapping sometimes and looking derpy
  * I couldn't reproduce this myself, but forced a minimum width on the stat widget
* Fixed KubeJS (or indeed any mod which can modify the recipe manager) not being able to remove or modify Amadron offers
* Fixed client crash with Pneumatic Helmet hacking of Jukeboxes
* Amadron GUI cosmetic fixes
  * The search textfield is no longer focused by default, since right-clicking of offers doesn't work when it's
    focused, which is highly unintuitive.  It's still easy to search: press Tab, then type your string.
  * Fixed fluid resources in Amadron offers lacking their tooltip
  * Fixed tooltip overlap on edge of right-hand slot in an offer leading to garbled tooltip

## 2.6.2-73 (20 Nov 2020)

### New
* Placing a Range Upgrade in the Pneumatic Chestplate now gives the player a 3.5-block reach distance upgrade
  * This does not affect melee range, just block interaction
  * There is an associated air cost (5mL/t) while the upgrade is switched on
  
### Updates
* Armor GUI: upgrade button layout in the main armor screen handles low resolutions and/or large scaling better now

### Fixes
* Fixed NPE when breaking Aerial Interface

## 2.6.1-72 (18 Nov 2020)

### Fixes
* Fixed crash with Spawner Extractor on dedicated server
* Fixed a couple of HUD messages with helmet block tracker & mob spawners

## 2.6.0-70 (16 Nov 2020)

### New
* Added a new mob spawning system!
  * This is in addition to the existing (useful but limited) Spawner Agitator
  * New Spawner Core item which can be extracted from vanilla Spawners, or crafted
  * Extract full Spawner Cores from a Spawner with the new Spawner Extractor (be ready for a fight...)
  * Or craft an empty Spawner Core and use the new Vacuum Trap to fill it with mob essence
  * Use a partially or completely filled Spawner Core in the new Pressurized Spawner to spawn mobs
  * Pressurized Spawner uses pressure, can be redstone-controlled, accepts Speed Upgrades...

### Updates 
* The Vacuum Pump is now more efficient at creating a vacuum (more vacuum produced on the "-" side per air consumed on the "+" side) 
* Redstone Mode selection in GUI's has had a makeover
  * There is now one button per mode, rather than a single button to cycle the modes
* Animated stats (i.e. GUI side tabs and the popup HUD boxes for pneumatic armor) have been heavily reworked internally
  * Player-visible changes are fairly limited, but side tabs do wrap their text better now (taking advantage of some vanilla `ITextComponent`/`IReorderingProcessor` code to manage text wrapping & layout)
  * Side tabs don't scale their text now when short of horizontal space, since that never looked good. 
  * Instead, text is always wrapped to fit the available width and a scrollbar added where necessary.
  * Dropped the remaining use of raw `String` in many places, in favour of `ITextComponent`
  * There are API breaks for `IGuiAnimatedStat`, `IHackableBlock` and `IHackableEntity` - sorry!
* The `/dumpNBT` command now requires player permission level 2 (it could be used to inspect item data which should remain secret from regular players)

### Fixes
* Fixed a problem on SMP where players get kicked when they are near other players wearing or holding pressurizable items (tools & armor)
  * I couldn't reproduce this one myself, and I suspect another mod may be interfering, but I've added some defensive coding to cover it

## 2.5.0-66 (5 Nov 2020)
  
### Updates 
* Support for MC 1.16.4, which is very much compatible with MC 1.16.3
  * This release runs on both MC 1.16.3 and 1.16.4
* Thermopneumatic Processing Plant now has "has work" comparator support, like the Refinery
  * When there are valid ingredients in the TPP, and room for output, an attached Comparator will emit a signal of 15
* Pressure tube performance improvement
  * Was unnecessarily computing connections every tick (for leak detection) when it's only necessary to do so on neighbour block updates
* Programmer GUI now warns if multiple Item Filter widgets are attached to an Item Assign widget
* Area widgets in Programmer GUI now show their coordinates and/or variables (when "Show Info" is enabled)
* Drone variable parsing (with `${varname}` syntax): item variables are now supported too
* "Right Click Block" widget is now called just "Right Click" (since it can be used to click both items and blocks)
  * Cosmetic change only, no functional changes

### Fixes
* Fixed a few locale-related errors in recipe deserialization
* Fixed positioning of selected fluid in Fluid Filter GUI
* Fixed drone debugger GUI view not being draggable (like it used to be in 1.12.2)
* Fixed fluid tank displays in JEI recipe pages (e.g. for Speed Upgrades) not showing their contained fluid in the tooltip
* Fixed client crash when trying to open Condition: RF & Drone Condition: RF patchouli pages from the Programmer GUI
* Fixed client crash caused by certain mods trying to pass null world or blockpos data when querying blocks shapes for camouflageable blocks
  * Not a PNC bug as such, but pays to be defensive
  
# Minecraft 1.16.3

## 2.4.5-62 (25 Oct 2020)

### Updates
* Added a new "Void Item" progwidget for drones to use
  * Could be useful for writing quarry programs...
* The `/amadron_deliver` command now requires players to have op level 2 or greater
* Moved Amadron player offers from global config (`config/pneumaticcraft/AmadronPlayerOffers.cfg`) to per-world file (`world/pneumaticcraft/AmadronPlayerOffers.cfg`)
  * Existing player offers in your instance will be automatically moved across, but make a backup of the above file before upgrading if you're concerned
  * This isn't very relevant for single-player worlds since player offers are very much a multiplayer feature

### Fixes
* Fixed server NPE when drones try to place certain unplaceable blocks (e.g. Farmer's Delight rice when there's no water block)
* Fixed dedicated server crash (NoClassDefFoundError) when trying to merge drone programs

## 2.4.4-59 (15 Oct 2020)

### Fixes
* Hotfix: fix init crash which will affect some Java installations (I was wrongly using a library routine not guaranteed to be present at runtime)

## 2.4.3-58 (15 Oct 2020)

### Updates
* Universal Sensor heat sensor can now read the temperature of Heat Frames
  * Also other non-tile entity blocks such as magma, campfires, ice, etc...

### Fixes
* Fixed occasional Aerial Interface crashes on player login when connected to FE cables
* Fix start up crash in certain locales
  * e.g. Turkish, or any other locale where lowercasing a capital letter gives a non-ASCII character
* Fixed server crash caused by Amadrones trying to restock more than 36 stacks of a player offer at once
  * Drone carrying limit is 36 stacks; if more is available for restocking, multiple trips will now be made
* Fixed Volume Upgrades not being taken into account for negative pressures (Vacuum Pump)

## 2.4.2-49 (9 Oct 2020)
 
Releases from 2.4.2 onward *require* Forge 34.1.0 or later.

### Updates
* Mekanism integration has returned!  To recap:
  * Mekanism and PneumaticCraft blocks can exchange heat
    * See `config/pneumaticcraft-common.toml`, "Integration" section, for some settings on heat exchange properties
  * Mekanism Fuelwood and Resistive Heaters can be used to heat PNC:R machines like the Refinery or Thermopneumatic Processing Plant
  * PNC:R Vortex Tube can be used to heat the Mekanism boiler
  * Heat cables (PNC:R Heat Pipes and Mekanism Thermodynamic Conductors will connect to Mekanism and PNC:R machines, respectively)
  * Mekanism Liquid Ethylene and Liquid Hydrogen can be used as fuels in PNC:R Liquid Compressors
  * Mekanism Configurator can be used to wrench PNC:R blocks
* GUI textfields in various places can now all be right-clicked to clear their current text
* Hopefully improved air particle rendering
* Cyclic XP Juice is now supported in the Aerial Interface

### Fixes
* Fixed Safety Valve tube modules not releasing air when they're supposed to
* Ensure oil lakes configured feature is properly registered (not doing so can cause compat problems with other mods' worldgen)
* Fixed server crash (ConcurrentModificationException) related to Skeleton Horse traps
* Programmer area previewing now disables depth testing (i.e. preview blocks are no longer hidden when in or behind solid blocks)
* Fixed Minigun Ammo and Micromissiles having infinite durability
* Fixed Micromissile tooltips not fully rendering

## 2.4.1-44 (25 Sep 2020)

### Updates
* AE2 Support has returned!  Tested with AE2 8.1.0-alpha.3
  * To summarise: place a Requester Frame on an ME Interface, and tick the "AE2 Integration" checkbox in the AE2 GUI tab
  * Then all items provided via PNC Logistics will appear as "craftable" items in AE2
  * Request crafting of such items to get an available Logistics Drone to fetch those items from PNC Logistics and deposit them into the ME Interface
* Logistics drone behaviour tweaks:
  * If a drone is carrying some resource (item or fluid) that it's unable to drop off, it will now still be able to handle requests for the other resource type
  * Drones will now also ignore minimum order sizes if dropping off a resource they're already carrying (but continue to honour minimum order sizes if it means collecting that resource from a provider frame)
  * Storage and Default Storage frames now also support specifying minimum order sizes
* It's now possible to right-click a Drone (that you own) with an empty fluid tank to drain any stored liquid from its internal tank
  * This may be useful if you end up with a Logistics Drone which can't drop off its fluid (items are dropped when a drone is wrenched, but fluid is not)
* Added some config control over villager trades and house generation (primarily for the use of progression-based modpacks). See the `Villages` section in `config/pneumaticcraft-common.toml`
  * `addMechanicHouse` (boolean - default true) controls whether Pressure Mechanic houses can appear in villages
  * `mechanicTrades` (NONE, PCB_BLUEPRINT or ALL - default ALL) defines which trades a Pressure Mechanic offers
  * Note that neither setting is retroactive - any already-generated houses and villagers will not be affected by this

### Fixes
* Fixed client crash when rendering fluid textures in GUI's
* Fixed Amadron trade addition GUI bug where trying to open item/search GUIs just returned to the main Amadron GUI
 
## 2.4.0-36 (18 Sep 2020)

The initial 2.4.0 release for MC 1.16.3 release is largely equivalent in functionality to the 2.2.2 release (for MC 1.16.1) with a couple of small worldgen-related changes.

### Updates
* Oil Lake worldgen blacklisting is now done by biome ID instead of dimension ID (see `oil_world_gen_blacklist` in `config/pneumaticcraft-common.toml`)
* Oil Lakes can now generate in the Basalt Deltas biome in the Nether (but no other Nether biomes)

# Minecraft 1.16.2

*There were no public releases for Minecraft 1.16.2 (would have been PneumaticCraft: Repressurized 2.3.x).*

# Minecraft 1.16.1

The initial 1.16.1 release is largely equivalent in functionality to the 1.4.2 release (for MC 1.15.2), with a few minor player-visible changes.

Releases from 2.1.0 onward *require* Forge 32.0.108 or newer.

## 2.2.2-35 (18 Sep 2020)

## Updates
* All pressurizable tool items are now unstackable (does not apply to drones or air canisters)
* When Alt-clicking an item in the Smart Chest GUI, holding Shift as well will set the slot item limit to the stack's max size instead of the stack's current size

## Fixes
* Fixed drone "Right Click Entity" prog widget not always working (e.g. milking cows with an empty bucket failed)
* Fixed air dupe exploit with stacked pressurizable items and the Charging Module
* Fixed player death message translations

## 2.2.1-32 (14 Sep 2020)

### Fixes
* Fixed Amadrones thinking they had only one inventory slot, causing orders with more than 64 of an item to fail
* Fixed Memory Stick sometimes losing its stored experience
* Left-clicking Memory Stick to toggle XP absorption mode now also works when left-clicking air (previously required a block to be clicked)
* Drone "Inventory Export" widget will now try to keep items stacked in the inventory it's exporting to
  * This also applies to Collector Drones, which use the "Inventory Export" widget internally

## 2.2.0-29 (3 Sep 2020)

Important: if you are also using Immersive Engineering, this release of PNC:R *requires* IE 1.16.1-4.0.0-118 or newer.

### Updates
* Drones (with the Pick up Items widget) will no longer "steal" items off Immersive Engineering conveyor belts
  * Specifically, the widget now honours the "PreventRemoteMovement" entity tag
  * This can be overridden via the widget's GUI when creating the drone program
* A few small cosmetic cleanups and improvements in the Charging Station GUI (including the item upgrade sub-GUI)
  * Upgrade sub-GUI no longer shows armor slots (it's pointless there)
* Reinforced Stone blocks are now a little easier to mine up (blast resistance has not been changed though)
* Sourdough Bread can now be used to make Culinary Construct sandwiches. Yum.
* Overhauled the Drone special variables system a bit:
  * New special variables: `$drone_pos`, `$player_pos`, `$controller_pos`, `$owner_pos`
  * Existing variables `$drone`, `$player` & `$owner` still work but it's recommended to use the new ones
  * Note that `$drone` gets the blockpos *above* the drone (which it always has for historical reasons), but `$drone_pos` gets the drone's real blockpos
  * `$controller_pos` is completely new and gets the Programmable Controller's blockpos, or (0,0,0) if used by an actual drone entity
  * Patchouli manual is updated (see Programming / Variables section) with much more detail.
* Aerial Interface now supports Industrial Foregoing Essence as an XP fluid.
* Waila now shows 2 decimal places of pressure in the focused block, same as The One Probe already does.
* When Charging Station is set to output redstone, frequency of signal changes is now limited to at most once every 10 ticks
  * This should reduce lag caused by rapid output toggling, which can happen when the charged item is at or around the charging threshold
* Thermal Compressor GUI: temperature gauge scales have been tweaked to hopefully show a clearer distinction between hot & cold sides

### Fixes
* Fixed (hopefully) an issue where client-side Logistics Frames would disappear on certain blocks
  * This also caused a client-side crash if right-clicking a "missing" frame with the Logistics Configurator
* Fixed Elevator sometimes entering an air feedback loop, rapidly leading to explosions
* Fixed drone Block Right Click widget sometimes bugging out, leaving the drone repeatedly trying to right-click the same block, even when an area is provided
* Fixed Goto programming widget GUI "Done when departed" and "Done when arrived" meanings being switched.
* Fixed Programmer not showing programming widgets on hi-dpi displays (Macbooks)
* Fixed Pressure Chamber crafting issue where items split across multiple stacks would not get recognised as valid recipe ingredients
* Fixed Liquid Hopper not being able to fill fillable items (buckets, tanks...) dropped in front of the hopper output side.
* Fixed Pneumatic Armor bound keys also triggering when any GUI is open, e.g. typing in a textfield
* Fixed Minigun item being rendered too far right in GUI context
* Fixed Jackhammer item model always rendering with drill bit when not held by player (even when no bit is installed)
* Fixed Pneumatic Wrench opening machine GUIs when rotating blocks in creative mode
* Fixed The One Probe showing a block's current pressure where the max pressure should be shown
* Fixed very long words not being split in GUI side tabs, causing unreadably small text
  * This mainly affects languages such as Chinese, where whitespace is not necessarily used.
* Fixed heat-tintable blocks (e.g. Compressed Iron Block) flashing blue briefly when placed down

## 2.1.1-14 (14 Aug 2020)

### Updates
* Tweaked heat & air output of advanced compressors somewhat
  * All compressors now produce heat proportional to the air produced (adding 1 heat per 20 air/tick produced)
  * This affects the Flux Compressor in particular, which was producing much less heat per air than the Advanced Compressor & Advanced Liquid Compressor
  * Flux Compressor now produces double the heat it used to, so yes: this can be considered a nerf!
  * Heat generation will drop a bit as efficiency drops (since less air is being produced)
  * Fixed issue where fractional amounts of air/tick were getting rounded down (so compressors may produce very slightly more now, depending on circumstances)
* Added Waila/Hwyla support back in
* Drone placement tweak: if Drone is deployed on block with no collision box, deploy the Drone *in* that blockspace instead of the adjacent one

### Fixes
* Fixed Jackhammer being able to break bedrock (and other unbreakable blocks) in area dig modes
* Fixed Drone upgrade crafting recipe not needing a PCB ingredient

## 2.1.0-11 (11 Aug 2020)

### New
* Added a Pneumatic Jackhammer!
  * Powerful and customisable multi-tool which can efficiently break any block
  * Multiple digging modes, selectable via GUI; better modes require a better Drill Bit to be attached
  * Includes two vein miner modes: ores & logs only, and all blocks (requires the top-end Netherite Drill Bit)
  * Upgradable with Speed Upgrades and Volume Upgrades: top speed is extremely fast, but extremely pressure-hungry
  * Can also take a Fortune or Silk Touch enchanted book (swappable on the fly; no enchanting table needed)
  * Documented more in the Patchouli manual (in the "Tools" section)
* Added a Renewables system!
  * Make Biodiesel via a multi-step process requiring the production of Yeast Solution, Ethanol and Vegetable Oil
  * Biodiesel is equivalent to Diesel in fuel quality, and can also be used to make Lubricant and Plastic
  * Some useful by-products & foods are also added:
    * Bandages
    * A way to get double the Speed Upgrades per bucket of Lubricant
    * Sourdough Bread, Chips, Cod & Chips - all good-quality foods
  * All documented in the Patchouli manual in a new "Renewables" section
* Added a new Fluid Mixer machine which can mix two fluids into a new fluid and/or item
  * Used by default for making Biodiesel
  * Can also mix Water & Lava to make Obsidian
  * More recipes can be added via datapacks

### Updates
* Kerosene is now much more efficient in the Kerosene Lamp than other fuels
* Pneumatic Armor HUD now displays only 1 decimal place of pressure for armor pieces
  * This is not just a cosmetic change, but part of an overall performance improvement with pressure sync'ing
  * Previously sync'ing pressures of pneumatic items (including armor) was much too chatty in terms of server->client traffic
  * Now only rounded air levels are sync'd to client, so the client generally knows item pressures with 0.1 bar of precision
  * This is enough for the vast majority of display purposes; e.g. item pressure tooltips already display with 0.1 bar of precision
  * Server->client traffic is now greatly reduced, especially for fast-changing pressures such as Jet Boots
* Added the option to display graphical bars rather than numbers for Armor HUD pressure readouts
  * Select this in main "Core Components" armor GUI - it's also persisted to client config (`show_pressure_numerically`) 
 
### Fixes
* Fixed buggy drone "Right Click Block" behaviour (ported forward from 1.5.2 version)
* Progwidget Item Filter GUI: don't gray out the "Match NBT" checkbox when the filter item has no NBT data
  * Just because it has no NBT data doesn't mean actual instances of the item can't...
* Fixed (worked around) a problem where drones would refuse to pathfind to the space above "tall" blocks
  * "Tall" blocks being fences, walls, gates...
* Fixed drones ignoring installed Inventory Upgrades
* Fixed buggy Heat Frame cooling behaviour (would void some fluid containers such as Mekanism tanks)
* Fixed Logistics Frames not being placeable on the Aerial Interface
* Fixed possible race conditions when registering custom model loaders (pressure chamber glass, minigun, fluid tanks)
  * Now using Forge `ModelRegistryEvent`, hence the 32.0.108 requirement

## 2.0.0-4 (30 Jul 2020)

### Updates
* The Programmable Controller can now optionally charge (with pressure and/or Forge Energy) the "drone's" held item (i.e. the item in inventory slot 0)
  * Added a GUI tab with a checkbox to the controller's GUI to control this.
* The Electrostatic Compressor now needs an Advanced Air Compressor to craft (rather than a basic Air Compressor)
* A few heat-related changes
  * Heat frames now take on their environment's ambient temperature when placed down instead of just using 30C
  * Heat frames will now slowly cool (or warm) back to their ambient temperature when left idle
  * Heat sinks now don't burn the player until they reach 60C (up from 50C).  Burn damage is less at 60C then it used to be, but hotter heat sinks now hurt more to stand in front of.
  * Tintable blocks (Heat Sinks, Compressed Iron Blocks, etc.) now vary their tint much more smoothly as their temperature changes.


### Fixes
* Fixed fluid rendering tile entities not rendering when any GUI is open

# Minecraft 1.15.2

## 1.4.2-58 (29 Jul 2020)

### Updates
* Smart Chest filter slots can now also limit the number of items allowed in a slot
  * Default limit is the size of the itemstack in the slot when filtering is enabled
  * Use Alt + mouse wheel (or Alt + key UP/DOWN) to adjust the limit
    * Hold Shift as well for fast adjustment
  * Existing Smart Chests from previous worlds will use a limit of 64 (or whatever the item's max stack size is) to avoid changing existing behaviour
* Omnidirectional Hopper can now use round-robin when exporting items
  * Default is to try leftmost item first, as before
  * Use button in GUI to toggle to round-robin export
  * Could be useful for example when feeding the player via Aerial Interface for a varied diet
* Aerial Interface in player feed mode now informs the player what they just ate
    
### Fixes
* Fixed NPE when throwing some blocks with chestplate launcher
* Fixed NPE when placing down a new Programmable Controller

## 1.4.1-56 (20 Jul 2020)

### Updates
* Drones can now absorb Experience Orbs
  * Use the Entity Import puzzle piece to do this
  * Added a "@orb" entity filter to whitelist or blacklist XP orbs
  * Imported orbs are auto-converted to Memory Essence fluid and stored in the drone's internal fluid tank
  * Conversion is at the standard rate: 1 XP point = 20mB Memory Essence 
  * Use a Fluid Export puzzle piece to deposit imported Memory Essence into any fluid tank
  * Added config option `drones_can_import_xp_orbs` (default true) to disable this feature entirely if desired
    
### Fixes
* Fixed clientside crash related to minigun tracer rendering with multiple players involved
* Heat Frame fixes: item dupe and loss issues under some circumstances with both cooking and cooling
* Fluid tanks with any fluid will now never stack, even with identical fluid & amount (empty Fluid Tanks will still stack)
* Fixed empty Fluid Tank items being unstackable with other empty tanks after draining in item form (e.g. during heat frame cooling)

## 1.4.0-53 (14 Jul 2020)

### New
* Mekanism integration, still fairly experimental and subject to rebalancing
  * Mekanism and PneumaticCraft blocks will now exchange heat
    * See `config/pneumaticcraft-common.toml`, "Integration" section, for some settings on heat exchange properties
  * Mekanism Fuelwood and Resistive Heaters can be used to heat PNC:R machines like the Refinery or Thermopneumatic Processing Plant
  * PNC:R Vortex Tube can be used to heat the Mekanism boiler
  * Heat cables (PNC:R Heat Pipes and Mekanism Thermodynamic Conductors will connect to Mekanism and PNC:R machines, respectively)
  * Mekanism Liquid Ethylene and Liquid Hydrogen can be used as fuels in PNC:R Liquid Compressors
  * Mekanism Configurator can be used to wrench PNC:R blocks

### Updates
* PNC:R loot items (Stop! Worm, Nuke Virus and Spawner Agitator) should be turning up again in dungeon loot
  * This is still under review to potentially add other PNC:R loot
* More Pneumatic Armor settings (primarily around air usage) are now tunable via mod config
* Jet Boots GUI now offers a throttle control slider bar (default is 100% power)
  * Reducing this might be useful if you have fast jet boots in a tight space, like caves...
* Improved visual appearance of temperature gauges in machine GUI's
* Heat Frame Cooling improvements
  * Making Ice and Obsidian can now be done with tanks (PNC, but also some other modded tanks) containing Water or Lava respectively, not just buckets
  * Made recipes with bonus output a bit more obvious in JEI (spot the big yellow "+" icon!)
  * Recipes taking fluid ingredients now show the ingredient in buckets and in tanks in JEI, for a visual clue
  * Added bonus output for Lava->Obsidian, up to 25% depending on Heat Frame temperature

### Fixes
* Fixed performance issue when world has a large number of entities
  (an event handler was running which scanned all entities every tick, when it only needed to scan a few)
* Fixed bug where upgrades weren't always properly processed in chunkloaded machines
  * Caused machines with volume upgrades to explode on world reload
* Fixed pathfinding bug making Amadrones unable to collect more than two stacks of items
* Fixed keybind handling bug causing modifiers (shift/control/alt) to be ignored in some circumstances
* Fixed buggy Chestplate Launcher behaviour (wrongly consuming the mainhand item)

## 1.3.2-42 (26 Jun 2020)

### Updates
* Block camouflage now properly mimics the complete blockstate recorded on the Camouflage Applicator
  * e.g. for rotatable blocks like Logs, the rotated log is now mimicked, not just the log's default (unrotated) blockstate
* Breaking any machine with redstone settings now preserves those settings (even when broken with a pickaxe)

### Fixes
* Fixed minigun tracers rendering behind other blocks
* Fixed drone block placing (Place widget) not consuming items when block item being placed was not in drone's inventory slot 0
* Fixed NPE with tile entity moving under certain circumstances (see https://github.com/TeamPneumatic/pnc-repressurized/issues/540)
* Fixed Tag Filters in inventories with Requester Frames wrongly counting as requested items

## 1.3.1-39 (22 Jun 2020)
### New
* Immersive Engineering integration has been added back (note: at time of writing, Immersive Engineering is in alpha status on 1.15.2)
  * Harvesting Drones (and Harvest programming widget) recognise Hemp as a harvestable crop
  * External Heater can be used to heat PneumaticCraft blocks (energy usage and efficiency can be adjusted or disabled in PNC config)
  * Immersive Engineering Hammer can be used as a wrench to rotate/drop PneumaticCraft blocks
  * PneumaticCraft Diesel can be used in the IE Diesel Generator, and IE Biodiesel can be used in PNC liquid compressors
    * They are equivalent to each other in fuel quality
  * Pneumatic Chestplate with Security Upgrade installed will protect you from uninsulated wiring damage (at an air cost proportional to the damage prevented)
  * IE Uranium Blocks function as a PneumaticCraft heat source (not very hot, but last a *long* time, eventually turning to Lead Blocks)
  
### Updates
* All sound effects have been converted to mono, since Minecraft doesn't do distance-based attenuation on stereo sounds
  * This fixes the problem of sentry turrets, air leaks, etc. sounding much too loud from a distance
  * Because of this, some default sound volumes have been adjusted upwards in the mod config `pneumaticcraft-client.toml`
  * If you're updating from a previous version (rather than a fresh install) some sounds might seem too quiet; if so, you can review their volume in the above config file on your client
* Universal Sensor work
  * Base range (with no Range Upgrades) has been increased from 2 to 8 blocks
  * ComputerCraft event support - `os.pullEvent('universalSensor')` - should work now 
  * All sensor description texts are now localised, and tidied up
  * GPS Tool icon in the GPS slot is now shaped more like the item
* Sentry Turrets now point the same direction you're facing when you place them (when they're idle, of course)
* Sentry Turret entity filter is now preserved if you break the turret with a wrench (sneak + right-click)
* Safety Valve Modules on basic Pressure Tubes now leak at 4.92 bar instead of 4.9 bar
  * This avoids unwanted leaks if you're pressurising a tier 1 network from a tier 2 network with Regulator Module (4.9 bar)

### Fixes
* Programmer GUI: fixed updates to Coordinate widgets not getting sync'd to server
* All GUI upgrade tabs: the upgrade list is now sorted by upgrade name instead of being completely arbitrary
* Fixed Sentry Turret bullet tracers rendering slightly too low for some targets
* Universal Sensor block is now recognised as a redstone emitter by drone Redstone Condition widget
* Fixed up the Universal Sensor GUI upgrades tab (it was a big mess)
* Players in spectator mode are now ignored by Air Grate Modules and Sentry Turrets
* Fixed Liquid Hoppers not being able to absorb fluid from buckets in front of their input
* Fixed a couple of fluid dupe issues:
  * Tanks may now only be used as crafting ingredients when completely empty (item must have no NBT, so newly-crafted is recommended)
  * Emptying a stack of full tanks into another larger tank would transfer twice the fluid (actually a Forge bug but worked around in PNC)

## 1.3.0-33 (12 Jun 2020)

### Updates
* Logistics Core recipe has been changed to create two cores instead of one
* Some drone pathfinding optimisation, in particular reducing server-side CPU usage when drones can't find a path
  * Added some checks to cull a lot of unnecessary pathing searches
* Small visual improvement for the Omnidirection & Liquid Hopppers
  * Output spout is slightly narrower, and the output end is a little darker
* Pastebin GUI: added "Pretty?" option to control pretty-printing of JSON output to Pastebin/clipboard
  * Default is now *not* to pretty-print JSON, so saved drone programs & remote layouts are a lot more compact
* Redstone Module I/O direction can now be configured in the module's GUI
  * This does not require an Advanced PCB to be installed
  * Toggling I/O with a Wrench still works too, as it used to
* Drones (both entity and item form) now show their carried fluid, if any
  * Shown in item tooltip for drones in item form
  * Shown in TOP/Waila display for drone entities
* It's now possible to attach levers/buttons/etc. to the side of camouflaged blocks (assuming the camouflage is solid, of course)
  * This is particularly nice for camouflaged Pressure Tubes with Redstone Modules
* Elevators with Charging Upgrades installed will now only add air back if it's safe to do so (pressure < 4.9 bar)  
* Some Micromissiles improvements
  * Micromissiles now do more damage on direct entity hit (explosion position was further from the target entity than it should have been) 
  * Micromissiles can now be "repaired" in an Anvil with TNT (1 TNT restores 25 missile shots)
  * Micromissile smoke particles are a bit less dense now
  * Reduced default cooldown time from 15 ticks to 10 ticks (still adjustable in config)
* Some Sentry Turret improvements
  * Default entity filter for newly placed Sentry Turrets is now `@mob` - avoids pain & suffering if players load the turret and forget to set a filter
  * Fixed the entity filter string not showing in GUI the first time it's opened
  * Better filter validation and error display in the GUI
  
### Fixes
* Fixed Charging Stations at 0 bar not being able to discharge pressure from items into the station
* Drones should be able to place blocks much more reliably now
* Fixed Drones (and other entities) sometimes getting badly confused while trying to path across or through some PneumaticCraft blocks
  * Blocks were wrongly reporting themselves suitable for pathing through, when their hitbox actually didn't allow it
* Fixed the clientside `drama_splash` config option not being properly honoured when set to false
* Some Programmer GUI fixes:
  * Fixed widget area dragging behaviour when dragging items from inventory slots across it
  * Fixed Area widgets in the programmer GUI not being sync'd to server when clicked with a GPS (or Area GPS) Tool
  * Fixed Area widgets jumping position when clicked with a GPS or Area GPS tool in the programmer
* Fixed Pastebin communication (now using HTTPS exclusively, looks like HTTP is no longer accepted) 
* Fixed client crash caused by wrongly attempting to send some server->client sync packets
  * Caused sometimes when breaking off camouflage blocks
* Performance fix: Omnidirectional and Liquid Hoppers were causing some unnecessary block updates when their comparator output level changed 
* Fixed Omnidirectional Hopper comparator output being inaccurate under certain circumstances
* All translation keys are now properly namespaced
  * No player-visible change here but eliminates the risk of translation key clashes with other mods
* Electrostatic Compressor item tooltip is much shorter now (long tooltip still visible in JEI and GUI side tab)

## 1.2.2-30 (1 Jun 2020)

### New
* Added new Thermal Lagging block, an alternative to covering your Refinery & other heat machines with trapdoors
  * Thermal Lagging has no collision box unless sneaking or holding a wrench or pickaxe
  * Makes it easy to "click through" to the machine behind the lagging

### Updates
* Small/Medium/Large tanks now have Comparator support to measure their fullness
* Pressure Mechanic villagers now have some workstation sounds again, as they did in 1.14.4

### Fixes
* Fixed Programmable Controller drones being unable to pick up items or pull items from chests
* Fixed Programmable Controller ignoring its Inventory Upgrades
* Fixed player kick (and subsequent inability to log back in) caused by middle-clicking non-ammo slots in the Minigun magazine GUI
* Fixed minor fluid rendering issues in connected tanks (Small/Medium/Large Tank)
* Fixes to drone pathfinding to hopefully work better with "openable" blocks like trapdoors, doors, etc.
  * Note that drones still can't properly pathfind through doors and trapdoors but this helps interacting with blocks that are covered with trapdoors
* Fixed drones losing all their carried fluid when wrenched to item form
* Fixed Amadrones sometimes dropping their carried items when they suicide
  * Couldn't reproduce this one myself, but added some extra checks to ensure Amadrones never drop anything on death  
* Fixed programmer GUI widget area clipping issues (sometimes not showing any widgets)
  * Most obvious when using the "Auto" GUI scale - this is now much more robust
* Fixed semiblock items (crop sticks, logistics frames etc.) being consumed when placed in creative mode
* Fixed crop sticks allowing multiple sticks to be placed in the same block space

## 1.2.1-25 (25 May 2020)

### Updates
* Made the volume of several sounds configurable in client-side config (pneumaticcraft-client.toml)
  * Miniguns (item, drone & sentry turret)
  * Air leaks (beware of setting this to silent, it helps tracks leaks in your system!)
  * Jet Boots (with separate volume level for builder mode)
  * Elevators
* Regulator Module (without Advanced PCB) now always regulates to 4.9 bar, even when on Advanced Pressure Tubes
  * Was a bit pointless having a Regulator which regulated to 19.9 when the point is to regulate down to tier 1
  * Pressure level is still interpolated from 4.9 down to 0 based on redstone (0 redstone = 4.9 bar, 15 redstone = 0 bar)
  * Regulators with an Advanced PCB are still fully configurable, as always
* Elevators are now much easier to build
  * Elevator Frames are now placed like scaffolding
     * Right-click a frame against another places it on top, building a tower from the bottom
     * Sneak-right-click places it normally (if possible)
  * Elevator Frames will now drop as an item if the block beneath is not an Elevator Base or Elevator Frame
  * Elevator Frames can now only be placed on an Elevator Base or Elevator Frame

### Fixes
* Elevator fixes
  * Fixed client crash when cycling through floors in Elevator GUI (with no Elevator Callers present)
  * Fixed Elevator Frames having a player-blocking hitbox with 3x3 or larger elevators
* Fixed External Program progwidget not doing anything
* Fixed Safety Tube Module not releasing air when it should
* Fixed Regulator Module bug which caused excess pressure to build up in their tube section
* Fixed Refinery Controller block shape (caused x-ray effect with solid adjacent blocks)
* Fixed client crash when trying to camouflage blocks with Pressure Chamber Glass

## 1.2.0-20 (18 May 2020)

### Known Issues
* There is an incompatibility with the version 1.15.2-10m and older of the Performant mod, which (with default settings) will mess up Drone pathfinding
  * Using Performant 1.15.20-11m (not released at time of writing) or newer fixes the problem
  * Alternatively (for older versions), setting `fastPathFinding` to `false` in Performant's config works around the problem

### New
* ComputerCraft support has returned!
  * You will need CC:Tweaked, version 1.88.1 or newer.  Older releases will *not* work.
  * Other than that, it should work pretty much as it did for 1.12.2, although testing has been fairly light so far, so consider this an alpha-level feature.

### Updates
* Pneumatic Armor can now be repaired in an Anvil with Compressed Iron Ingots (and experience)
  * Each ingot repairs 16 durability
  * XP Level cost is (ingots / 2), minimum 1 level
  * This is an addition to using Item Life upgrades, which still work
* Drones with Magnet Upgrade now respect Botania's Solegnolia
* Drones now apply a 40-tick pickup delay to items dropped with the Drop Item widget, same as players do
  * This avoids problems where Drones with Magnet Upgrades just immediately pick up dropped items again
  * The delay can be disabled in the Drop Item widget GUI if you need to
  
### Fixes
* Fixed non-fatal startup exceptions being logged when soft-depended mods (e.g. The One Probe) aren't present 
* Fixed Logistics Drones & Modules not properly handling NBT matching in item filters
* Fixed Aphorism Tile not recalculating the text scale when text adjusted server-side (via drone)
* Fixed GPS Area tool Line and Wall modes sometimes being off by one block (arithmetic rounding error)
* Several GUI fixes and cleanups:
  * Fixed widgets with dropdowns (combo box and color selector) often rendering their dropdown under other widgets
  * Cleaned up the prog widget Area GUI and GPS selection GUI a bit
  * Fixed Programmer widget tray dragging sometimes selecting a widget under the open tray instead of the widget in the tray
  * Fixed fluid searching in the Liquid Filter prog widget GUI

## 1.1.2-14 (May 11 2020)

### Updates
* Oil and Lubricant are now in the `forge:oil` and `forge:lubricant` fluid tags, respectively.
  * This means that (for example) Silent Mechanisms Oil (also tagged as `forge:oil`) is now accepted in the Refinery by default.
* Disabled Redstone particle emission for pressure tube modules
  * This was unreliable and fixing it properly would involve a lot more packet syncing from server to client which is not really worth it
  * Waila/TOP still shows the output level

### Fixes
* Fixed infinite air exploit with Thermal Compressor
  * Thermal Compressor handles heat -> air conversion differently now since there was the possibility of a feedback loop in conjunction with a Vortex Tube
  * Increased heat capacity of several heat source blocks (such as magma) to compensate.
  * It's technically still possible to generate "free" air with sufficiently complex setups, but the amount generated is very small and not really worth the effort (it would be very difficult to do any useful work with it). This is a necessary compromise between having an exploitable system and making the Thermal Compressor so weak that it's useless.
* Fixed server crash with UV Light Box (related to a drone flying nearby and playing particle effects)
* Fixed some bugginess in Pressure Tubes connecting and disconnecting or wrongly leaking
* Fixed Small Tanks not getting used up in crafting recipes
* Fixed Logistics Module not rendering properly
* Fixed Pressure Chamber crafting bug where 2 milk buckets made slime balls (correct recipe is 1 milk bucket + 4 green dye = 1 bucket + 4 slime balls)
* Fixed Heat Frame Cooling not working on dedicated server
* Fixed Flux Compressor GUI not offering a "Low Signal" redstone mode
* Fixed Charging Module over-aggressively caching an item handler capability (was most apparent with Aerial Interface not reliably working with side switching and the Charging Module)
* Fixed crashes when adding Amadron player-player trades
* Fixed Amadron not matching items with NBT even when the item in the offer has no NBT
  * E.g. Quark adds some NBT to the vanilla Compass, which is a villager Amadron can offer sometimes. With the added NBT, any Compasses players held were untradeable.

## 1.1.1-6 (May 5 2020)

### Updates

* Added/updated some item/block tags
  * `pneumaticcraft:plastic_sheets` contains `pneumaticcraft:plastic`
  * `minecraft:stone_bricks` includes `pneumaticcraft:reinforced_stone_bricks`
  * `forge:stone` includes `pneumaticcraft:reinforced_stone`
* All crafting recipes using compressed iron now use the tag `forge:ingots/compressed_iron` rather than the compressed iron item
  * Note that Silent Mechanisms also provides compressed iron using this tag, so you can use that mod's compressed iron too. 
  * This is fine with me since Silents compressed iron is probably more effort to make than PNC:R's.
* All Refinery and TPP recipes now use fluid tags rather than the fluid directly.  
  * All PNC:R fluids have a corresponding fluid tag now (e.g. `pneumaticcraft:diesel` tag contains `pneumaticcraft:diesel` fluid by default and so on)
  * This makes it a lot easier to support other mods' fluids with default recipes - just add them to the appropriate `pneumaticcraft:` fluid tag
* Entity filters now support a couple of new matches for sheep, wolves & cats
  * `sheep(shearable=yes)` matches only sheep which have wool right now (good if you want to give your shearing drone a break)
  * `sheep(color=XXX)` matches only sheep of a given color (colors are e.g. "white", "black", "light_blue"...)
  * `wolf(color=XXX)` and `cat(color=XXX)` match only wolves/cats with a given collar color, just in case you feel the need to sort your pets by collar color

### Fixes

* Fixed Refinery GUI not showing output fluids
* Fixed Amadron recipe scanning barfing if recipe JSON's are missing `type` field; such recipes are now quietly ignored
* Fixed server crash if no valid villager professions are found when looking for villager trades to add to Amadron (this should only happen if a previous datapack loading error occurred, like the missing `type` field error above)
* Removed reference to non-existent plastic mixer from the Patchouli guidebook which was causing some log spam about missing items

## 1.1.0-3 (May 3 2020)

This release adds no major new features to the 1.14.4 version, but there are several smaller changes & fixes worth noting.  See 1.14.4 changes below for major changes relative to 1.12.2, and also https://gist.github.com/desht/b604bd670f7f718bb4e6f20ff53893e2

### Updates
* Security Station hacking has been disabled for now; it needs a reimplementation.
  * The Security Station still works to protect areas but currently can't be hacked.
* Recipes
  * All machine recipes are now handled through the vanilla recipe system, and loaded from `data/<modid>/recipes/<machine-type>/*.json`. 
  * The most player-visible effect of this is that machine recipes now show up properly in JEI on dedicated servers.
* Amadron changes
  * Builtin Amadron offers are now loaded as vanilla recipes from `data/<modid>/recipes/amadron/*.json`.  Note that villager trades and player-player offers are still handled separately.
  * JEI now shows *only* the Amadron offers which have been loaded from datapack.  Periodic villager trades and player-player offers require an Amadron tablet to view.
* Smelting Plastic Construction Bricks to Plastic Sheets no longer provides any experience.
* Reduced vertical aggro range of Guard Drone to 8 up & 5 down, to minimise risk of aggroing something in a cave deep below and teleporting off, leaving owner puzzled as to where it went.  Horizontal range is unchanged at 16 in each direction.
* Logistics advancements no longer require plastic to be unlocked (since logistics items no longer require plastic...)
* Drones no longer use their owner's UUID for their fake player.
  * While this was convenient for protection mods, it introduced some subtle problems, where the server associated a player's UUID with a fake player object instead of the real player.  The most obvious effect of this was advancements often not working.
  * Protection mods should now use "{playername}_drone" to permit a given player's drones.
* Improved textures for Air Cannon, Vacuum Pump & Charging Station.  Also, these machines now use Reinforced Stone Slabs instead of Cobblestone or Stone slabs in their crafting recipes.
* Reduced network chatter for leaking pressure tubes (sounds and particles now played purely client-side)
* Reduce network chatter for moving elevators
* Reduced Air Grate air usage; it now only uses air when actively pushing/pulling entities
* Botania support added back
  * Solegnolia blocks the Pneumatic Chestplate Magnet upgrade
  * Paint lens will now dye Plastic Construction Blocks
  * Blaze Block now act as heat sources (turn to glowstone on excess heat extraction)
* Programmable Controller now accepts Forge energy (up to 100,000FE)
  * Allows it to use Import/Export RF programming widgets properly
* Sheep can now be hacked (randomise their wool colour)
* Pneumatic machines and tubes now occasionally creak if over-pressure and air is added
* Vortex tube now has permanent red & blue bands at either end to make it clear which is the hot side and which is the cold side
* Pressure gauge module now only renders when player is within 16 blocks
* Pressure interface doors render better, especially when beside Pressure Glass (doors now no longer stick out the side when open)
* Jet Boots speed slightly increased, back to 1.12.2 levels (was a little slower in 1.14.4 version due to an error on my part)
* Fixed multiblock elevators playing their sound effects much too loud.
* JEI now shows all "special" crafting recipes: gun ammo + potion crafting, drone dyeing, drone upgrading, guidebook crafting and pneumatic helmet + one probe crafting.

# Minecraft 1.14.4

This release brings a very major internal rewrite and many many major new and modified gameplay elements. See also https://gist.github.com/desht/b604bd670f7f718bb4e6f20ff53893e2

## 1.0.1-10 (Apr 17 2020)
### Fixes
* Fixed Block Tracker behaviour (performance and crashes) with Hackables
* Also, Block Tracker now picks up blocks added by block tag (doors & buttons)
* Fixed client crash (NoSuchMethodError) when pressing Return (insert line) in an Aphorism Tile gui, when on dedicated server
* Fixed Elevator not always rendering when extended
* Fixed Gas Lift: block model now only shows tube connectors where connected, and GUI now shows fluid in the tank
* Fixed Air Cannon not being able to fling entities when Entity Tracker upgrade is installed
* Refinery Output block now has the right block shape

## 1.0.0-8 (Mar 22)

### Known issues
* On dedicated server, JEI may not show custom machine recipes when you log in. 
  * If this happens, a "F3+T" asset reload will fix the problem.
  * A timing issue as far as I can tell (JEI processes recipes before the client gets custom recipes from the server); don't have a fix for it yet.
* JEI currently doesn't show recipes for a few special crafting operations (but the recipes themselves should work fine):
  * Drone colouring (drone + dye)
  * One Probe helmet (Pneumatic Helmet & Probe)
  * Minigun potion ammo (regular ammo + any potion)
  * Crafting Patchouli guide (book + compressed iron ingot)
  * Drone upgrade crafting (any basic drone + Printed Circuit Board)
* Mouse-wheel scrolling GUI side tabs with scrollbars doesn't work if JEI is active and has icons on that side of the GUI
  * Mousing over the scrollbar itself and scrolling should work
  * JEI issue 

### New
#### Recipes
* Recipes in general use a lot less iron, especially in the early game.  However, more stone will be required (so it's worth smelting up a stack or two of cobblestone before you start on the mod).
  * Added a collection of Reinforced Stone blocks (bricks, slabs, pillars, walls...), made with stone and (a little) Compressed Iron.  
    * These are both good for building with (high blast resistance), and are used in many recipes to reduce compressed iron requirements.
* For modpack makers: all machine recipes are now loaded from datapacks (`data/<modid>/pneumaticcraft/machine_recipes/<machine_type/*.json`) so can be easily overridden and reloaded on the fly with the `/reload` command.  To remove an existing recipe, simply create a JSON file of the same name in your datapack with an empty JSON document: `{}`
  * You can see all default recipes at https://github.com/TeamPneumatic/pnc-repressurized/tree/1.14/src/main/resources/data/pneumaticcraft/pneumaticcraft/machine_recipes
#### Machines
* Pressure Chamber
  * The unloved Pressure Chamber Interface filter system is gone.  The Interface will now only pull crafted items (there is a button in the Interface GUI to pull everything in case the chamber needs to be emptied).
  * The Pressure Chamber Interface also accepts a Dispenser Upgrade; if installed it will eject items into the world if there is no adjacent inventory.  (Note that the interface still auto-pushes items; no need to pull items from it).
  * Several new default recipes, including ways to make slime balls, ice, packed ice, and blue ice
* Refinery
  * There is now a separate Refinery Controller block in addition to the four Refinery Outputs
  * Refinery Outputs can be stacked beside or on top of the Refinery Controller
  * Outputs only output fluid, never accept it (except from the Controller)
  * Controller only accepts input fluid (Oil by default)
* Thermopneumatic Processing Plant
  * Can now take recipes which produce an item output in addition to or instead of a fluid output
    * One such recipe is added by default: an Upgrade Matrix taking Lapis and Water, which is used to craft upgrades in a more lapis-efficient way
* Etching Tank
  * This is a new machine which replaces the old in-world crafting of PCB's with Etching Acid
  * It's faster than the old method of etching PCB's (twice as fast by default, much faster if the tank is heated)
  * With sufficient infrastructure, this can be even faster than using the Assembly System to mass-produce PCB's (but the Assembly System remains a simpler & more convenient option)
  * Separate output slots for succesful and failed PCB's; failed PCB's can be recycled in a Blast Furnace (which also yields a little XP)
* UV Light Box
  * The GUI now has a slider to set the exposure threshold instead of using redstone emission
  * Exposed items (past the configured threshold) are moved to a new output slot, which can be extracted from using automation
* Programmer improvements
  * Fix inconsistent zoom out/zoom in behaviour
  * Now possible to merge programs from Pastebin, clipboard or saved items
* Gas Lift
  * Now uses new Drill Pipe block instead of Pressure Tube
  * Drill Pipe is a non-tile entity block so it's also suitable for decorative building
#### Plastic
* Coloured plastic is gone, and so has the Plastic Mixer.
  * There is now only one type of plastic: the Plastic Sheet.
    * Which also means there's only one type of Programing Puzzle Piece, making Drone programming a lot easier.
  * You can make Plastic Sheets by pouring a bucket of Molten Plastic (made in the Thermopneumatic Processing Plant from LPG & Coal as before) into the world.  It will solidify after 10 ticks.
  * Alternatively, put a bucket (or tank) of Molten Plastic in an inventory with a Heat Frame attached, and chill the Heat Frame as much as possible (-75C is optimum) for bonus Plastic Sheet output; up to 1.75x.
  * New Plastic Construction Brick™, made from plastic and any dye. Can be used for building; they also damage any entity that steps on them without footwear.
#### Semiblocks 
* Semiblocks are now entities. No direct gameplay effect, but rendering and syncing of semiblocks should be far more robust now.
  * Semiblocks include crop supports, logistics frames, heat frame, spawner agitators and transfer gadgets.
#### Storage
* Added Reinforced Chest - a 36-slot, blast-proof inventory which keeps its contents when broken
* Added Smart Chest - a 72-slot blast-proof inventory which keeps its contents when broken
  * Smart Chest can also filter each slot to only hold a certain item
  * And close one or more slots, effectively giving the chest a configurable inventory size
  * *And* push and pull items to adjacent inventories
  * *AND* eject items like a dropper and absorb nearby items like a vacuum hopper
  * Super powerful, but not the cheapest to make (needs a PCB)
* Added three new fluid storage tanks, imaginatively title the Small Fluid Tank (32000mB), Medium Fluid Tank (64000mB) and Large Fluid Tank (128000mB)
  * Denser storage than the Liquid Hopper
  * Can be stacked vertically, and combined (with a wrench) into a sort-of-multiblock
  * Has a GUI where items can be inserted to transfer fluid, e.g. buckets, liquid hoppers, memory sticks (see below)
#### Items & tools
* Camo applicator right-click behaviour changed a little
  * Right click any non-camo block to copy its appearance
  * Sneak right click to clear camo
  * Right click any camo block to apply (or remove) camo
* Vortex Cannon
  * Reduced air usage by half
  * Increased crop/leaves breaking range from 3x3x3 to 5x5x5
  * Can now also break webs (very efficiently)
* Memory Stick (new)
  * Can be used to store and retrieve experience
  * Added a new Memory Essence fluid which is liquid experience (20mB = 1 XP point)
  * Memory Essence can be extracted from a Memory Stick via Liquid Hopper or any machine that can pull fluid from an item (e.g. the new tanks mentioned above)
  * Memory Stick will go in a Curio slot
  * Memory Essence is supported by the Aerial Interface
* Minigun
  * Freezing Ammo now creates a cloud (like a potion cloud) instead of "ice" blocks.  The cloud will freeze and damage entities in it.
#### Heat System
* Added new Heat Pipe block, perfect for transferring heat from one place to another.
  * Heat Pipe loses no heat to adjacent air or fluid blocks.  Consider it a much more compact alternative to lines of Compressed Iron Blocks surrounded by insulating blocks.
  * Heat Pipe can be camouflaged to run through walls/floors, and is waterloggable.
* Heat Frame Cooling expanded a little
  * Fluids to be cooled can now be provided with tanks, not just buckets (assuming the tank item provides a fluid handler capability)
  * Recipes can have a bonus output based on the temperature (new Molten Plastic -> Plastic Sheet recipe does this, but the old lava->obsidian and water->ice recipes do not)
* Campfire is recognised as a heat source, and is better than lighting a fire on netherrack.
#### Amadron
* Significant internal rewrite to hopefully fix the various syncing problems encountered in 1.12.2.
* Villager trades work much as before, but with support for trade levels: higher level trades will appear much more rarely in the random offers list.
* Player->player trades are still added via the tablet GUI, and are stored in `config/pneumaticcraft/AmadronPlayerOffers.cfg`.
* All static Amadron trades are now loaded from datapacks (`data/<modid>/pneumaticcraft/amadron_offers`) so are much easier to modify or disable.  Because of this, adding static/periodic trades via the tablet GUI is no longer a thing.
  * See https://github.com/TeamPneumatic/pnc-repressurized/tree/1.14/src/main/resources/data/pneumaticcraft/pneumaticcraft/amadron_offers for the default offers
  * Default offers can be disabled by simply using an empty JSON document `{}`.
#### Villagers
* You might get lucky and find a Pneumatic Villager house when exploring (there are different houses for each of the five village biomes)
  * These houses have a couple of basic PneumaticCraft tools and machines, and a chest with some handy loot
* The Pneumatic Villager (Mechanic) has a massively expanded trade list, with some nice trades at higher levels
  * Villager point-of-interest is the Charging Station, so unemployed villagers can become Mechanics
  * Because of this, the old way of creating mechanics in the pressure chamber no longer works
#### Drones
* Added two new basic (non-programmable) drones:
  * Guard Drone: attacks any mobs in a 31x31x31 area around where it's deployed
  * Collector Drone: collects nearby items (17x17x17) and puts them in an inventory that it's deployed on or adjacent to. Has some basic item filtering functionality. Can take range upgrades to expand the item collection range.
#### Logistics
* Added the ability to filter by mod ID
* Added support for the Tag Filter item, a way to filter by item tags (the replacement for ore dictionary matching)
  * Use a Tag Workbench (new block) to create Tag Filters
* Reduced the amount of air used to transport items & fluids, and made the amount adjustable in config (see "Logistics" section in `pneumaticcraft-common.toml`)
#### Upgrades
* Max upgrade amounts are now hard-enforced in machine & item GUIs
  * e.g. if a machine takes a max of 10 Speed Upgrades, it is now impossible to put more than 10 Speed Upgrades in the machine (unlike in 1.12.2 where any number could be added but only 10 were used).
* Volume upgrades have changed a bit
  * Max volume upgrades in anything is 25.
  * They have diminishing returns as more added: 1 upgrade will double the base volume, 4 upgrades will quadruple it, 25 upgrades will increase the volume by a factor of 10.
* New upgrades:
  * Jumping Upgrade - replaces Range Upgrade in Pneumatic Leggings
  * Inventory Upgrade - replaces Dispenser Upgrade in Drones
  * Flippers Upgrade - for Pneumatic Boots, swim speed increase
  * Standby Upgrade - for Logistics & Harvesting drones; allows them to go on standby when idle, saving air
  * Minigun Upgrade - replaces Entity Tracker Upgrade in drones  
* Some upgrades are now *tiered*, meaning there is a different crafting recipe for each tier. 
  * Jet Boots Upgrade and Jumping Upgrade are examples of such upgrades.
#### Tube Modules
* Regulator Module now by default regulates to 0.1 bar below tube pressure (4.9 for basic tubes, 19.9 for advanced tubes)
  * A full redstone signal will reduce the regulation amount to 0 (acting like a shut-off valve), intermediate signal levels interpolate the regulation level
  * With an Advanced PCB installed, fine-grained control is available as it used to be
* Safety Valve Module now leaks air at 0.1 bar below tube pressure (4.9 for basic tubes, 19.9 for advanced tubes)
  * Again, adding an Advanced PCB allows fine-grained control, as it used to
* Redstone Module will now take input from a Pressure Gauge module on the same tube section
* Tubes with inline modules (the Regulator and Flow Detector) can *only* be connected to on the two ends of the module
  * no more connecting to a Regulator module on the side (which never worked properly anyway) 
#### Misc
* Added a Display Table, a simple one-item inventory which displays its held item. For both aesthetic and possible automation purposes.
* Fuels now have a burn rate multiplier in addition to a "total air produced" amount
  * Diesel burns more slowly now, but produces slightly more air overall
  * LPG burns a little faster, same overall production
  * Gasoline burns significantly faster, same overall production
  * Kerosene is unchanged
* Kerosene Lamp uses much less fuel now, especially at larger ranges
* Lots of general GUI cleanup and polishing
* Updated textures for many items
* Fluid-containing items (Liquid Hopper, tanks, machines...) now show their contained fluid while in item form
* Pressure Tubes are now waterloggable
#### Mod Integration
* Currently patchy compared with 1.12.2, but the following mods are currently supported:
  * The One Probe
  * WAILA/HWYLA
  * JEI
  * Patchouli (the guide book has been updated to reflect all the new changes)
  * Curios
* More mod support will be added in future, but this is not likely to happen until the 1.15.2 port.
