package pneumaticCraft.common.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.api.recipe.PressureChamberRecipe;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.item.ItemNetworkComponents;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Names;
import cpw.mods.fml.common.registry.GameRegistry;

public class CraftingRegistrator{
    public static void init(){
        ItemStack lapis = new ItemStack(Items.dye, 1, 4);
        ItemStack swiftnessPotion = new ItemStack(Items.potionitem, 1, 8194);//3.00m variant
        ItemStack cobbleSlab = new ItemStack(Blocks.stone_slab, 1, 3);
        // tubes
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.pressureTube, 4, 0 /* normal */), "igi", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass"));
        GameRegistry.addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_FLOW_DETECTOR)), "bbb", "btb", "bbb", 'b', Itemss.turbineBlade, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        GameRegistry.addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_SAFETY_VALVE)), " g ", "ltl", 'g', Itemss.pressureGauge, 'l', Blocks.lever, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        GameRegistry.addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_REGULATOR)), "sts", 's', ModuleRegistrator.getModuleItem(Names.MODULE_SAFETY_VALVE), 't', new ItemStack(Blockss.pressureTube, 1, 0));
        GameRegistry.addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_AIR_GRATE)), " b ", "btb", " b ", 'b', Blocks.iron_bars, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        GameRegistry.addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_GAUGE)), " g ", "rtr", 'g', Itemss.pressureGauge, 'r', Items.redstone, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        GameRegistry.addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_CHARGING)), " r ", "rtr", " r ", 'r', Blockss.chargingStation, 't', new ItemStack(Blockss.pressureTube, 1, 0));

        // tube addons
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.pressureGauge), " g ", "gig", " g ", 'g', Items.gold_ingot, 'i', Names.INGOT_IRON_COMPRESSED));

        // pressure chamber
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.pressureChamberWall, 4, 0), "iii", "i i", "iii", 'i', Names.INGOT_IRON_COMPRESSED));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.pressureChamberWall, 4, 6), "iii", "igi", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass"));
        GameRegistry.addShapelessRecipe(new ItemStack(Blockss.pressureChamberWall, 4, 6), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), Blocks.glass);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.pressureChamberValve, 4, 0), "iii", "iti", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.pressureTube, 1, 0)));
        GameRegistry.addShapelessRecipe(new ItemStack(Blockss.pressureChamberValve, 4, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureTube, 1, 0));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.pressureChamberInterface, 4, 0), "ici", "ihi", "ici", 'i', Names.INGOT_IRON_COMPRESSED, 'c', new ItemStack(Itemss.pneumaticCylinder, 1, 0), 'h', Blocks.hopper));

        // cannon
        GameRegistry.addRecipe(new ItemStack(Blockss.airCannon), " b ", " st", "hhh", 'b', Itemss.cannonBarrel, 's', Itemss.stoneBase, 't', new ItemStack(Blockss.pressureTube, 1, 0 /* normal */), 'h', cobbleSlab);
        GameRegistry.addRecipe(new ItemStack(Itemss.stoneBase), "s s", "sts", 's', Blocks.stone, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.cannonBarrel), true, "i i", "i i", "pii", 'i', Names.INGOT_IRON_COMPRESSED, 'p', ModuleRegistrator.getModuleItem(Names.MODULE_SAFETY_VALVE)));

        GameRegistry.addRecipe(new ItemStack(Itemss.GPSTool), " r ", "pgp", "pdp", 'r', Blocks.redstone_torch, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE), 'g', Blocks.glass_pane, 'd', Items.diamond);

        // compressor
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.airCompressor), true, "iii", "i t", "ifi", 'i', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.pressureTube, 1, 0), 'f', Blocks.furnace));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.advancedAirCompressor), true, "iii", "i t", "ifi", 'i', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.advancedPressureTube, 1, 0), 'f', Blockss.airCompressor));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.liquidCompressor), true, "iii", "ibi", "ici", 'i', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE), 'b', Items.bucket, 'c', Blockss.airCompressor));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.advancedLiquidCompressor), true, "iii", "ibt", "ici", 'i', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.RAIN_PLANT_DAMAGE), 'b', Items.bucket, 'c', Blockss.liquidCompressor, 't', Blockss.advancedPressureTube));
        GameRegistry.addRecipe(new ItemStack(Blockss.electrostaticCompressor), "bpb", "dra", "bcb", 'b', Blocks.iron_bars, 'p', Itemss.printedCircuitBoard, 'd', Items.diamond, 'r', Itemss.turbineRotor, 'a', new ItemStack(Blockss.advancedPressureTube), 'c', Blockss.airCompressor);

        // Charging Station
        GameRegistry.addRecipe(new ItemStack(Blockss.chargingStation), "  t", "ppp", "sss", 's', cobbleSlab, 't', new ItemStack(Blockss.pressureTube, 1, 0), 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.elevatorFrame, 4, 0), "i i", "i i", "i i", 'i', Names.INGOT_IRON_COMPRESSED));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.pneumaticCylinder), "pip", "pip", "pbp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.RAIN_PLANT_DAMAGE), 'i', Names.INGOT_IRON_COMPRESSED, 'b', Itemss.cannonBarrel));
        GameRegistry.addRecipe(new ItemStack(Blockss.elevatorBase, 4, 0), "cpc", "pcp", "cpc", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'c', Itemss.pneumaticCylinder);
        GameRegistry.addRecipe(new ItemStack(Blockss.elevatorCaller, 1, 0), "cpc", "prp", "cpc", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SLIME_PLANT_DAMAGE), 'c', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FLYING_FLOWER_DAMAGE), 'r', Items.redstone);
        GameRegistry.addRecipe(new ItemStack(Blockss.elevatorCaller, 1, 0), "cpc", "prp", "cpc", 'c', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SLIME_PLANT_DAMAGE), 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FLYING_FLOWER_DAMAGE), 'r', Items.redstone);

        //Security Station
        GameRegistry.addRecipe(new ItemStack(Blockss.securityStation), "gbg", "tpt", "ggg", 'g', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'b', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE), 't', Itemss.turbineRotor, 'p', Itemss.printedCircuitBoard);
        GameRegistry.addRecipe(new ItemStack(Itemss.networkComponent, 16, ItemNetworkComponents.NETWORK_NODE), "ttt", "tct", "ttt", 't', Itemss.transistor, 'c', Blocks.chest);
        GameRegistry.addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_IO_PORT), "ttt", "tct", "ttt", 't', Itemss.capacitor, 'c', Blocks.chest);
        GameRegistry.addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_REGISTRY), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.REPULSION_PLANT_DAMAGE), 'c', Blocks.chest);
        GameRegistry.addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.DIAGNOSTIC_SUBROUTINE), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE), 'c', Blocks.chest);
        GameRegistry.addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_API), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.RAIN_PLANT_DAMAGE), 'c', Blocks.chest);
        GameRegistry.addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_DATA_STORAGE), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'c', Blocks.chest);

        // Machine Upgrades
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.machineUpgrade, 1, 0), "lil", "ici", "lil", 'l', lapis, 'i', Names.INGOT_IRON_COMPRESSED, 'c', new ItemStack(Itemss.airCanister, 1, OreDictionary.WILDCARD_VALUE)));
        GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 1), "lil", "idi", "lil", 'l', lapis, 'i', Items.quartz, 'd', Blocks.dispenser);
        GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 2), "lal", "aca", "lal", 'l', lapis, 'a', Items.apple, 'c', Items.clock);
        GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 3), "lbl", "bsb", "lbl", 'l', lapis, 'b', Items.bone, 's', Items.fermented_spider_eye);
        GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 4), "lwl", "wsw", "lwl", 'l', lapis, 'w', Blockss.pressureChamberWall, 's', Items.fermented_spider_eye);
        GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 5), "lsl", "scs", "lsl", 'l', lapis, 's', swiftnessPotion, 'c', Items.cake);
        GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 6), "lel", "ege", "lel", 'l', lapis, 'e', Items.ender_eye, 'g', Items.golden_carrot);
        GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 7), "lrl", "rgr", "lrl", 'l', lapis, 'r', Items.redstone, 'g', Itemss.GPSTool);
        GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 8), "lal", "aba", "lal", 'l', lapis, 'a', Items.arrow, 'b', Items.bow);
        GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 9), "lol", "obo", "lol", 'l', lapis, 'o', Blocks.obsidian, 'b', ModuleRegistrator.getModuleItem("safetyTubeModule"));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()), " t ", "iri", "iri", 'i', Names.INGOT_IRON_COMPRESSED, 'r', Items.redstone, 't', new ItemStack(Blockss.pressureTube, 1, 0)));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.turbineRotor), " b ", " i ", "b b", 'i', Names.INGOT_IRON_COMPRESSED, 'b', Itemss.turbineBlade));
        GameRegistry.addRecipe(new ItemStack(Blockss.vacuumPump), "grg", "trt", "sss", 'g', Itemss.pressureGauge, 'r', Itemss.turbineRotor, 's', cobbleSlab, 't', new ItemStack(Blockss.pressureTube, 1, 0));

        // NEI support recipes
        GameRegistry.addRecipe(new ItemStack(Itemss.vortexCannon, 1, Itemss.vortexCannon.getMaxDamage()), "ppp", "c  ", "plp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.HELIUM_PLANT_DAMAGE), 'l', Blocks.lever, 'c', new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()));
        GameRegistry.addRecipe(new ItemStack(Itemss.pneumaticWrench, 1, Itemss.pneumaticWrench.getMaxDamage()), "ppp", "c  ", "plp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'l', Blocks.lever, 'c', new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()));
        GameRegistry.addRecipe(new ItemStack(Itemss.pneumaticHelmet, 1, Itemss.pneumaticHelmet.getMaxDamage()), "cec", "c c", 'e', Itemss.printedCircuitBoard, 'c', new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()));
        GameRegistry.addShapelessRecipe(new ItemStack(Itemss.manometer, 1, Itemss.manometer.getMaxDamage()), new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()), Itemss.pressureGauge);

        // Pneumatic Items
        GameRegistry.addRecipe(new RecipeGun(ItemPlasticPlants.HELIUM_PLANT_DAMAGE, Itemss.vortexCannon));
        GameRegistry.addRecipe(new RecipeGun(ItemPlasticPlants.BURST_PLANT_DAMAGE, Itemss.pneumaticWrench));
        GameRegistry.addRecipe(new RecipePneumaticHelmet());
        GameRegistry.addRecipe(new RecipeManometer());

        RecipeSorter.register("pneumaticcraft:gun", RecipeGun.class, Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("pneumaticcraft:pneumaticHelmet", RecipePneumaticHelmet.class, Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("pneumaticcraft:manometer", RecipeManometer.class, Category.SHAPED, "after:minecraft:shaped");

        //misc
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.compressedIron), "iii", "iii", "iii", 'i', Names.INGOT_IRON_COMPRESSED));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Itemss.ingotIronCompressed, 9, 0), Names.BLOCK_IRON_COMPRESSED));

        GameRegistry.addShapelessRecipe(new ItemStack(Itemss.printedCircuitBoard), Itemss.unassembledPCB, Itemss.transistor, Itemss.transistor, Itemss.transistor, Itemss.capacitor, Itemss.capacitor, Itemss.capacitor);
        GameRegistry.addShapedRecipe(new ItemStack(Itemss.advancedPCB), "rpr", "pcp", "rpr", 'c', Itemss.printedCircuitBoard, 'r', Items.redstone, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.HELIUM_PLANT_DAMAGE));
        GameRegistry.addShapedRecipe(new ItemStack(Itemss.advancedPCB), "prp", "rcr", "prp", 'c', Itemss.printedCircuitBoard, 'r', Items.redstone, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.HELIUM_PLANT_DAMAGE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.uvLightBox), "lll", "ibt", "iii", 'l', Blocks.redstone_lamp, 'b', Itemss.PCBBlueprint, 'i', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.pressureTube, 1, 0)));

        //Assembly Machines
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.assemblyDrill), true, "dcc", "  c", "ipi", 'd', Items.diamond, 'c', Itemss.pneumaticCylinder, 'i', Names.INGOT_IRON_COMPRESSED, 'p', Itemss.printedCircuitBoard));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.assemblyLaser), true, "dcc", "  c", "ipi", 'd', new ItemStack(Items.dye, 1, 1), 'c', Itemss.pneumaticCylinder, 'i', Names.INGOT_IRON_COMPRESSED, 'p', Itemss.printedCircuitBoard));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.assemblyIOUnit), true, "hcc", "  c", "ipi", 'h', Blocks.hopper, 'c', Itemss.pneumaticCylinder, 'i', Names.INGOT_IRON_COMPRESSED, 'p', Itemss.printedCircuitBoard));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.assemblyPlatform), true, "a a", "ppp", "ici", 'a', Itemss.pneumaticCylinder, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.PROPULSION_PLANT_DAMAGE), 'i', Names.INGOT_IRON_COMPRESSED, 'c', Itemss.printedCircuitBoard));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.assemblyController), true, " c ", "tcc", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'c', Itemss.printedCircuitBoard, 't', new ItemStack(Blockss.pressureTube, 1, 0)));

        GameRegistry.addSmelting(Itemss.failedPCB, new ItemStack(Itemss.emptyPCB, 1, Itemss.emptyPCB.getMaxDamage()), 0);

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.pneumaticDoor), "cc", "cc", "cc", 'c', Names.INGOT_IRON_COMPRESSED));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.pneumaticDoorBase), true, " #c", "cct", "ccc", '#', Itemss.pneumaticCylinder, 'c', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.pressureTube, 1, 0)));

        for(int i = 0; i < 16; i++) {
            GameRegistry.addShapelessRecipe(new ItemStack(Items.dye, 1, i), new ItemStack(Itemss.plasticPlant, 1, i));
            GameRegistry.addShapelessRecipe(new ItemStack(Items.dye, 1, i), new ItemStack(Itemss.plasticPlant, 1, i + 16));//TODO remove legacy
        }
        GameRegistry.addRecipe(new ItemStack(Blockss.universalSensor), "plp", "lpl", "pcp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.ENDER_PLANT_DAMAGE), 'l', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.CHOPPER_PLANT_DAMAGE), 'c', Itemss.printedCircuitBoard);
        GameRegistry.addRecipe(new ItemStack(Blockss.aerialInterface), "whw", "ese", "wtw", 'w', Blockss.pressureChamberWall, 'h', Blocks.hopper, 'e', Items.ender_pearl, 's', new ItemStack(Items.skull, 1, 1), 't', new ItemStack(Blockss.advancedPressureTube, 1, 0));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.omnidirectionalHopper), "i i", "ici", " i ", 'i', Names.INGOT_IRON_COMPRESSED, 'c', Blocks.chest));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.liquidHopper), "i i", "ici", " i ", 'i', "blockGlass", 'c', Blocks.hopper));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blockss.plasticMixer), "igi", "g g", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass"));

        addProgrammingPuzzleRecipes();
        GameRegistry.addRecipe(new ItemStack(Itemss.drone), " b ", "bcb", " b ", 'b', Itemss.turbineRotor, 'c', Itemss.printedCircuitBoard);
        GameRegistry.addRecipe(new ItemStack(Blockss.programmer), "gbg", "tpt", "ggg", 'g', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE), 'b', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE), 't', Itemss.turbineRotor, 'p', Itemss.printedCircuitBoard);

        //Temporary recipes
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.PCBBlueprint), true, "eee", "eie", "eee", 'e', Items.emerald, 'i', Names.INGOT_IRON_COMPRESSED));
        GameRegistry.addRecipe(new ItemStack(Itemss.assemblyProgram, 1, 0), "eee", "eie", "eee", 'e', Items.emerald, 'i', Items.diamond);
        GameRegistry.addRecipe(new ItemStack(Itemss.assemblyProgram, 1, 1), "eee", "eie", "eee", 'e', Items.emerald, 'i', new ItemStack(Items.dye, 1, 1));
        GameRegistry.addShapelessRecipe(new ItemStack(Itemss.assemblyProgram, 1, 2), new ItemStack(Itemss.assemblyProgram, 1, 0), new ItemStack(Itemss.assemblyProgram, 1, 1));

        addPressureChamberRecipes();
        addAssemblyRecipes();
    }

    public static void addProgrammingPuzzleRecipes(){
        List<ItemStack> widgets = new ArrayList<ItemStack>();
        ItemProgrammingPuzzle.addItems(widgets);
        for(ItemStack output : widgets) {
            output.stackSize = 4;
            GameRegistry.addRecipe(output, "ppp", "pcp", "ppp", 'p', new ItemStack(Itemss.plastic, 1, output.getItemDamage()), 'c', Itemss.printedCircuitBoard);
        }
    }

    private static void addPressureChamberRecipes(){
        // diamond
        if(Config.enableCoalToDiamondsRecipe) PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Blocks.coal_block, 8, 0)}, 4.0F, new ItemStack[]{new ItemStack(Items.diamond, 1, 0)}, false));
        // compressed iron
        PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Items.iron_ingot, 1, 0)}, 2F, new ItemStack[]{new ItemStack(Itemss.ingotIronCompressed, 1, 0)}, false));
        PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Blocks.iron_block, 1, 0)}, 2F, new ItemStack[]{new ItemStack(Blockss.compressedIron, 1, 0)}, false));

        // turbine blade
        PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Items.redstone, 2, 0), new ItemStack(Items.gold_ingot, 1, 0)}, 1F, new ItemStack[]{new ItemStack(Itemss.turbineBlade, 1, 0)}, false));
        // plastic
        for(int i = 0; i < 16; i++) {
            PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Itemss.plasticPlant, 1, i)}, 0.5F, new ItemStack[]{new ItemStack(Itemss.plastic, 1, i)}, false));
            PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Itemss.plasticPlant, 1, i + 16)}, 0.5F, new ItemStack[]{new ItemStack(Itemss.plastic, 1, i)}, false));//TODO remove legacy
        }
        // Empty PCB
        PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.CREEPER_PLANT_DAMAGE), new ItemStack(Itemss.ingotIronCompressed, 1, 0)}, 1.5F, new ItemStack[]{new ItemStack(Itemss.emptyPCB, 1, Itemss.emptyPCB.getMaxDamage())}, false));
        // Etching Acid Bucket
        PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Itemss.plastic, 2, ItemPlasticPlants.CREEPER_PLANT_DAMAGE), new ItemStack(Items.rotten_flesh, 2, 0), new ItemStack(Items.gunpowder, 2, 0), new ItemStack(Items.spider_eye, 2, 0), new ItemStack(Items.water_bucket)}, 1.0F, new ItemStack[]{new ItemStack(Itemss.bucketEtchingAcid)}, false));
        // Transistor
        PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE), new ItemStack(Itemss.ingotIronCompressed), new ItemStack(Items.redstone)}, 1.0F, new ItemStack[]{new ItemStack(Itemss.transistor)}, false));
        // Capacitor
        PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.LIGHTNING_PLANT_DAMAGE), new ItemStack(Itemss.ingotIronCompressed), new ItemStack(Items.redstone)}, 1.0F, new ItemStack[]{new ItemStack(Itemss.capacitor)}, false));
        //Vacuum dis-enchanting
        PressureChamberRecipe.specialRecipes.add(new PressureChamberVacuumEnchantHandler());
    }

    private static void addAssemblyRecipes(){
        AssemblyRecipe.addLaserRecipe(new ItemStack(Itemss.emptyPCB, 1, Itemss.emptyPCB.getMaxDamage()), Itemss.unassembledPCB);
        AssemblyRecipe.addLaserRecipe(new ItemStack(Blockss.pressureChamberValve, 4, 0), new ItemStack(Blockss.advancedPressureTube, 8, 0));
        AssemblyRecipe.addLaserRecipe(Blocks.quartz_block, new ItemStack(Blockss.aphorismTile, 4, 0));

        AssemblyRecipe.addDrillRecipe(new ItemStack(Blockss.compressedIron, 1, 0), new ItemStack(Blockss.pressureChamberValve, 4, 0));
    }

    public static void addAssemblyCombinedRecipes(){
        calculateAssemblyChain(AssemblyRecipe.drillRecipes, AssemblyRecipe.laserRecipes, AssemblyRecipe.drillLaserRecipes);
    }

    private static void calculateAssemblyChain(List<AssemblyRecipe> firstRecipeList, List<AssemblyRecipe> secondRecipeList, List<AssemblyRecipe> totalRecipeList){
        for(AssemblyRecipe firstRecipe : firstRecipeList) {
            for(AssemblyRecipe secondRecipe : secondRecipeList) {
                if(firstRecipe.getOutput().isItemEqual(secondRecipe.getInput()) && firstRecipe.getOutput().stackSize % secondRecipe.getInput().stackSize == 0 && secondRecipe.getOutput().getMaxStackSize() >= secondRecipe.getOutput().stackSize * (firstRecipe.getOutput().stackSize / secondRecipe.getInput().stackSize)) {
                    ItemStack output = secondRecipe.getOutput().copy();
                    output.stackSize = output.stackSize * (firstRecipe.getOutput().stackSize / secondRecipe.getInput().stackSize);
                    totalRecipeList.add(new AssemblyRecipe(firstRecipe.getInput(), output));
                }
            }
        }
    }

    /**
     * Adds recipes like 9 gold ingot --> 1 gold block, and 1 gold block --> 9 gold ingots.
     */
    public static void addPressureChamberStorageBlockRecipes(){
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        for(IRecipe recipe : recipes) {
            if(recipe instanceof ShapedRecipes) {
                ShapedRecipes shaped = (ShapedRecipes)recipe;
                ItemStack[] input = shaped.recipeItems;
                ItemStack ref = input[0];
                if(ref == null || input.length < 9) continue;
                boolean valid = true;
                for(int i = 0; i < 9; i++) {
                    if(input[i] == null || !input[i].isItemEqual(ref)) {
                        valid = false;
                        break;
                    }
                }
                if(valid) {
                    ItemStack inputStack = ref.copy();
                    inputStack.stackSize = 9;
                    PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{inputStack}, 1.0F, new ItemStack[]{shaped.getRecipeOutput()}, false));

                    ItemStack inputStack2 = shaped.getRecipeOutput().copy();
                    inputStack2.stackSize = 1;
                    PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{inputStack2}, -0.5F, new ItemStack[]{inputStack}, false));

                }
            }
        }
    }
}
