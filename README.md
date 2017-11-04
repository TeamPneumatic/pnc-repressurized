# PneumaticCraft: Repressurized

This is a port to Minecraft 1.12.2 of MineMaarten's awesome PneumaticCraft mod: https://github.com/MineMaarten/PneumaticCraft.  It is *mostly* functional at this point, although likely to be fairly buggy and unstable. There are no official JAR releases; if you want to play with it, you can either:

* Use CI builds from https://circleci.com/gh/desht/pnc-repressurized

or 

* Build it from source.  It should be enough to clone the repository and do:

```
$ ./gradlew setupDecompWorkspace
$ ./gradlew build
```

...and any IDE-specific steps (genIntellijRuns etc.)

I'm welcoming any GitHub issue if you find problems but **please**:
1. Check that there isn't already an issue for your problem
1. Be as descriptive as possible, including stacktrackes (link to gist/pastebin/etc. please), and full instructions on reproducing the problem.

This code is based on MineMaarten's 1.8.9 code (see the *MC1.8.8* branch in the above-linked repo), which is fairly functional on 1.8 but missing a lot of client-side models & rendering.  At the time of writing (2 Nov 2017), pretty much all of that is re-implemented for 1.12.2!

Basically, this is a developer preview, not a fully usable mod at this stage, although it's getting very close.

Note that this tree also contains a version of igwmod from https://github.com/Z-Doctor/IGW-mod/tree/1.11/src/igwmod, further ported to 1.12 by me.  It's not usable as a mod here, but only for the API to allow PneumaticCraft: Repressurized to compile.  I haven't decided what to do with IGWMod yet; I may just remove it and move the in-game docs to the FTB Utilities guide system or similar (McJty is working on a new docs mod called Lector which sounds interesting...)

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
* Pneumatic Helmet
* Oil lake generation
* Gas Lift
* Refinery (including support for 3rd party oils; as of now Thermal Expansion Crude Oil and Actually Additions Oil - that's the 2nd tier oil - should work)
* Thermopneumatic Processing Plant
* Plastic Mixer
* Vacuum Pump (and pressure chamber disenchanting)
* Kerosene Lamp (also works with any burnable liquid fuel, duration dependent on fuel quality)
* Omni Hopper and Liquid Hopper
* Heat system (including 3rd-party interaction: Gelid Cryotheum cooling is a thing!)
* Programmer
* Drones (tested several functions; mostly working but there are some pathfinding issues causing drones to teleport when they need to move to a distant location because they can't pathfind far enough)
* Amadron trading system (but see drone pathfinding issues above)
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
* Flux Compressor & Pneumatic Dynamo (using Forge Energy; no Tesla or direct CoFH support at this point - not sure if it will be necessary to add)
* Universal Sensor
* Programmable Controller (only basic testing done, but a simple "break blocks in area" program works)

### Not Working Yet

I'm now tracking work that's needed before the initial Alpha release here: https://github.com/desht/pnc-repressurized/milestone/1 (and general issues here: https://github.com/desht/pnc-repressurized/issues)

If you're playing with the mod and encounter problems, check there first before raising a new issue.

### Not Tested Yet

* Security system (block appears to be working, but I need to set up a multiplayer environment to test properly)
* OpenComputers integration
* Universal Actuator *(implementation of this block was never completed in the original PneumaticCraft)*

*Any mod features not listed anywhere else can be assumed to be in the **Not Tested Yet** category.*

### TODO (once the mod is stable, post-release)

Features & enhancements tracked for beta release: https://github.com/desht/pnc-repressurized/milestone/2

Features & enhancements tracked for post-beta: https://github.com/desht/pnc-repressurized/milestone/3


