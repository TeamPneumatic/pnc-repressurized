# PneumaticCraft: Repressurized - Changelog

This is an overview of significant new features and fixes by release.  See https://github.com/TeamPneumatic/pnc-repressurized/commits/master for a detailed list of all changes.

Changes are in reverse chronological order; newest changes at the top.

## Minecraft 1.15.2

## 1.2.1-?? (unreleased)

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

## Minecraft 1.14.4

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