# PneumaticCraft: Repressurized

This is a port to Minecraft 1.12.2 of MineMaarten's awesome PneumaticCraft mod: https://github.com/MineMaarten/PneumaticCraft.  It is *mostly* functional at this point, although likely to be fairly buggy and unstable. There are no official JAR releases just yet; if you want to play with it, you can build it from source.  It should be enough to clone the repository and do:

```
$ ./gradlew setupDecompWorkspace
$ ./gradlew build
```

...and any IDE-specific steps (genIntellijRuns etc.)

I'm welcoming GitHub issues if you find problems but **please**:
1. Check that there isn't already an issue for your problem
1. Be as descriptive as possible, including stacktraces (link to gist/pastebin/etc. please), and full instructions on reproducing the problem.

This code is based on MineMaarten's 1.8.9 code (see the *MC1.8.8* branch in the above-linked repo), which is fairly functional on 1.8 but missing a lot of client-side models & rendering.  At the time of writing (2 Nov 2017), pretty much all of that is re-implemented for 1.12.2!

Basically, this is a developer preview, not a fully usable mod at this stage, although it's getting very close.

IGW support is back, since MineMaarten has ported IGW to MC1.12.2!

With all that said, have fun!

## What's Working?

### Generally Working Well

Just about everything!  At least, all of the functionality from the 1.7.10 PneumaticCraft should now be replicated on 1.12.2, along with a few little extras:

* Aphorism tiles can now be edited (right-click with an empty hand).  They can also now have Minecraft markup (colours, bold/italic/underline/strikethrough) by using Alt + 0-9/a-f/l/m/n/o/r, and there's popup keymapping help if you hold down F1.  Also, drama splash is back, without the Drama Splash mod dependency (that mod hasn't been ported to 1.12).  And drama splash can be disabled in config.
* The kerosene lamp can now burn *any* burnable fuel; better fuels last longer (LPG is the best right now).  That can be disabled in config, to have the old behaviour of burning kerosene only.
* A new tool: the Camouflage Applicator.  This can be used to camouflage pressure tubes, elevator bases & callers, charging stations and pneumatic door bases with pretty much any solid block.
* Pressure tubes can be disconnected with a wrench, allowing ends to be closed off, and preventing connections where you don't want them.  Note that pressure tubes are not multiparts in this version (MCMP2 just isn't ready for prime time on 1.12 yet, IMHO).
* The One Probe is supported, and the probe can be crafted with the Pneumatic Helmet to integrate it.
* Vortex Cannon is now more effective at breaking plants and leaves.  You can also use the cannon to fling yourself considerable distances (but beware fall damage!)
* Touching a very cold heatsink (< -30C) will give you a slowness debuff.  Extremely cold heatsinks (< -60C) will also cause damage.  Hot heatsinks (> 60C) still hurt, but don't set you on fire until over 100C.
* GUI problem tab now shows a green tick icon (instead of the red "!" icon) when there are no problems with the machine.

### Not Working Yet

I'm now tracking work that's needed before the initial Alpha release here: https://github.com/desht/pnc-repressurized/milestone/1 (and issues in general here: https://github.com/desht/pnc-repressurized/issues)

If you're playing with the mod and encounter problems, check there first before raising a new issue.

### Not Tested Yet

* Security system (block appears to be working, but I need to set up a multiplayer environment to test properly)
* OpenComputers integration
* Universal Actuator *(implementation of this block was never completed in the original PneumaticCraft)*

*Any mod features not listed anywhere else can be assumed to be in the **Not Tested Yet** category.*

### TODO (once the mod is stable, post-release)

Features & enhancements tracked for beta release: https://github.com/desht/pnc-repressurized/milestone/2

Features & enhancements tracked for post-beta: https://github.com/desht/pnc-repressurized/milestone/3

### Other Stuff

PneumaticCraft: Repressurized is licensed under the GNU GPLv3: https://www.gnu.org/licenses/gpl-3.0.en.html

PneumaticCraft: Repressurized also includes the following free sound resources, which are licensed separately:

* https://freesound.org/people/ThompsonMan/sounds/237245/ (CC BY 3.0)

