# PneumaticCraft: Repressurized

* 1.20.1 ![1.20.1 build status](https://github.com/TeamPneumatic/pnc-repressurized/actions/workflows/main.yml/badge.svg?branch=1.20.1)
* 1.19.2 ![1.19.2 build status](https://github.com/TeamPneumatic/pnc-repressurized/actions/workflows/main.yml/badge.svg?branch=1.19.2)

This is a port to Minecraft 1.20.1 of MineMaarten's awesome PneumaticCraft mod: https://github.com/MineMaarten/PneumaticCraft.  It is fully functional at this point, and under active maintenance.

* Releases are available from https://minecraft.curseforge.com/projects/pneumaticcraft-repressurized
* Maven artifacts (including dev builds) are available from https://modmaven.dev/me/desht/pneumaticcraft/pneumaticcraft-repressurized/

To build from source, clone the repository and load it into your IDE (e.g. for Intellij, *File* -> *New* -> *Project from Existing Source...* and select the `build.gradle` file). Then run any IDE-specific steps that you need (``genIntellijRuns`` etc.)

GitHub issues are welcome if you find problems or have enhancement request but **please**:
1. Check that there isn't already an issue for your problem
1. Be as descriptive as possible, including stacktraces (link to gist/pastebin/etc. please), and full instructions on reproducing the problem.

Have fun!

## Discord

Join us on https://discord.gg/Zf5XwbfBRj to chat about the mod.  Keep it clean, please :)

## New Features in PneumaticCraft: Repressurized

See the [Changelog](https://github.com/TeamPneumatic/pnc-repressurized/blob/1.20.1/Changelog.md) for more information.

Also see https://gist.github.com/desht/b604bd670f7f718bb4e6f20ff53893e2 for an overview of new and planned new features in PneumaticCraft for 1.14.4 and later.

### Who is Team Pneumatic?

Team Pneumatic consists of two developers: MineMaarten and desht; MineMaarten is the original author of PneumaticCraft for 1.6.x/1.7.x/1.8.x, and desht carried out the port to 1.12.2 and later Minecraft releases.  MineMaarten is not currently actively developing, but desht is developing & maintaining releases for modern Minecraft versions.  Releases for version earlier than 1.18.2 are no longer maintained (1.18.2 is critical-fixes-only), and no support is provided (although questions about older releases are welcome on the Discord; just don't expect any updates).

Of course, being an open-source project, there are other welcome contributors - see https://github.com/TeamPneumatic/pnc-repressurized/graphs/contributors for a full list (this includes contributors to the original PneumaticCraft project too).

### Licensing Information

* The PneumaticCraft: Repressurized mod is licensed under the GNU GPLv3: https://www.gnu.org/licenses/gpl-3.0.en.html
* The PneumaticCraft: Repressurized API (everything under `src/main/java/me/desht/pneumaticcraft/api/`) is licensed under the GNU LGPLv3: https://www.gnu.org/licenses/lgpl-3.0.en.html

The separate API licensing is intended to allow other mods to link against the API without needing to be licensed under the GPLv3.

PneumaticCraft: Repressurized also includes the following free sound resources, which are licensed separately:
* https://freesound.org/people/ThompsonMan/sounds/237245/ (CC BY 3.0)
* https://freesound.org/people/Crinkem/sounds/481787/ (CC0 1.0)

