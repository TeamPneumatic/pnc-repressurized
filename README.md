# PneumaticCraft: Repressurized

This is a port to Minecraft 1.12 of MineMaarten's awesome PneumaticCraft mod: https://github.com/MineMaarten/PneumaticCraft.  It is *not* fully functional at this point, and there are no JAR releases; if you want to play with, build it from source.  It should be enough to do:

```
$ ./gradlew setupDecompWorkspace
$ ./gradlew build
```

...and any IDE-specific steps (genIntellijRuns etc.)

This code is based on MineMaarten's 1.8.9 code (see the *MC1.8.8* branch in the above-linked repo), which is fairly functional but is missing a lot of client-side stuff.  Basically, this is a developer preview, not a usable mod at this stage.  I'm welcoming any assistance & pull requests, especially in the area of modelling and client-side animations; those have not been ported from the original 1.7.10 mod yet and they're not my strong point either (time to learn!)

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
* Refinery
* Thermopneumatic Processing Plant
* Plastic Mixer
* Vacuum Pump (and pressure chamber disenchanting)
* Kerosene Lamp (also works with any burnable fuel)
* Omni Hopper and Liquid Hopper (liquid hopper textures are messed up though)
* Heat system
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
* Logistics Drones & Frames (not well tested: active provider frame & default storage frame work with logistics drone)
* Elevators
* Pneumatic Villagers (mechanics)
* Flux Compressor (using Forge Energy, no Tesla or direct CoFH support at this point)

### Functional but Needs Clientside Work

* Vortex tube & Heat Sink don't show colours to indicate temperature (OBJ models can't have a tintindex - may need to redo Heat Sink with JSON, Vortex Tube may need a TESR?)
* Miniguns (ammo works, mobs die, but no item model)
* Vortex Cannon (vortex entities work fine but cannon needs an item model)
* Flux Compressor could use a fancier model...

### Not Working Yet

* Multipart pressure tubes with MCMP2; support is in, but not working yet.  I need to get a better understanding of the API.  Pressure tubes work fine if you set "mcmultipart" to "false" in ``config/pneumaticcraft/thirdparty.cfg``.
* Electric Compressor (not in game, need to reimplement with IC2 API)
* Achievements (need to convert to 1.12 Advancements)
* IGW support (there's no official 1.12 port, but see https://github.com/Z-Doctor/IGW-mod/tree/1.11/src/igwmod)

### Not Tested Yet

* Security system
* Amadron system
* Programmable Controller

