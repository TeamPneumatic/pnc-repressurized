# PneumaticCraft: Repressurized - Changelog

This is an overview of significant new features and fixes by release.  See https://github.com/TeamPneumatic/pnc-repressurized/commits/master for a detailed list of all changes.

Changes are in reverse chronological order; newest changes at the top.

## Minecraft 1.12.2

### 0.11.1-??? (unreleased)
#### Fixes
* Fixed Plastic Mixer allowing items to be piped into the output slot.
* Fixed clicking the tank in the Liquid Compressor GUI causing the machine's redstone mode to be changed.
* Fixed problem where Plastic Mixer "Item Selection" GUI side tab could sometimes be too small (depending on screen resolution), causing buttons to be outside the tab's area, and unclickable.
* Fixed Chestplate upgrades being missing in charging station upgrade GUI when Tough As Nails not installed.
### 0.11.0-357 (20 May 2019)
#### New
* Added the Redstone Module, a tube module that allows redstone signals to be transmitted along pressure tubes.
  * Toggle a module between input and output by right-clicking with a wrench.
  * 16 independent channels are available; right-click with a dye to set a module's channel (the dye will not be used up by default, but see ``useUpDyesWhenColoring`` config setting)).
  * Add an Advanced PCB to the module to gain some extra signal processing, e.g. integrated logic gates, timers, toggle latches, comparators...
* Tough As Nails support:
  * PneumaticCraft heat (and cold!) sources will have an effect on your body temperature. Take care!
  * Added the Air Conditioning Upgrade for the Pneumatic Chestplate.  This will use pressure to try and regulate your body temperature, protecting from Tough As Nails temperature extremes.  Up to 4 upgrades can be added (although the max of 4 would only be needed for the most extreme conditions).
  * See ``tanHeatDivider`` and ``tanRefreshInterval`` config settings.
* Added the Thermal Compressor, a machine which converts temperature differences on opposite sides directly to air pressure.
  * Heat will attempt to equalize across sides, so effort is needed to maintain a temperature gradient across the compressor.
  * See ``thermalCompressorThermalResistance`` config setting.
#### Updates
* Plastic Mixer CraftTweaker support has been heavily reworked:
  * There is now far more extensive CraftTweaker support, allowing melting or solidifying more than just plastic. E.g. you could add a recipe to convert Thermal Foundation Rockwool to/from Blazing Pyrotheum.
  * Fluid->solid ratio (mB per item) can be defined on a per-recipe basis.
  * Conversions can be melt-only, solidify-only, or both melt & solidify.  E.g. you could add a recipe to melt Lapis into IC2 Coolant, but not allow resolidification.
  * Since the Plastic Mixer also colourizes items/blocks, this is primarily intended for those which can have one of 16 colours (Wool, Concrete, Terracotta...). However, melting-only recipes (such as Lapis->Coolant) will work.  Trying to solidify to items/blocks which aren't colourizable does work, but still uses dye.
  * See https://github.com/TeamPneumatic/pnc-repressurized/issues/343 for CT docs until https://crafttweaker.readthedocs.io/en/latest/#Mods/PneumaticCraft_Repressurized/PneumaticCraft_Repressurized/ is updated.
  * This breaks the CT methods added in 0.10.4 - sorry!  But the new system is far more powerful.
* General Plastic Mixer changes:
  * Quality of Life: The item selection GUI side tab now auto-opens if the Plastic Mixer has fluid in the tank
  * If there's no fluid in the tank which can be solidified, the item selection side tab now hides the buttons (since they don't do anything).  The Lock Selection checkbox remains usable, though.
  * The misleading GUI arrow from input item to output item is gone, replaced by arrows from the input item to the fluid tank, and from the fluid tank to the output item.
  * The above arrow has been replaced by a small "book" icon, which can be used to show all recipes for the Plastic Mixer in JEI.
  * The Item Temperature gauge now shows the required temperature to melt an item in its tooltip, if applicable.
* When moving Pneumatic Armor stat windows, the move GUI now shows other open stat windows too (with a grey border) to make it easier to line things up, avoid overlaps etc.
* Performance: tile entity data syncing now causes far fewer block/chunk updates, which should improve performance in general.
* Performance: player jet boots particles are now all handled client-side; only player jet boots state needs to be synced from server to clients when it changes - much less network traffic.
* Performance: crop sticks growth particles are now done client-side, reducing server->client traffic.
* Crop stick growth tick chance is now configurable: see ``cropSticksGrowthBoostChance``.
* Custom air particle density (used in many places: pressure chamber, leaking tubes, jet boots...) now respects the clientside particle density setting.
* Dyed Aphorism Tiles now keep their colours when the block is broken.
  * Added a self-craft recipe which resets the colour of a dyed Aphorism Tile item.
* Made drone area sorting more deterministic (see https://github.com/TeamPneumatic/pnc-repressurized/issues/342).
* JEI now shows an info tab for all items & blocks which have tooltip information (same info as the item tooltip is shown here - just another way to view it)
* Dyeing drones no longer uses the dye by default (but see ``useUpDyesWhenColoring`` config setting)
#### Fixes
* Restored the Thermopneumatic Processing Plant "Dump Input Tank" button. Was a mistake to remove that.
* Added missing Patchouli docs page for the "Pick Up Item" programming widget.

### 0.10.5-346 (29 Apr 2019)
#### Fixes
* Hotfix: fix crashes due to null ItemStack field in Charging Station tile entity (manifested either as server crashes on world load or client crashes when rendering the Charging Station charged item)

### 0.10.4-343 (28 Apr 2019)
#### New
* Added a Reinforced Air Canister, which can hold up to 120000mL of air at 20 bar. These can be used (with a little design) as a wireless pressure transfer mechanism using a couple of Aerial Interfaces & Charging Modules and a (vanilla) Ender Chest...
#### Updates
* Plastic Mixer can now be configured to accept alternative input fluids, either via Java API or via CraftTweaker.
  * Ratio of liquid to solid plastic sheets can be defined on a per-input basis too.
  * CT docs will be added to https://crafttweaker.readthedocs.io/en/latest/#Mods/PneumaticCraft_Repressurized/PneumaticCraft_Repressurized/ but see https://github.com/TeamPneumatic/pnc-repressurized/issues/326 for now.
* Thermopneumatic Processing Plant updates:
  * Thermopneumatic Processing Plant now runs up to 2.5 times as fast if given more heat than the minimum recipe requirement; heat will also be consumed more quickly, but it's slightly more efficient in terms of heat usage to run the plant hotter than the minimum.
  * Thermopneumatic Processing Plant now emits smoke particles when running.
  * Thermopneumatic Processing Plant GUI now shows required temperature in heat gauge tooltip.
* JEI version 4.12.0 or later is now required.
* Refinery smoke particles are now all handled client-side, reducing server->client traffic when the Refinery is running.
* Pressurizable items now draw their durability bar in a light blue colour (darkening as the pressure decreases). Since the bar is showing air and not actual damage, this differentiates it from a normal item damage bar.
  * The pressure bar is now always shown on pressurizable items, even when full. If you prefer the old behaviour, set the clientside ``alwaysShowPressureDurabilityBar`` config setting to false.
  * Pneumatic Armor now shows the new-style pressure bar in addition to the existing durability bar.
* Pneumatic Chestplate Magnet Upgrade will no longer pull unfinished PCB's from Etching Acid pools.
* Pneumatic Chestplate Security Upgrade now provides electrical protection from Immersive Engineering wiring. You will not be injured, but air will be used from the chestplate to provide this protection. Knockback is *not* prevented.
* Similarly, a Security Upgrade in a Drone will protect it from IE wiring, also at an air cost. Drones didn't get knocked back by the shock in any case. 
#### Fixes
* Fixed crash in Programmable Controller when running a program which refers to the ``$owner`` special variable. The Programmable Controller's owner's (head) position is now returned, as expected.
* Fixed Thermopneumatic Processing Plant sometimes forgetting what it was supposed to be doing.
* GPS Area Tools may now be used when setting an area widget's position via the inventory search GUI (previously this caused a crash). Note that when the area tool contains multiple positions, an arbitrary position will be selected.
* Aerial Interface now properly enforces minimum pressure requirements for moving items in & out.
* Fixed item dupe issue relating to Drones with Magnet Upgrades in certain circumstances.
* Fixed excessive packet sending causing FPS drops with Charging Station and UV Light Box when items were being processed.

### 0.10.3-337 (6 Apr 2019)
#### New
* Experimental Immersive Engineering heat integration
  * Immersive Engineering External Heater will supply heat to PneumaticCraft machines (Refinery etc.) if given RF (Forge Energy)
  * By default it uses 100RF/t to provide 1 heat unit; this can be changed in config (``pneumaticcraft.cfg`` -> "integration" section) 
  * A redstone signal to the heater will stop it providing heat to PneumaticCraft (but it will continue to charge RF)
  * Can be disabled entirely by setting ``ieExternalHeaterRFperTick`` to 0 in config
  * NOTE: highly-experimental and subject to possible significant retuning for balance purposes
* Experimental Mekanism heat integration
  * PNC and Mekansim machines will now exchange heat with each other
  * Heat exchange properties can be configured in ``pneumaticcraft.cfg`` -> "integration" section
  * Can be disabled entirely bet setting ``mekHeatEfficiency`` to 0.0 in config
  * NOTE: should be considered even more experimental than the Immersive Engineering integration!  Heavily subject to possible rebalancing.
* Aerial Interface now has Baubles support
  * Any FE (RF etc.) items in your Baubles slots will now be charged by the Aerial Interface
  * The GUI Side Configuration tab now also has the ability to connect a face of the block to your Baubles inventory, if available
* Added a Creative Upgrade item, for use in the Omnidirectional and Liquid Hoppers.  This allows them to infinitely dispense items/fluids.
  * A liquid hopper with a Creative Upgrade will also act as an infinite fluid sink if it receives any fluid that it already contains
* Dispenser Upgrade can now be configured with a direction
  * Right-click any block to set the direction to the clicked face, right-click air to clear the direction
  * This is used by the Gas Lift and Thermopneumatic Processing Plant to enforce a specific direction to eject fluids to
  * If no direction is defined, outputs are searched in D-U-N-S-W-E order, as before.
#### Updates
* Got rid of the "Dump Input" button from the Thermopneumatic Processing Plant GUI.  This isn't necessary anymore, since the TPP doesn't accept fluids that it can't process.
#### Fixes
* Fixed another startup crash related to BlockHeatProperties.cfg (so many edge cases...) - this one occurred with fluids that don't have an associated block.
* When hiding the GUI (with F1 by default), Pneumatic Armor HUD is now also properly hidden
* Fixed minor problem with Air Grate vacuuming mode where items could get caught on edges, e.g. farmland->block transition
* Slightly increased Air Grate vacuum mode range for adjacent inventory insertion (items could get stuck just outside insertion range)

### 0.10.2-332 (30 Mar 2019)
#### Updates
* Air grate module now has slightly better range for inserting vacuumed items into an adjacent inventory
* Plastic Mixer: liquid to solid plastic ratio can now be configured in pneumaticcraft.cfg, Machine Properties section
#### Fixes
* Fix another potential startup crash related to new heat system and certain fluids
* Fixed some rendering glitches related to Transfer Gadgets and Heat Frames
* Fixed Plastic Mixer item heat gauge sometimes showing stupid values

### 0.10.1-330 (28 Mar 2019)
#### Fixes
* HOTFIX: fix crash on startup related to new heat system when mods referenced in BlockHeatProperties.cfg are not installed.

### 0.10.0-328 (28 Mar 2019)
#### New
* Lots of work on the Logistics system:
  * Very major performance improvements for Logistics, both for Logistics Modules and Logistics Drones. Smarter caching of discovered logistics frames means a large reduction in server CPU used.
  * Logistics frames now have a facing direction, which tells Logistics Drones which side to access the framed inventory on. Not important for non-sided inventories like chests, but very important for sided inventories such as Furnaces or the Aerial Interface.
    * The facing direction defaults to the clicked face of the inventory it's placed on. Logistics frames placed before this change will face up by default; you can adjust the facing from within the GUI with the new "Facing" side tab.
    * Logistics Modules ignore the frame's facing and always use the side of the inventory they face.
  * Requester Frames can now specify the minimum amount of items or fluid to transfer at a time. This avoids situations where Logistics Drones could end up making constant trips moving 1mB of fluid at a time (e.g. moving fuel from a Refinery output to a Liquid Compressor).
* Significant overhaul of the Heat system:
  * Block heat properties can now be specified in the ``config/pneumaticcraft/BlockHeatProperties.cfg`` config file. Modpack makers, you're free to edit this file - entries you've changed won't be overwritten by future mod updates, although new entries could be added. (Note that this will likely become a datapack if & when I port to MC 1.13/1.14).
  * Several other mod blocks and fluids now have useful heat properties, for heating or cooling PneumaticCraft machines:
    * IC2 and Immersive Engineering Uranium blocks are not very hot but will last for ages before turning into Lead (not 4.46 billion years, though)
    * IC2 Coolant & Hot Coolant are highly effective for heating & cooling purposes
    * IC2 Steam & Superheated Steam are also useful, though not as good as Coolant
    * Natura Heat Sand will emit a decent amount of heat before turning to Sand
    * Thermal Foundation Enderium Blocks are unnaturally cold and will absorb a lot of heat before turning to Platinum
    * Cryotheum is still an excellent coolant but now turns to Snow instead of Stone
    * Quark blaze lamps are good heat sources, and will turn to Glowstone blocks
    * Quark brimstone and permafrost are decent heat sources - not particularly hot/cold, but will last a while, and will turn to Cobblestone
  * If you add a modded block or fluid entry which is generally useful, I will happily add that to the default list above. Just raise a github issue or (even better) a pull request.
  * Note that *all* fluids have heat properties; a fluid's temperature is mod-defined, but its thermal resistance, total heat capacity and block transitions can all be overridden in ``config/pneumaticcraft/BlockHeatProperties.cfg``.
  * Custom block heat properties now allow for a block transition both for hot and cold (e.g. IC2 steam becomes superheated steam if too much heat is absorbed, and water if too much heat is lost). Previously only a hot *or* cold transition was possible.
  * Ambient temperature is no longer a flat 22C, but varies by biome and altitude
    * Plains biome at sea level is 27C
    * Ambient temperature drops above Y=80 and below Y=40 (by 0.1C per block by default, but configurable)
    * Temperature variations are configurable in ``config/pneumaticcraft/BlockHeatProperties.cfg`` and can be disabled entirely if you want (set "ambientTemperatureBiomeModifier" and "ambientTemperatureHeightModifier" to 0)
    * This makes biomes like Deserts great for running a Refinery, but less great for running advanced compressors (not impossible, just less efficient), and vice versa for biomes like Taiga or Extreme Hills.
#### Updates
* Charged and active Pneumatic Boots will no longer trample farmland.
* Some Patchouli guidebook additions and improvements. Added missing page for the Logistics puzzle piece.
* The Refinery, Thermopneumatic Processing Plant and Plastic Mixer now warn in their GUI if the block is poorly insulated, and thus wasting heat (Plastic Mixer only warns if you're trying to melt down plastic).
* The Plastic Mixer now remembers any dye in its internal buffers if the block is broken and put down again, avoiding dye wastage if you need to move the machine.
* Added "/dumpNBT" command (op level), which dumps the NBT of the currently-held item as a JSON string. Primarily intended for getting internal Forge fluid names from a bucket of the fluid, for adding to custom fluid properties to ``config/pneumaticcraft/BlockHeatProperties.cfg``, but is also generally useful for debugging purposes.
* Pressure chamber textures don't look quite as flat now.
#### Fixes
* Fixed two or more Transfer Gadgets on one block causing messy breakage.
* Fixed Refinery & Thermopneumatic Processing Plant GUI's wrongly reporting insufficient temperature even if the temperature is fine and machine is running properly.
* Programmer GUI: Fixed middle-click on rightmost column of expanded widget tray closing the tray instead of opening docs for the clicked widget
* Added a couple of missing Patchouli docs pages for programming widgets.  A few other Patchouli docs fixes & updates too.
* Logistics frame info is now properly shown by The One Probe on dedicated server (previously just said "Error") 
* Semiblocks (logistics frames, heat frames, etc.) no longer render for blinded players unless they're close enough to see the block the semiblock is on.
* Fixed Pneumatic Armor pieces not booting up on login if player isn't wearing the Pneumatic Helmet (this was due to the "master switch" being installed in the helmet - a holdover from when the helmet was the only armor piece).
* Fixed minor problem where placing a pressure chamber wall against another pressure chamber wall didn't play the block-place sound.
* Scaled down 8 programming widget textures with excessively large (256x256, ouch) textures sizes, saving a good chunk of texture atlas space.

### 0.9.4-1 (4 Mar 2019)
#### Fixes
* Hotfix release: fix fluid dupe bug in Liquid Hopper

### 0.9.3-324 (4 Mar 2019)
#### Fixes
* FOV modifications (as done by Pneumatic Leggings with Speed Upgrades and Minigun with Entity Tracker Upgrades) should now cooperate much better with other mods, assuming those mods do their FOV calculations correctly too (see https://github.com/TeamPneumatic/pnc-repressurized/issues/304).
* Programmer GUI: Coordinate Operators will now report divide-by-zero attempts as an error instead of silently ignoring them.
* Fixed Thermopneumatic Processing Plant recipes not showing their temperature in JEI.
* Jet Boots Builder Mode speed modification should work better now in conjunction with other mods that modify dig speed (e.g. Aerial Affinity enchant).

### 0.9.2-321 (16 Feb 2019)
#### New
* If you have 8 or more Jet Boots Upgrades installed, it is now possible to switch to Jet Boots "Builder Mode"
  * This allows more creative-like flight (where holding Space rises), but much slower movement
  * Dig speed is improved while in the air and in Builder Mode (need 10 Jet Boots upgrades for full normal dig speed)
  * Ideal when building and fine adjustment is needed.  Normal mode is still much better for fast travel.
#### Updates
* Pressure Chamber: interface doors now animate open and closing (like they did in 1.7.10)
* ComputerCraft/OpenComputers improvements:
  * Significant performance improvement work
  * Some thread-safety fixes
  * Drone Interface now lights up green when a drone is connected
* Albedo support for the UV Lightbox has been dropped (could no longer reach the Maven repo)
* Chestplate Magnet now checks for items every 4 ticks instead of 8.
* Some performance improvements:
  * Thermopneumatic Processing Plant will use less server CPU when idle (smarter recipe checking)
  * Charging Station will use less client CPU to render (static model instead of TESR to render charge pad upgrade) and process (was running some code that's only necessary server-side)
  * Less network traffic for tile entity sync (machines no longer sync all their upgrades to clients, only those of specific interest)
* Item & Block tooltips are now a little more consistent & informative in a few places
* Minor GUI improvements for the Charging Station (primarily less cramped-looking)
* GUI pressure gauges use a smaller text size for the numbers around the edge of the gauge
* Entity/Block tracker & hacking: the popup "can hack" window now also notes which hotkey is used for hacking
* Patchouli guidebook is now advancement-gated, so only relevant sections of the book are displayed depending on how far you've progressed through the mod.
* Remote Editor: Spruced up the GUI a little. Added a snap-to-grid option to make it easier to line up widgets on the GUI.
* Universal Sensor: Added a "Global Analog Variable" sensor, which emits an analog (0..15) redstone signal based on the X value of the linked global variable. This is in addition to the existing "Global Variable" sensor which emits 15 if the linked variable is non-zero, and 0 otherwise.
#### Fixes
* Fixed problem where machines could become unbreakable under some circumstances (related to trying to sneak-wrench a machine which had upgrades installed or other data that needs to be saved to the dropped item stack).
* Refinery comparator support: "has work" now correctly emits a signal when Refinery isn't up to temperature (the check is "has work", not "did work"). This makes it more useful for enabling/disabling a heat source for the Refinery, depending on if it has work to do.
* Fixed Remote Editor GUI not being able to modify the dimensions of button widgets.
* Added missing Patchouli docs page for the Remote.

### 0.9.1-317 (20 Jan 2019)
#### New
* NOTICE: If you use Applied Energistics 2, then rv6 is now required.  AE2-rv5 is no longer supported.
* A Patchouli (https://minecraft.curseforge.com/projects/patchouli) manual has been added. IGWmod remains supported for now (a decision on future docs directions has yet to made).
#### Updates
* Omnidirectional Hopper can now take a Dispenser Upgrade to auto-eject items into the world at its output side (when there isn't an inventory there); this makes for an excellent replacement for the vanilla Dropper. Can be disabled in config (see 'B:omniHopperDispenser').
* Omnidirectional Hopper should be more efficient on server CPU usage now when many speed upgrades are installed.
* Pneumatic Boots: Jet Boots air usage is now configurable (see 'I:jetBootsAirUsage').
* Pneumatic Door now renders the pneumatic cylinder connecting the door and door base.
* Blocks launcher via Chestplate Launcher now respect claim protection (no placing blocks in protected claims by firing them in there - blocks will drop in item form if you try).
* Pneumatic Armor: Speed Upgrades have a slightly less marked effect on armor start-up time now (start-up time is 200 * 0.8^n ticks, where n is number of installed Speed Upgrades; it used to be 200 / (n+1) ticks).  Also, base start-up time is now configurable (see 'I:armorStartupTime').
* Universal Sensor GUI has had a minor polish.
#### Fixes
* AE2 Integration should be much more reliable now (fixed problems with Logistics Drones sometimes overfetching requests from AE2)
* Fixed Sentry Turrets losing their contents & filters on reload due to an exception thrown when reading NBT.
* Fixed Refinery not running in some situations (failing to search for a recipe), e.g. when placing down a Refinery block which already holds some Oil.
* Fixed blocks launched via Chestplate Launcher not rendering clientside when connected to dedicated server.
* Fixed bug in Pneumatic Helmet where SCUBA could be usable without a SCUBA upgrade installed.

### 0.9.0-310 (28 Dec 2018)
#### New
* Major Minigun enhancements!
  * Minigun now has a 4-slot inventory for ammo, saving valuable player inventory slots.  The Minigun will only draw ammo from these slots.
  * Sneak & Right-Click the Minigun to open the inventory and load ammo
  * Ammo slots can be "locked" by middle-clicking a slot; when a slot is locked, the Minigun will *only* pull ammo from that slot.  Useful when multiple ammo types are loaded.
  * Added several new ammo types:
    * Armor-Piercing: higher base damage, ignores armor, 250 shots per cartridge
    * Incendiary: sets fire to targets, 500 shots per cartridge
    * Freezing: slows targets, has chance to encase in damaging "ice", 500 shots per cartridge
    * Weighted: very short range, high damage, 250 shots per cartridge
    * Explosive: shots have chance to cause explosions (terrain damage off by default), 125 shots per cartridge
    * Plain ammo remains as before, and is the only ammo type which can be potion-tipped
  * Minigun can now take upgrades (use a Charging Station to install):
    * Speed Upgrades: reduces spin-up time, chance to fire multiple rounds per shot, increased air usage
    * Range Upgrades: increase range from 50 blocks to up to 80 blocks (5 blocks per upgrade), increased air usage
    * Entity Tracker Upgrades: zooms in while firing
    * Dispenser Upgrades: increase chance of all ammo types (including regular ammo with potions) proc'ing their effect, greatly increased air usage
    * Item Life Upgrades: slowly replenish ammo when gun is in inventory for significant air usage
    * Security Upgrade: prevent damage to pets and players
* Added Micromissiles: hand-held missile pods which can fire guided missiles with an explosive payload
  * 100 missiles per pod (by default; configurable)
  * Can configure missiles to balance between top speed, turn speed and damage
  * Missiles will lock onto to nearest valid target; can filter targets with entity filters as used in other PneumaticCraft blocks & items
  * Dumb-fire mode where missiles just fly in a straight line; slower but higher damage
  * Missiles have a configurable launch cooldown (20 ticks by default)
* Pneumatic Chestplate now takes up to 4 Dispenser upgrades to enable an item launcher:
  * Press & release the launch hotkey (default: Control + C) to charge the launcher and fire items/blocks
  * Full charge takes 15 ticks
  * Some items (arrows, eggs, TNT...) have special behaviour, similar to how a dispenser would operate
  * Other items will be simply fired as item entities
  * Other blocks will be fired as "tumbling block" entities, which try to reform as a block on contact with any other block. This allows torch launching functionality and other remote block placing, for example.
#### Updates
* Programmer GUI improvements:
  * Added a hi-res version of the GUI, used when the (scaled) resolution is 700x512 or higher.  This hi-res GUI gives a much larger area for programming widgets.
  * The GUI will now auto-recentre on the Start widget when a program is loaded from an item (drone, network storage) or from Pastebin, or if all widgets are off-screen when the GUI is re-opened.
  * When the widget tray is open, middle-click can now also be used to open IGW docs for puzzle pieces (pressing 'I' still works, but the filter field has focus when the tray is open so the mouse is more convenient)
* Entity Filter strings can now be prefixed with a "!" to negate the check (e.g. "!zombie" means "anything except zombies")
* Entity Filter handling is now a bit more efficient in general (pre-parsing the filter string wherever possible, so less string processing)
* It is now possible to raise or lower the thermal resistance of blocks (e.g. magma, fire, etc) in config - see D:blockThermalResistanceMultiplier. Note that the thermal resistance of fluids is already configurable, via I:fluidThermalResistance.
* CraftTweaker: it is now possible to add Refinery recipes specifying a minimum temperature at which refining starts, with a new CT method "addRecipe(int minTemp, ILiquidStack input, ILiquidStack[] outputs)" . The existing addRecipe() method still works with a default minimum temp. of 373K.
* The Pneumatic Chestplate Magnet upgrade now also works on XP orbs.
* Camo Applicator now shows particle effects when applying/removing camo from a block.
* Recipes requiring vanilla chests now accepts any oredicted "chestWood" chests.
#### Fixes
* Fixed Minigun Drone sync issue: minigun not orienting toward targets and not display bullet traces when firing
* Fixed bug where players sometimes take damage from jumping with Pneumatic Leggings + Range Upgrade (any fall damage from such a jump is supposed to be cancelled)
* Fixed Pneumatic Door dropping an item when breaking top half in creative mode
* Fixed messy block break particles when breaking a Pneumatic Door in survival mode
* Fixed crash when Pneumatic Door rotation is messed up (by other mods' rotation code)
* Fixed client crash when opening Refinery GUI while holding certain specific fluid containers.
* Fixed some missing (black/purple) particle textures for some Assembly machines and the Pressure Chamber wall.

### 0.8.4-303 (25 Nov 2018)
#### Updates
* The Kerosene Lamp can now hold 2000mB of fuel (up from 1000mb). This makes it practical to use fuel buckets to automatically fuel it (because the lamp reduces its lighting and fuel consumption when very low on fuel, it would previously run for ages on low lighting before exhausting its fuel supply and accepting a new bucket).
* Pneumatic Helmet Entity Tracker mob targeting warnings are now all handled server-side, reducing network traffic requirements and also resolving a client-side crash with certain modded entities (Ice & Fire Gorgons in particular, but potentially others).
* Restored some pre-0.8.0 functionality: the Aerial Interface once again allows access to the player's armor via the top face even when a Dispenser Upgrade is installed (but only when the top face is connected to armor). This was technically an unintended quirk, but some players were using it.
* Improved server-side performance of the Assembly Controller (smarter about discovering its machines), and made client-side GUI diagnostic messages more informative regarding duplicate and/or missing machines.
* Logistics Modules are now much cheaper to use in terms of air cost, especially for moving fluids:
  * The air cost now increases linearly with distance instead of quadratically.
  * The overall multiplier cost for items and fluids has also been halved.
  * Example: moving 8000mB of fluid a distance of 20 blocks now costs 8000mL of air, instead of an impossible 320000mL.
  * When there isn't enough air available in the tube to move the entire requested amount of items or fluid, the module will now move what it can instead of giving up and doing nothing. This makes Logistics Modules far more capable of moving resources over longer distances.
* Drone debug messages (accessed with Pneumatic Helmet + Entity Tracker & Dispenser Upgrades) now show elapsed time since last message of that type rather than the number of messages of that type, which wasn't too useful (and could also use a lot more memory than necessary in some situations).
* Drone Place Block program piece will now overwrite replaceable blocks such as tall grass & snow layers.
* Reduced Elevator sound volume depending on the number of individual elevators in a multiblock collection (each elevator plays the sound, so multiple elevators together can be quite loud).
* Drones can now pick up & drop Minecarts and Boats (including any passengers and/or contents) using Entity Import/Export programming pieces.
* In entity filters, "@boat" now matches boat entities.
* In entity filters, "@mob" now matches any hostile entity, including Slimes and Shulkers (which were previously not included).
* Added a new "Match by Block" checkbox to the Item Filter GUI in the Programmer, which is only used by the "Dig" programming piece. This allows Drones to match blocks which never drop an item, such as Abyssalcraft's Shoggoth Ooze. So you can get your Drones to clean up that nasty stuff now.
#### Fixes
* Fixed a long-standing but rather subtle bug where Advanced Pressure Tubes only had 1000mL air volume following a world reload instead of the 4000mL they should have had.  More details:
  * Advanced Pressure Tubes are supposed to have a 4000mL volume, and they do when initially placed down. However due to a bug, upon reloading the world, their volume reverts to 1000mL (same as a basic Pressure Tube).
  * This changes resolves that problem for new Advanced Pressure Tubes placed from now on.
  * However, any pre-existing Advanced Pressure Tubes will need to be broken and replaced if you want to take advantage of their correct air volume.
  * This isn't essential and you can replace tubes at your leisure. You will have improved air storage in your tube network once your tubes have re-pressurized.
  * If you're using Logistics Modules, you will probably see the greatest benefit, since the newly fixed tubes are much more effective in moving items and fluids around due to their greater air storage.
* Fixed Vortex Cannon not always breaking plants/leaves/etc. even though the fired vortex appears to make contact.
* Fixed Vortex Cannon re-equip animation constantly being played if the held cannon is being charged from Pneumatic Chestplate or Aerial Interface.
* Fixed client crash when selecting area type in Area GPS Tool.
* Fixed client crash when selecting uninitialized GPS Tools in the GUI Inventory Search window.
* Fixed Elevators playing a spurious "elevator stop" sound to any players who log in nearby, even when the Elevator is idle.
### 0.8.3-299 (5 Nov 2018)
#### Updates
* Elevator Base now accepts up to 4 Charging Upgrades.  Each Charging Upgrade allows the elevator to reclaim some of the air spent to raise the elevator when the elevator descends again, up to a max of 60% of the air (15% per upgrade). This comes with the penalty of slower elevator descent (10% slower per upgrade).
* GUI side tabs now have a more visually appealing beveled border instead of a hard black border.  If you prefer the old appearance, set the 'B:guiBevel' clientside option to false in pneumaticcraft.cfg.
* Aerial Interface has a new informational GUI side tab making it clear whether it's currently interfacing items or food/xp.
* Aerial Interface RF charging is now significantly more efficient on server CPU.  Note however that it may take a couple of seconds to start charging items that have been newly added to your inventory, or have been moved in your inventory.
* Air Grate Module improvements:
  * Now works on entity eye position, not feet position.  If you can see it, it can see you...
  * Range display box now accurately shows the range.  It will appear for ~6 seconds when the module's range changes due to a pressure change.
  * The Module now plays occasional air particles towards entities that it's affecting.
* Added config setting 'B:liquidHopperDispenser' to control whether the Liquid Hopper accepts a Dispenser Upgrade to pull or push fluids from/to the world.  Default is true.
#### Fixes
* Fixed client crash when opening Amadron GUI, introduced in 0.8.2.
* Fixed server crash in conjunction with Quark when a Drone breaks a tool/weapon (Quark auto item restock wasn't agreeing with the Drone's fakeplayer inventory).
* Fixed some text information being omitted from GUI Redstone side tabs.
* Fixed greater-than/less-than threshold not being toggleable in Pressure Gauge GUI (with Advanced PCB installed).
* Fixed problem with Aerial Interface sometimes showing the same XP fluid types more than once in the XP GUI side tab.
* Fixed visual artifacts when rendering Forge Energy bars in GUI's (Aerial Interface, Flux Compressor...)
* Fixed Omnidirectional Hopper failing to pull from some machines (noticed with Gregtech machines)
* Fixed player sometimes being left standing on top of an elevator frame and not being "collected" by a descending elevator platform.
* Fixed Pressure Chamber Interface allowing items to be piped in/out without checking that the corresponding door was fully open.

### 0.8.2-288 (30 Oct 2018)
#### New
* Added a SCUBA Upgrade for the Pneumatic Helmet for underwater breathing and better underwater vision. It's an alternative to using the Aerial Interface, which still works; the Aerial Interface is more efficient in terms of air usage, but doesn't provide the clear vision (best option is of course to use both!)
* Added a Night Vision Upgrade for the Pneumatic Helmet. No prizes for guessing what that does.
* Added armor GUI controls for Pneumatic Leggings speed and jump upgrades, to allow the boost magnitude to be throttled back (0-100%). Useful if you want to e.g. reduce your jump height without needing to find a Charging Station to swap out upgrades from your armor.
* Electrostatic Compressor has been heavily reworked:
  * No longer triggered by real lightning strikes. This was much too open to abuse via any other mod which can spawn a lightning entity (e.g. Psi)
  * Instead, each compressor now has a chance to cause a fake lightning entity random with a 6-block circular radius of itself
  * Iron bars grid are still important - the fake lightning bolt must strike a connected grid to be effective. Although the bars must be connected, they may be up to 5 blocks above or 5 blocks below the compressor.
  * Strike chance is very low in clear weather, better in the rain, and much better in a thunderstorm
  * Strike chance can be slightly improved by adding a lightning rod: a vertical column of iron bars up to 8 blocks directly above the compressor
  * Multiple compressors can be connected to the same grid: generated pressure will be shared, as before.
  * Chiseled Iron Bars are now also recognised as valid iron bars
* Added buttons to Air Cannon GUI allowing the force to be throttled between 0% and 100%.  Allows finer control over the cannon's range than just adding Range Upgrades.  The Air Cannon will now also aim lower if it can.
* New fancier particle effects for air rendering (air leaks, pressure chamber, jet boots...)
* Heat mechanics update: it is no longer possible to repeatedly break and replace a heat source block to stop it converting to stone/obsidian. That has always been considered an exploit. Using heat source blocks (lava/magma/pyrotheum...) is still a valid approach, but you will need to supply new materials to replaced the cooled ones.
#### Updates
* Default thermal resistance for all fluids is now 500.  Previously this only applied to flowing Lava and Water (all other fluids including static lava and water had a resistance of only 10).  This means that Lava (and fluids such as Blazing Pyrotheum) is now a more effective heat source for blocks like the Refinery since they won't solidify almost immediately. This may not work for existing worlds; in this case, you can set D:fluidThermalResistance=500.0 in pneumaticcraft.cfg.
* The maximum number of Speed Upgrades in the Pneumatic Leggings has been raised from 3 to 4, for a 2x speed boost over default.
* Client-side rendering performance improvement when highlighting camouflageable blocks (while holding the Camouflage Applicator).
* To hover in-place with the Jet Boots, only 7 Jet Boots Upgrades are now required, instead of 10.
* Aerial Interface now uses much less air to keep a player's air topped up.
* Players will no longer take thorns damage when attacking (Elder) Guardians with the Minigun.
* Some more Pressure Chamber work:
  * Client-side rendering (particles and items in chamber) is skipped if the chamber multiblock has no glass blocks.
  * Particle rendering is skipped if the player is more than 16 blocks away from the chamber (specifically: the chamber's primary valve).
  * A particle effect is played when the multiblock initially forms.
  * Air particle density in the chamber now responds better to changes in chamber pressure (better syncing to clients).
  * Pressure Chamber Interface door open/close sound is now a bit louder, but also no longer repeatedly plays over itself.
* Immersive Petroleum Gasoline is now accepted as a fuel in the liquid compressors by default; it's equivalent in quality to PneumaticCraft's Fuel.
* Sneak-wrenching a pneumatic machine now preserves any stored air in the dropped block (breaking the machine with a pick still loses stored air).
* Air leak sound pitch is now somewhat dependent on the pressure of the leak (higher pressure = higher-pitched leak sound)
* FoV change (zoom out) when Pneumatic Leggings speed boost is active has been removed. Added a clientside config setting D:leggingsFOVfactor if you prefer to keep the FoV adjustment (or would like a smaller adjustment) - 1.0 (default) for no adjustment, 0.0 for maximum adjustment.
* When Elevators are moving, Elevator Frames now "nudge" the player toward the centre of the block if they're hanging off by more than a little. Still possible to force-walk off the elevator platform if you're so inclined, though.
* Elevators now only "grab" entities within their frames if the entity is with 2.5 blocks vertically of the moving platform (previous behaviour was to grab regardless of height which can be a little... startling)
* Elevator Frames now only cancel fall damage if it's onto a moving elevator platform.
* Some IGW pages have had extra or clearer information added.
* Charging Module is lighter on server CPU now (CPU usage was quite heavy when charging a player's Pneumatic Armor via an Aerial Interface).
* Redid sound effect for Jet Boots a bit (lower-pitched, also modified to be lower & quieter when underwater)
#### Fixes
* Fixed some configurable values (e.g. blocks per elevator base) not being adjustable from the default.
* Fixed a deadlock issue with the Omnidirectional Hopper.  Under certain circumstances (all slots in the hopper partially full) it could fail to find suitable items in the input inventory.
* Fixed an item dupe in the Pressure Chamber under some fairly specific circumstances (related to performance improvements in 0.8.1).
* Fixed camouflageable blocks not being breakable with a pick (only a wrench) - related to camouflage updates in 0.8.1.
* Targeting Pressure Tube modules now works correctly for TOP & WAILA information display.
* Fixed some JEI Pressure Chamber recipes not showing the correct number of ingredients for some recipes (those items added by oredict key).
* Fixed an arithmetic rounding error causing Speed Upgrades to be less effective than they should have been in the Charging Station.
* Fixed problem with the Pneumatic Armor GUI causing a client crash when opened.  I couldn't reproduce this myself, but I've had confirmation that my fix appears to have succeeded...
* Fixed cosmetic issue where Pneumatic Armor feature enable/disable message were shown even when the corresponding upgrade(s) weren't installed (the actual features were not enabled, though)
* Fixed a few cosmetic "Format Error" strings in GUI info tabs.

### 0.8.1-274 (8 Oct 2018)
#### Updates
* The Advancement tree has been greatly expanded.  Many many more advancements are now available, and some give XP rewards.
* Significant performance improvement for the Refinery: now uses far less CPU when idle, and significantly less when processing.
* Significant performance improvement for the Pressure Chamber, which was unnecessarily recalculating recipes every tick (when it only needed to recalculate when chamber contents changed).
* Pressure Chamber now plays a quiet "pop" sound when crafting occurs (not more than once per 5 ticks, and you need to be pretty close to hear it).
* Camouflaging updates:
  * Connected textures are now supported!  With thanks to Botania; I figured out how to do this by looking at the Abstruse Platform code.
  * Breaking a camouflaged block now breaks off the camouflage without breaking the underlying block.
  * Applying the same camouflage to a block twice now just plays a click and does nothing (previously it reapplied the camo block, unnecessarily using pressure from the Camo Applicator).
* Some JEI work:
  * Added support for Heat Frame Cooling recipes (by default just lava bucket->obsidian and water bucket->ice, but recipes can be added with CraftTweaker).
  * Recipes which show a pressure gauge (Pressure Chamber, Thermopneumatic Processing Plant) now have a tooltip on the gauge graphic showing the exact pressure required.
  * Thermopneumatic Processing Plant recipes which don't care about pressure (Lubricant & Plastic by default) no longer show the pressure gauge at all.
  * JEI now shows Pressure Chamber enchanting & disenchanting recipes.
#### Fixes
* Pressure Chamber GUI: "Pressure" side tab now shows correct Volume information for the chamber.
* Fixed visual bug where Pressure Tubes would appear to disconnect after a Tube Module GUI was closed.
* Fixed not being able to manually insert oil into a Refinery by right-clicking with a fluid container in hand.
* Fixed side checkboxes in RF Import/Export puzzle pieces being ignored (drones would always try to access all sides until one side succeeded)

### 0.8.0-267 (17 Sep 2018)
#### New
* Logistics frames now support fuzzy item meta and NBT matching, as well as whitelist/blacklist for filters. This can all be configured with a new side tab on the right of the logistics GUI. Default is whitelist, match meta, ignore NBT, as before.
* Shulkers can be now be hacked (Pneumatic Helmet with Entity Tracker & Security Upgrades).  Disables Shulker missile attack and (usually) forces them open.
* Some quality of life improvements to Drone Debugging (Pneumatic Helmet with Entity Tracker and Dispenser upgrades):
  * In the debug screen, any widget which has an area can be right-clicked to show the area in-world.  Right-click again to stop showing the area.
  * Added "Show Start" and "Show Active" buttons to jump the debug display to the Start widget or currently executing widget, respectively.
  * Added "Follow Active" checkbox, which when selected, will continuously jump the debug display to the currently executing widget.
* Aerial Interface and Programmable Controller now have a GUI tab to configure which sides of the block should be connected to which inventory:
  * Default for Aerial Interface is top & bottom -> armor slots, sides -> player main inventory, as before.
  * Default for Programmable Controller is bottom -> programmable slot, other faces -> fake drone inventory, as before.
  * Aerial Interface now also supports interfacing with player's offhand slot and (vanilla) Ender inventory.
  * Additionally, any face of the two blocks can be disconnected from any inventory.
  * Both blocks are now rotatable (this is required for side configuration to work properly).  Default rotation for any pre-existing blocks is North - you can rotate the blocks as normal with a wrench.
  * Both blocks have had a bit of a re-texture to make it clear where the front is.
* Explosion crafting (i.e. converting iron to compressed iron via an explosion) is now manageable with CraftTweaker:
  * Arbitrary mappings of input item (or oredict entry) to output item can now be added or removed, but the mapping is always 1:1, with a configurable (average) loss rate.
  * API docs will eventually be on https://crafttweaker.readthedocs.io/en/latest/#Mods/PneumaticCraft_Repressurized/PneumaticCraft_Repressurized/ but in the meantime see https://github.com/TeamPneumatic/pnc-repressurized/issues/108
  * The config setting I:configCompressedIngotLossRate still exists, but applies *only* to the default compressed iron ingot & block recipes added by the mod.  For general loss rate configuration, use CraftTweaker.
  * JEI display for explosion crafting has changed slightly due to the possibilty of multiple possible recipes for one output; the description text is now a tooltip shown by hovering over the little explosion icon.
#### Updates
* Speed Upgrades are now slightly less expensive to use in machines in terms of fuel usage and heat generation.  Default multiplier is now 1.65, down from 1.8.  Note that this is exponential: usage is multiplier_value^num_speed_upgrades.
* The multipliers for Speed Upgrades are now configurable, in the "machine_properties" section of the config: D:speedUpgradeSpeedMultiplier and D:speedUpgradeUsageMultiplier.
* Several crafting recipes now produce more output:
  * All logistics frames: 4 frames instead of 1
  * Pneumatic Cylinder: 2 cylinders instead of 1
  * All programming puzzle pieces: 8 pieces instead of 4
* UV Light Box progress rate is now dependent on the exposure level of the PCB it's processing: much faster at low levels, a little slower at high levels.  The overall time it takes to fully expose a PCB (10 minutes with no speed upgrades) is unchanged, but this makes for a more interesting tradeoff of processing speed vs. etching failure chance (previously there was no good reason not to expose a PCB to 100%).
* When UV Light Box is set to emit a redstone signal at a given exposure level, automated (piped) extraction is only possible when the exposure level is high enough to emit a signal.  This makes for easier automation, but note that vanilla hoppers won't work here due to redstone signal emission; use an Omnidirectional Hopper or some other item transport system.
* Entity Selector strings (as used in Sentry Turret, Drone Programs, (advanced) Air Grate Module can now be a sequence of filters, separated by ";".  If any element of the sequence matches, the filter matches.  e.g. "@mob;shulker" will match any mob or a shulker (shulkers, although hostile, are considered by Minecraft to be golems rather than mobs).
* The minimum temperature for fluids to be considered as fuels for Liquid Compressors has been raised to 373K (100C) and is now configurable (see I:minimumFluidFuelTemperature)
* When using a Pneumatic Helmet with Entity Tracker installed, drones no longer automatically show redstone particles where they're working; you now also need to have the Entity Tracker enabled, and have a Dispenser Upgrade installed (which is also required for drone debugging).  You also need to be within 32 blocks of the drone.
* Jet Boots flight is now a little more hazardous at Hard difficulty level; you will take more damage from horizontal collisions, and your boots won't absorb all the fall damage if you hit the ground while thrusting.
#### Fixes
* Removing a drone or Network API from the programmable controller now resets its digging position, preventing a phantom digging laser being shown.
* Fixed item loss bug with logistics drones and logistics modules when requesting a specific number of items from a Requester Frame with some (but not all) non-vanilla inventories.
* Logistics Frames can now once more be configured when in item form by right clicking (this is an old 1.7.10 feature that stopped working in the port to 1.12.2).
* Logistics Frame item tooltips now once more show their filter settings when shift is held (again, old 1.7.10 feature).
* WAILA/TOP info now shows detailed Logistics Frame filter information when player sneaks.
* Logistics Requester Frame now correctly requests fluids.
* Fixed log spam for Aphorism tile (was wrongly sending sync packets from client side)

### 0.7.8-259 (28 Aug 2018)
#### Updates
* Breaking any PneumaticCraft block by shift-clicking with any wrench will now keep any installed upgrades in the dropped block. Breaking the block with a pickaxe drops the upgrades, as before.
* Info tab on the GUI for all Logistics frames (shown by right-clicking with Logistics Configurator) has better descriptions for each frame type.
* Added client-side config setting (B:semiBlockLighting) to control if block lighting should be used on semiblocks like the logistics frames and heat frame. True by default, but can be set to false if lighting is glitchy.
#### Fixes
* Hopefully fix item loss issues with Programmable Controller (this needs more testing before release!)
* Fix problem with Programmable Controllers and tools like the Draconic Staff of Power which drop items at the player's location (fake player's location was wrong, causing items to be dropped in odd places)
* Fix occasional caching problem where neighbouring tile entities were not detected by PneumaticCraft blocks
* Fix greedy Amadron drones stealing all the fluid from your tank instead of the advertised amount.
* Fix intermittent Amadron tablet desync (caused when a periodic offer which happened to match a static offer was shuffled out)
* Drones now once more render their targeting laser while digging blocks like they did in 1.7.10 (actually they always did, but with an alpha value of 0...)
* GUI redstone control tab: the redstone button no longer sometimes renders excessively wide.

### 0.7.7-255 (16 Aug 2018)
#### Fixes
* Fixed stupid bug I added in 0.7.4 which caused Amadron offers to sometimes go out of sync on client & server, breaking the whole Amadron system.

### 0.7.6-254 (13 Aug 2018)
#### Fixes
* Fixed items in pressure chamber not always syncing to client properly.
* Fixed air amount showing negative in vacuum pump info tab: minimum is now floored at 0mL.
* Fixed heat frames and logistics frames looking unnaturally bright when in dark areas.  Note heat frame still glows (but doesn't emit light) when hot.
* Fixed pressure chamber valve sync errors in the log which may occur when loading a save from versions of PNC:R earlier than 0.7.2 (size of internal pressure chamber item buffer changed)
* Fixed problem where burn time of lava buckets in furnaces was badly nerfed; this was a change that should only have affected buckets containing PNC:R's own fuels.
* Fixed (hopefully) ConcurrentModificationException crash, possibly when changing dimensions (leaving the End?). This was not one that we could reproduce ourselves, but some extra protection code has been added to be safe. Might be related to some other mod causing chunks to be loaded on a different thread.
* Fixed rendering of minigun when held by other players
* Fixed ClassCastException being thrown (log spam) when firing minigun
* Fixed sync packets wrongly being sent from client-side pressure chamber valve code (log spam)
* Fixed (hopefully) semiblocks rendering leaking across dimensions (semiblocks are logistics frames, heat frames, crop supports, spawner agitators & transfer gadgets)
* Fixed visual issue with some JEI recipes (explosion crafting, UV light box, PCB etching) seen in newer JEI release

### 0.7.5-248 (26 Jul 2018)
#### Fixes
* Fixed NPE when using a newly-crafted GPS Area Tool
* Fixed Amadron offers not syncing properly in 'Open to LAN' worlds
* Fixed client kick when trying to add periodic Amadron offers on dedicated server

### 0.7.4-244 (25 Jul 2018)
#### Updates
* Amadron Tablet has seen a lot of attention...
  * Recoloured GUI background to now match what the item looks like
  * Moved the lesser-used "Add Trade" button into its own "Custom Trades" side tab. This button often appeared to get confused with the "Place Order" button beside it, which is what most players more often want to do.
  * Also in "Custom Trades", added 2 buttons which admin players can use to add custom periodic & static trades via the GUI.  Players need the "pneumaticcraft.amadron.addPeriodicTrade" and/or "pneumaticcraft.amadron.addStaticTrade" Forge permission nodes to do this (which by default means being an op). Note that custom periodic trades can't (yet) be removed via GUI; edit the "config/pneumaticcraft/AmadronOffersPeriodic.cfg" & "config/pneumaticcraft/AmadronOffersStatic.cfg" files to do that.
  * Also added a button for admin players to add custom static trades via the GUI, with the same restrictions as for custom periodic trades (permission node "pneumaticcraft.amadron.addStaticTrade", file "config/pneumaticcraft/AmadronOffersStatic.cfg").
  * "Place Order" button is now greyed out when the basket is empty
  * Added audible feedback when an order is successfully placed
  * Trade items with NBT are now supported (e.g. vanilla potions or enchanted books, and many modded items).  Note that the Amadron system will always try to match exactly the metadata and NBT you added the trade with.
* A bit more work (hopefully the last) on smoother fluid tank updates to client
* Can now install up to 10 Jet Boots upgrades in the Pneumatic Boots.  10 upgrades allows hovering in-place, but watch out for the in-flight air cost...
* Some tile entity performance improvements, most notably with the (output mode) Pressure Chamber Interface which was wasting a lot of CPU time but also to a lesser degree with the Air Compressor, UV Light Box and Sentry Turret
* Drones are now 50% faster (this makes up for the fact that they don't travel diagonally anymore)
* Performance improvements with the Charging Module (similar to Charging Station improvements in 0.7.3 release)
* Minigun ammo can now also be crafted with splash & lingering potions to get the corresponding splash/linger effect. But be warned, splash ammo get used up 3 times as fast, and lingering ammo 6 times as fast.
* Minigun ammo potion proc chance is now configurable - see I:minigunPotionProcChance in config)
* Added 3 methods to the Drone Interface computer peripheral: getDroneName(), getOwnerName(), getOwnerID()
* Magma blocks are now treated as heat sources
* Made the thermal resistance of non-vanilla fluids configurable - see D:fluidThermalResistance.  Default is 10; higher values make heat move from fluids to adjacent heat handlers more slowly.
#### Fixes
* Fixed Amadron restocking/payout drones (for player-player trading) spawning twice
* Fixed custom Amadron fluid trades not working
* Fixed NPE when trying to extract items from bottom side of Aerial Interface
* Fixed some armor features (magnet, charging) functioning even without the necessary upgrades installed
* Fixed Minigun Ammo not being craftable

### 0.7.3-239 (16 Jul 2018)
#### Updates
* Reworked 3rd party wrench support: many more wrenches are now supported
* More performance work with fluid tank sync'ing, reducing the rate at which tanks sync to client
* Charging Station now performs far better with many Speed Upgrades installed (was causing severe FPS drops)
* A few IGW updates, in particular the new Pneumatic Armor pieces are now documented
* Shift-left-clicking a GPS Tool in the Programmer GUI now creates a corresponding Area puzzle piece (this is an addition to the existing Left-Click to create a Coordinate puzzle piece)
#### Fixes
* Toggling jet boots off while thrusting no longer causes a desync
* Area GPS Tool now works on dedicated servers
* Mechanic Villagers created in the Pressure Chamber now have the right set of trades
* Wrenching PNC machines with 3rd party wrenches no longer also opens the machine's GUI
* Wrenching Pressure Tubes with 3rd party wrenches no longer causes a crash, and works as expected
* Breaking tile entities with empty fluid tanks no longer writes empty NBT data to the dropped item (so it will stack with other fresh items of the same type)
* Partially fixed navigation in the Coordinate Tracker upgrade for the helmet.  This needs more work, though (pathfinding has changed significantly since the upgrade was first created back in MC 1.6/1.7)

### 0.7.2-234 (11 Jul 2018)
#### Updates
* Pneumatic Door now plays a (suitably pneumatic) opening/closing sound effect.
* Some performance improvement work, particularly around Omni and Liquid Hoppers.  In particular, extracting from the Refinery with Liquid Hoppers caused significant FPS drops.
* Liquid Hoppers no longer accept input fluids on their output face, and will no longer output fluids from their input face.  (Related to performance improvements mentioned above)
* Tile Entities with fluid tanks now send far fewer updates to the client (also related to aforementioned performance work).  Previously they sent an update whenever the tank's contents changed, which is overkill for just rendering fluids.  Now updates are only sent when the contents change by more than 1% of the tank's total capacity. Can be adjusted in config - see 'D:liquidTankUpdateThreshold'.
#### Fixes
* Added back a missing texture for Aphorism Tile
* Fixed a server crash caused by a null ItemStack reference in Amadron custom offer handling

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
