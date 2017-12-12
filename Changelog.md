# PneumaticCraft: Repressurized - Changelog

This is an overview of significant new features and fixes by release.  See https://github.com/TeamPneumatic/pnc-repressurized/commits/master for a detailed list of all changes.

Changes are in reverse chronological order; newest changes at the top.

## Minecraft 1.12.2

## 0.x.x-xx (Unreleased)

* Fixed the Heat Sink looking like it is facing up when actually facing down.

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
