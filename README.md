# PneumaticCraft: Repressurized

This is a port to Minecraft 1.12.2 of MineMaarten's awesome PneumaticCraft mod: https://github.com/MineMaarten/PneumaticCraft.  It is *not* fully functional at this point, and there are no JAR releases; if you want to play with, build it from source.  It should be enough to do:

```
$ ./gradlew setupDecompWorkspace
$ ./gradlew build
```

...and any IDE-specific steps (genIntellijRuns etc.)

This code is based on MineMaarten's 1.8.9 code (see the *MC1.8.8* branch in the above-linked repo), which is fairly functional but is missing a lot of client-side stuff.  At the time of writing, a significant amount of the fancy 1.7.10 rendering (block animations) has been ported and reimplemented in 1.12.2, but not all.

Basically, this is a developer preview, not a usable mod at this stage, although it's getting close.  I'm welcoming any assistance & pull requests, especially in the area of modelling and client-side animation.

Note that this tree also contains a version of igwmod from https://github.com/Z-Doctor/IGW-mod/tree/1.11/src/igwmod, further ported to 1.12 by me.  It's not usable as a mod here, but only for the API to allow PneumaticCraft: Repressurized to compile.  I haven't decided what to do with IGWMod yet; I may just remove it and move the in-game docs to the FTB Utilities guide system or similar.

With all that said, have fun!

## What's Working?

### Generally Working Well

* Explosions convert iron -> compressed iron
* Solid and liquid-fuelled air compressors, creative compressor
* Pressure tubes (but with MCMP2 disabled in config)
* Pressure chamber multiblock
* Pressure chamber interface
* Etching Acid
* UV Light Box
* Assembly system
* Pneumatic Helmet (there may be some persistence issues with keybinds etc?)
* Oil lake generation
* Gas Lift
* Refinery (only with our own Oil right now, but will be possible to use other mods's oils soon, e.g. Thermal Foundation Crude Oil)
* Thermopneumatic Processing Plant
* Plastic Mixer
* Vacuum Pump (and pressure chamber disenchanting)
* Kerosene Lamp (also works with any burnable fuel, duration dependent on fuel quality)
* Omni Hopper and Liquid Hopper
* Heat system (including 3rd-party interaction: Gelid Cryotheum is a thing!)
* Programmer
* Drones (not well tested, but a simple "Go To Location" program works)
* Pneumatic Wrench
* GPS Tool
* Manometer
* Seismic Sensor
* Aphorism Tiles
* Air Cannon
* Aerial Interface
* Pneumatic Doors
* Tube Modules
* Heat Frame
* ComputerCraft integration
* Logistics Drones & Frames (reasonably well tested)
* Elevators
* Pneumatic Villagers (mechanics)
* Flux Compressor & Pneumatic Dynamo (using Forge Energy, no Tesla or direct CoFH support at this point)
* Universal Sensor

### Functional but Needs Clientside Work

* Vortex tube doesn't get tinted to indicate temperature (OBJ models can't have a tintindex - may need a TESR or some IBakedModel trickery)
* Miniguns (ammo works, mobs die, but no item model)
* Vortex Cannon (vortex entities work fine but cannon needs an item model)
* Flux Compressor could use a fancier model...
* Refinery doesn't render the contained liquids
* Liquid Hopper texture is a mess

### Not Working Yet

* Multipart pressure tubes with MCMP2; support is in, but not working yet.  I need to get a better understanding of the API.  Pressure tubes work fine if you set "mcmultipart" to "false" in ``config/pneumaticcraft/thirdparty.cfg``.
* Electric Compressor & Pneumatic Generator (need to reimplement with IC2 API)
* Achievements (need to convert to 1.12 Advancements)
* IGW support (there's no official 1.12 port, but see https://github.com/Z-Doctor/IGW-mod/tree/1.11/src/igwmod)

### Not Tested Yet

* Security system
* Amadron system
* Programmable Controller
* Universal Actuator
* OpenComputers integration

*Any mod features not listed anywhere else can be assumed to be in the **Not Tested Yet** category.*

### TODO (once the mod is stable, post-release)

* Expose more functionality via capabilities (e.g. ``IAirHandler``, ``IHeatExchangerLogic``, ``IPressurizable``)
* Look at re-implementing some rendering with FastTESR's.  Animated blocks all use regular TESR's right now, which isn't great for client performance (but at least fewer TESR's are used now than in 1.7.10, so that's a start)
* Allow Pneumatic Dynamo to take Thermal Expansion augments?
