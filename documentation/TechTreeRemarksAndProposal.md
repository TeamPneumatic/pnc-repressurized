# PneumaticCraft Tech Tree
## Definitions
c.iron = Compressed Iron Ingot

## Current Tech Tree
See TechTree.png in this folder. This is based on both recipes and what makes sense (e.g. the Logistic Drone does not require plastic, but to be able to use it you need Logistic Frames, which do need plastic).

## Remarks about current tech tree (by MineMaarten)

As a whole, the mod currently is a mid-late game mod as a result of the resource/advancement requirements.

1. The Pressure Chamber set-up is too iron-heavy for an early game mechanic (16x c.iron for chamber + ~8x c.iron for 1x Air Compressor + 10x iron for 2x Pressure Chamber Interface = 34x iron)
2. The extraction of items from a Pressure Chamber with a filtered Pressure Chamber Interface is annoying to manage, stemming from the fact that c.iron could be a desired result item, or just a component of another recipe in a Pressure Chamber. The Interface does not know which is desired.
3. Various Tube Modules are too convoluted. Tube modules are controlled by redstone, while usually there is only an application for one setting. Advanced PCB's make this easier, but are too far down the tech tree.
4. Different plastic+programming piece colors do not add to the gameplay (the concept of the different plastic colors stems from the plastic plants era, where different colors had to be obtained from different places).
5. Programming pieces are a bit too expensive (2x plastic + 1/4 PCB per piece).
6. The Assembly System is way too c.iron+plastic heavy (157x c.iron + 87x plastic, mostly as a result of requiring 14x cylinders).
7. It would be nice if the Assembly System could move to an earlier point in the tech tree somehow, as it is a fun mechanic which not many people get to, as it is only required for the 2nd tier pressure.
8. The 2nd tier of pressure is way too high tier, and has very limited use (only next tier Compressors, Aerial Interface and Programmable Controller). 
9. The UV Light Box is a bit tricky to automate, because it allows extraction of incomplete PCB's.
10. As PCB development in the UV Light Box is linear, there's no benefit of stopping when the PCB's is at a less than 100% success chance. Therefore this does not add to the gameplay, as waiting for 100% simply is the best strategy.
11. The 'throw pcb in acid and wait' mechanic is meh, as the time taken does not add gameplay and it is not really nicely automatable.
12. Drone automation currently is too high in the tech tree.
	1. Logistic Drones (or actually, the Logistic Frames), are too high tier.
13. The Logistics Tube Module is too high tier (requiring plastic and 8x gold per module).
14. The mechanics of the mod, like the pressure system and the heat system could be explained in-game in a better way.
15. The Electrostatic Compressor is useless. This stems from the time where one of the plastic plants created Lightning, which could act as a energy source. This is not relevant anymore, and even back then it wasn't a worthwhile energy source.
16. The Volume Upgrade is not worthwhile making, because it adds an insignificant amount of air to the system.
17. The upgrades have oddly specific names (like Dispenser Upgrade) which have effects on other machines that don't make sense.
18. The Programmable Controller pressure stats (5 bar max) is inconsistent with its recipe (requiring an Advanced Pressure Tube).

Air Compressor not blowing up set-up?
Moving stuff to the 2nd tier pressure when it's more accessible?

## Proposals (by MineMaarten)

The proposals follow the same numbering as the remarks, addressing the same numbered remark. For example, 1 aims to resolve remark 1, 2 aims to resolve remark 2, etc.

1. Change recipes: (reducing the c.iron requirement from 34x c.iron to 12x c.iron)
	1. Add recipe: `1x c.iron + 8 stone/brick -> 8x Reinforced Brick`.
	2. Change `8x c.iron -> 16x Pressure Chamber Walls` to `1x Reinforced Brick -> 1x Pressure Chamber Walls`.
	3. Change `6x c.iron + stuff -> 1x Air Compressor` to `6x Reinforced Brick + stuff -> 1x Air Compressor`.
	4. Change `1x Hopper + 1x Pressure Chamber Wall -> 1x Pressure Chamber Interface` to `1x Hopper + 2x Pressure Chamber Wall -> 2x Pressure Chamber Interface`. This recipe has the added benefit that it also might hint that Pressure Chamber Interfaces are best used in pairs.
2. Change recipes so that c.iron ingot only can be the output of a recipe, never the input, by changing every Pressure Chamber recipe containing c.iron to use Reinforced Brick instead. When this is done, the filter options for the Pressure Chamber Interface can be removed, as the Interfaces themselves can determine what should be extracted. The recipes to change are:
	1. Change `1x c.iron + 1x green plastic -> 1x Empty PCB` to `1x Reinforced Brick + 1x Redstone + 1x plastic -> 1x Empty PCB`.
	2. Change `1x c.iron + 1x Redstone + 1x black plastic -> 1x Transistor` to `1x Reinforced Brick + 1x Coal + 1x plastic -> 1x Transistor`.
	3. Change `1x c.iron + 1x Redstone + 1x cyan plastic -> 1x Capacitory` to `1x Reinforced Brick + 1x Lapis Lazuli + 1x plastic -> 1x Capacitor`.
3. Remove the 'pressure thresholds scale to redstone' mechanic for the Safety Valve Tube Module and Regulator Tube Module, and fix the threshold to 4.5 bar by default when placed on a normal Pressure Tube, and 19.5 bar when placed on an Advanced Pressure Tube. The safety valve will not respond to redstone, the Regulator Tube Module will change to 0 bar when powered with redstone (so it can be used as a valve). Advanced PCB's still could be applied to allow for the linear redstone behaviour.
4. Remove all plastic/programming puzzle piece variants, and only keep one. Other recipes that overlap (like the different hacking components requiring 1x chest + 8x some type of plastic) will need to be adjusted. The Plastic Mixer will be removed. Liquid Plastic can be turned solid by one of the following instead:
	1. Placing Liquid Plastic in the world (e.g. with a Liquid Hopper + Dispenser Upgrade), and letting it cool, turning it into 1x plastic (not a plastic block, as 1000mB = 1x plastic). Picking items up is easily automatable with Hoppers.
	2. Recipe: 1x Liquid Plastic Bucket = 1x Empty Bucket (container item) + 1x plastic. Could be useful, but a bit trickier to automate.
	3. Support for Thermal Expansion Fluid Transposer.
	4. Support for Tinkers casting.
5. Double the Programming Piece recipe output from `1x PCB + 8x plastic -> 4x Puzzle Piece` to `1x PCB + 8x plastic -> 8x Puzzle Piece`.
6. Change recipes: (reducing the c.iron requirement from 157x to 58x c.iron, and reducing the plastic requirement from 87x to 45x plastic)
	1. Change `6x c.iron + 1x Pressure Tube -> 1x Cannon Barrel` to `6x Reinforced Brick + 1x Pressure Tube -> 1x Cannon Barrel` (See 1.i).
	2. Double the output of Pneumatic Cylinders from 1x to 2x.
	3. Change the Assembly machine's recipes from using c.iron as base to use Reinforced Brick instead.
7. See 6.
8. See 6.
9. Allow for easier automation of the UV Light Box by only allowing extraction of PCB's that have a success chance more than or equal to the configured success chance (the redstone setting).
10. Change the PCB development function to develop quicker at first (up to 80%) and then slower from 80% onwards (still 10 mins total to go from 0% to 100%). This should result in it being quicker to stop at 80%, and deal with a 20% Failed PCB's. This is harder to automate, but would only be for the people wanting to get that extra efficiency.
11. Add an Etching Tank (repurposing the Plastic Mixer model), in which Etching Acid can be pumped, as well as PCB's that need to be etched. It would have two output slots, one for Unassembled PCB's and one for Failed PCB's. PCB's can be processed in parallel, just like with the Acid pool. Whether or not a small amount of acid is used is up for debate.
12. Change the Logistic Frame recipes from `8x some plastic -> 1x Logistic Frame` to `8x Reinforced Brick + some dye -> 1x Logistic Frame`. Additionally, add other preprogrammed Drone types like a Harvesting/Farming Drone (already WIP), Item Pick up Drone, Killer Drone, ...
13. Change the Logistics Module recipe from `4x plastic + 4 c.iron + 1x regulator tube module -> 1x Logistics Module` to `4x Redstone + 4 Reinforced Brick + 1x Pressure Tube -> 1x Logistics Module`
14. Add research minigames as a way of tutorials. These minigames will be designed to be quick and easy for the player already familiar with the mechanics of the mod (so the repeating research minigame can be skipped), but more challenging for the newer players. The idea currently is to display a 2D map in a GUI, in which a simplified version of the mechanics need to be used to reach targets. Either that, or a 'tutorial dimension' in which people can optionally teleport to. These tutorials/minigames would include:
	1. Basic pressure mechanics.
		1. goal: Pressurize a machine a few tiles away by connecting up tubes.
		2. mechanics taught: Pressure dispersion mechanics, pressure too high = boom.
	2.  Heat mechanics.
		1. goal: Heat/cool a machine.
		2. mechanics taught: Heat dispersion mechanics, isolating blocks to prevent heat loss, vortex tube.
15. Remove the Electrostatic Compressor. Repurpose its model (it's a cool looking model!), for a lightning generator? I'm open to other suggestions.
16. Change the Volume Upgrade to add 50,000mL instead of 5,000mL to the volume of the machine.
17. Add more specific upgrades which target the specific machines (e.g. a 'Drone Inventory' upgrade, instead of the Dispenser Upgrade'). That way the name of the upgrade acts as documentation.
18. Increase the max pressure and minimal working pressure of the Programmable Controller to match a 2nd tier pressure.