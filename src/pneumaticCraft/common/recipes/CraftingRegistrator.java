package pneumaticCraft.common.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.apache.commons.lang3.tuple.ImmutablePair;

import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.api.recipe.IPneumaticRecipeRegistry;
import pneumaticCraft.api.recipe.PressureChamberRecipe;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.ItemNetworkComponents;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.semiblock.SemiBlockHeatFrame;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import pneumaticCraft.lib.Names;
import cpw.mods.fml.common.registry.GameRegistry;

public class CraftingRegistrator{
    public static void init(){
        ItemStack lapis = new ItemStack(Items.dye, 1, 4);
        ItemStack cobbleSlab = new ItemStack(Blocks.stone_slab, 1, 3);
        // tubes
        addRecipe(new ItemStack(Blockss.pressureTube, 8, 0 /* normal */), "igi", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass");
        addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_FLOW_DETECTOR)), "bbb", "btb", "bbb", 'b', Itemss.turbineBlade, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_SAFETY_VALVE)), " g ", "ltl", 'g', Itemss.pressureGauge, 'l', Blocks.lever, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_REGULATOR)), "sts", 's', ModuleRegistrator.getModuleItem(Names.MODULE_SAFETY_VALVE), 't', new ItemStack(Blockss.pressureTube, 1, 0));
        addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_AIR_GRATE)), " b ", "btb", " b ", 'b', Blocks.iron_bars, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_GAUGE)), " g ", "rtr", 'g', Itemss.pressureGauge, 'r', Items.redstone, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_CHARGING)), " r ", "rtr", " r ", 'r', Blockss.chargingStation, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_LOGISTICS), 4), "pip", "iri", "pip", 'i', Names.INGOT_IRON_COMPRESSED, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE), 'r', ModuleRegistrator.getModuleItem(Names.MODULE_REGULATOR));
        addRecipe(new ItemStack(ModuleRegistrator.getModuleItem(Names.MODULE_LOGISTICS), 4), "ipi", "prp", "ipi", 'i', Names.INGOT_IRON_COMPRESSED, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE), 'r', ModuleRegistrator.getModuleItem(Names.MODULE_REGULATOR));

        // tube addons
        addRecipe(new ItemStack(Itemss.pressureGauge), " g ", "gig", " g ", 'g', Items.gold_ingot, 'i', Names.INGOT_IRON_COMPRESSED);

        // pressure chamber
        addRecipe(new ItemStack(Blockss.pressureChamberWall, 16, 0), "iii", "i i", "iii", 'i', Names.INGOT_IRON_COMPRESSED);
        addRecipe(new ItemStack(Blockss.pressureChamberWall, 16, 6), "iii", "igi", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass");
        addShapelessRecipe(new ItemStack(Blockss.pressureChamberWall, 4, 6), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), Blocks.glass);
        addRecipe(new ItemStack(Blockss.pressureChamberValve, 16, 0), "iii", "iti", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        addShapelessRecipe(new ItemStack(Blockss.pressureChamberValve, 4, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureChamberWall, 1, 0), new ItemStack(Blockss.pressureTube, 1, 0));
        addShapelessRecipe(new ItemStack(Blockss.pressureChamberInterface), Blockss.pressureChamberWall, Blocks.hopper);
        addShapelessRecipe(new ItemStack(Blockss.pressureChamberValve, 1, 0), Blockss.pressureChamberWall, Blockss.pressureTube);
        addShapelessRecipe(new ItemStack(Blockss.pressureChamberWall, 1, 6), new ItemStack(Blockss.pressureChamberWall, 1, 0), Blocks.glass);

        //Oil related
        addRecipe(new ItemStack(Itemss.seismicSensor), " t ", "grg", "gcg", 't', Blocks.redstone_torch, 'g', "blockGlass", 'r', Items.repeater, 'c', Items.coal);
        addRecipe(new ItemStack(Blockss.refinery), "iii", "gdg", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass", 'd', "gemDiamond");
        addRecipe(new ItemStack(Blockss.thermopneumaticProcessingPlant), "igi", "tri", "igi", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass", 'r', "dustRedstone", 't', Blockss.pressureTube);
        addRecipe(new ItemStack(Blockss.gasLift), " t ", "tgt", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass", 't', Blockss.pressureTube);

        // cannon
        addRecipe(new ItemStack(Blockss.airCannon), " b ", " st", "hhh", 'b', Itemss.cannonBarrel, 's', Itemss.stoneBase, 't', new ItemStack(Blockss.pressureTube, 1, 0 /* normal */), 'h', cobbleSlab);
        addRecipe(new ItemStack(Itemss.stoneBase), "s s", "sts", 's', Blocks.stone, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        addRecipe(new ItemStack(Itemss.cannonBarrel), true, "i i", "i i", "pii", 'i', Names.INGOT_IRON_COMPRESSED, 'p', ModuleRegistrator.getModuleItem(Names.MODULE_SAFETY_VALVE));

        addRecipe(new ItemStack(Itemss.GPSTool), " r ", "pgp", "pdp", 'r', Blocks.redstone_torch, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE), 'g', Blocks.glass_pane, 'd', Items.diamond);
        addRecipe(new ItemStack(Itemss.remote), " i ", "tgt", "tdt", 'i', new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_IO_PORT), 't', Itemss.transistor, 'g', Itemss.GPSTool, 'd', new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_DATA_STORAGE));

        // compressor
        addRecipe(new ItemStack(Blockss.airCompressor), true, "iii", "i t", "ifi", 'i', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.pressureTube, 1, 0), 'f', Blocks.furnace);
        addRecipe(new ItemStack(Blockss.advancedAirCompressor), true, "iii", "i t", "ifi", 'i', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.advancedPressureTube, 1, 0), 'f', Blockss.airCompressor);
        addRecipe(new ItemStack(Blockss.liquidCompressor), true, "pbp", "lcl", 'p', Blockss.pressureTube, 'l', Items.leather, 'b', Items.bucket, 'c', Blockss.airCompressor);
        addRecipe(new ItemStack(Blockss.advancedLiquidCompressor), true, "iii", "ibt", "ici", 'i', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.RAIN_PLANT_DAMAGE), 'b', Items.bucket, 'c', Blockss.liquidCompressor, 't', Blockss.advancedPressureTube);
        addRecipe(new ItemStack(Blockss.electrostaticCompressor), "bpb", "prp", "bcb", 'b', Blocks.iron_bars, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'r', Itemss.turbineRotor, 'c', Blockss.airCompressor);

        // Charging Station
        addRecipe(new ItemStack(Blockss.chargingStation), "  t", "ppp", "sss", 's', cobbleSlab, 't', new ItemStack(Blockss.pressureTube, 1, 0), 'p', Items.brick);

        addRecipe(new ItemStack(Blockss.elevatorFrame, 4, 0), "i i", "i i", "i i", 'i', Names.INGOT_IRON_COMPRESSED);
        addRecipe(new ItemStack(Itemss.pneumaticCylinder), "pip", "pip", "pbp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.RAIN_PLANT_DAMAGE), 'i', Names.INGOT_IRON_COMPRESSED, 'b', Itemss.cannonBarrel);
        addRecipe(new ItemStack(Blockss.elevatorBase, 4, 0), "cp", "pc", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'c', Itemss.pneumaticCylinder);
        addRecipe(new ItemStack(Blockss.elevatorBase, 4, 0), "pc", "cp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'c', Itemss.pneumaticCylinder);
        addRecipe(new ItemStack(Blockss.elevatorCaller, 1, 0), "cpc", "prp", "cpc", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SLIME_PLANT_DAMAGE), 'c', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FLYING_FLOWER_DAMAGE), 'r', Items.redstone);
        addRecipe(new ItemStack(Blockss.elevatorCaller, 1, 0), "cpc", "prp", "cpc", 'c', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SLIME_PLANT_DAMAGE), 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FLYING_FLOWER_DAMAGE), 'r', Items.redstone);

        //Security Station
        addRecipe(new ItemStack(Blockss.securityStation), "gbg", "tpt", "ggg", 'g', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'b', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE), 't', Itemss.turbineRotor, 'p', Itemss.printedCircuitBoard);
        addRecipe(new ItemStack(Itemss.networkComponent, 16, ItemNetworkComponents.NETWORK_NODE), "ttt", "tct", "ttt", 't', Itemss.transistor, 'c', Blocks.chest);
        addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_IO_PORT), "ttt", "tct", "ttt", 't', Itemss.capacitor, 'c', Blocks.chest);
        addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_REGISTRY), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.REPULSION_PLANT_DAMAGE), 'c', Blocks.chest);
        addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.DIAGNOSTIC_SUBROUTINE), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE), 'c', Blocks.chest);
        addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_API), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.RAIN_PLANT_DAMAGE), 'c', Blocks.chest);
        addRecipe(new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_DATA_STORAGE), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'c', Blocks.chest);

        // Machine Upgrades
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 0), "lil", "ici", "lil", 'l', lapis, 'i', Names.INGOT_IRON_COMPRESSED, 'c', new ItemStack(Itemss.airCanister, 1, OreDictionary.WILDCARD_VALUE));
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 1), "lil", "idi", "lil", 'l', lapis, 'i', Items.quartz, 'd', Blocks.dispenser);
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 2), "lal", "aca", "lal", 'l', lapis, 'a', Items.apple, 'c', Items.clock);
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 3), "lbl", "bsb", "lbl", 'l', lapis, 'b', Items.bone, 's', Items.fermented_spider_eye);
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 4), "lwl", "wsw", "lwl", 'l', lapis, 'w', Blockss.pressureChamberWall, 's', Items.fermented_spider_eye);
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 5), "lsl", "scs", "lsl", 'l', lapis, 's', Items.sugar, 'c', Fluids.getBucket(Fluids.lubricant));
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 6), "lel", "ege", "lel", 'l', lapis, 'e', Items.ender_eye, 'g', Items.golden_carrot);
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 7), "lrl", "rgr", "lrl", 'l', lapis, 'r', Items.redstone, 'g', Itemss.GPSTool);
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 8), "lal", "aba", "lal", 'l', lapis, 'a', Items.arrow, 'b', Items.bow);
        addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 9), "lol", "obo", "lol", 'l', lapis, 'o', Blocks.obsidian, 'b', ModuleRegistrator.getModuleItem("safetyTubeModule"));

        addRecipe(new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()), " t ", "iri", "iri", 'i', Names.INGOT_IRON_COMPRESSED, 'r', Items.redstone, 't', new ItemStack(Blockss.pressureTube, 1, 0));

        addRecipe(new ItemStack(Itemss.turbineRotor), " b ", " i ", "b b", 'i', Names.INGOT_IRON_COMPRESSED, 'b', Itemss.turbineBlade);
        addRecipe(new ItemStack(Blockss.vacuumPump), "grg", "trt", "sss", 'g', Itemss.pressureGauge, 'r', Itemss.turbineRotor, 's', cobbleSlab, 't', new ItemStack(Blockss.pressureTube, 1, 0));

        // NEI support recipes
        addRecipe(new ItemStack(Itemss.vortexCannon, 1, Itemss.vortexCannon.getMaxDamage()), "idi", "c  ", "ili", 'd', "dyeYellow", 'i', Itemss.ingotIronCompressed, 'l', Blocks.lever, 'c', new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()));
        addRecipe(new ItemStack(Itemss.pneumaticWrench, 1, Itemss.pneumaticWrench.getMaxDamage()), "idi", "c  ", "ili", 'd', "dyeOrange", 'i', Itemss.ingotIronCompressed, 'l', Blocks.lever, 'c', new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()));
        addRecipe(new ItemStack(Itemss.logisticsConfigurator, 1, Itemss.logisticsConfigurator.getMaxDamage()), "idi", "c  ", "ili", 'd', "dyeRed", 'i', Itemss.ingotIronCompressed, 'l', Blocks.lever, 'c', new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()));
        addRecipe(new ItemStack(Itemss.amadronTablet, 1, Itemss.amadronTablet.getMaxDamage()), "ppp", "pgp", "pcp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'g', Itemss.GPSTool, 'c', new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()));
        addRecipe(new ItemStack(Itemss.pneumaticHelmet, 1), "cec", "c c", 'e', Itemss.printedCircuitBoard, 'c', new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()));
        addShapelessRecipe(new ItemStack(Itemss.manometer, 1, Itemss.manometer.getMaxDamage()), new ItemStack(Itemss.airCanister, 1, Itemss.airCanister.getMaxDamage()), Itemss.pressureGauge);

        // Pneumatic Items
        GameRegistry.addRecipe(new RecipeGun("dyeYellow", Itemss.vortexCannon));
        GameRegistry.addRecipe(new RecipeGun("dyeOrange", Itemss.pneumaticWrench));
        GameRegistry.addRecipe(new RecipeGun("dyeRed", Itemss.logisticsConfigurator));
        GameRegistry.addRecipe(new RecipePneumaticHelmet());
        GameRegistry.addRecipe(new RecipeManometer());
        GameRegistry.addRecipe(new RecipeColorDrone());
        GameRegistry.addRecipe(new RecipeLogisticToDrone());
        GameRegistry.addRecipe(new RecipeGunAmmo());
        GameRegistry.addRecipe(new RecipeAmadronTablet());

        RecipeSorter.register("pneumaticcraft:gun", RecipeGun.class, Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("pneumaticcraft:amadronTablet", RecipeAmadronTablet.class, Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("pneumaticcraft:pneumaticHelmet", RecipePneumaticHelmet.class, Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("pneumaticcraft:manometer", RecipeManometer.class, Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("pneumaticcraft:colorDrone", RecipeColorDrone.class, Category.SHAPELESS, "after:minecraft:shapeless");
        RecipeSorter.register("pneumaticcraft:logisticToDrone", RecipeLogisticToDrone.class, Category.SHAPELESS, "after:minecraft:shapeless");
        RecipeSorter.register("pneumaticcraft:gunAmmo", RecipeGunAmmo.class, Category.SHAPELESS, "after:minecraft:shapeless");
        RecipeSorter.register("pneumaticcraft:fluid", RecipeFluid.class, Category.SHAPED, "after:minecraft:shaped");

        //Heat related
        addRecipe(new ItemStack(Blockss.heatSink), "bbb", "igi", 'i', Names.INGOT_IRON_COMPRESSED, 'b', Blocks.iron_bars, 'g', "ingotGold");
        addRecipe(new ItemStack(Blockss.vortexTube), "iti", "gtg", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "ingotGold", 't', Blockss.pressureTube);
        addRecipe(new ItemStack(SemiBlockManager.getItemForSemiBlock(SemiBlockHeatFrame.class)), "iii", "ifi", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'f', Blocks.furnace);
        //misc
        addRecipe(new ItemStack(Blockss.compressedIron), "iii", "iii", "iii", 'i', Names.INGOT_IRON_COMPRESSED);
        addShapelessRecipe(new ItemStack(Itemss.ingotIronCompressed, 9, 0), Names.BLOCK_IRON_COMPRESSED);

        addShapelessRecipe(new ItemStack(Itemss.printedCircuitBoard), Itemss.unassembledPCB, Itemss.transistor, Itemss.transistor, Itemss.transistor, Itemss.capacitor, Itemss.capacitor, Itemss.capacitor);
        addRecipe(new ItemStack(Itemss.advancedPCB), "rpr", "pcp", "rpr", 'c', Itemss.printedCircuitBoard, 'r', Items.redstone, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.HELIUM_PLANT_DAMAGE));
        addRecipe(new ItemStack(Itemss.advancedPCB), "prp", "rcr", "prp", 'c', Itemss.printedCircuitBoard, 'r', Items.redstone, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.HELIUM_PLANT_DAMAGE));
        addRecipe(new ItemStack(Blockss.uvLightBox), "lll", "ibt", "iii", 'l', Blocks.redstone_lamp, 'b', Itemss.PCBBlueprint, 'i', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.pressureTube, 1, 0));
        addShapelessRecipe(new ItemStack(Itemss.gunAmmo), new ItemStack(Items.gunpowder), Names.INGOT_IRON_COMPRESSED, "ingotGold");
        addRecipe(new ItemStack(Blockss.sentryTurret), " m ", "pip", "i i", 'm', Itemss.minigun, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE), 'i', Names.INGOT_IRON_COMPRESSED);
        addRecipe(new ItemStack(Itemss.minigun), "cib", "g  ", 'c', Names.BLOCK_IRON_COMPRESSED, 'i', Names.INGOT_IRON_COMPRESSED, 'b', Itemss.cannonBarrel, 'g', "ingotGold");

        //Assembly Machines
        addRecipe(new ItemStack(Blockss.assemblyDrill), true, "dcc", "  c", "ipi", 'd', Items.diamond, 'c', Itemss.pneumaticCylinder, 'i', Names.INGOT_IRON_COMPRESSED, 'p', Itemss.printedCircuitBoard);
        addRecipe(new ItemStack(Blockss.assemblyLaser), true, "dcc", "  c", "ipi", 'd', new ItemStack(Items.dye, 1, 1), 'c', Itemss.pneumaticCylinder, 'i', Names.INGOT_IRON_COMPRESSED, 'p', Itemss.printedCircuitBoard);
        addRecipe(new ItemStack(Blockss.assemblyIOUnit), true, "hcc", "  c", "ipi", 'h', Blocks.hopper, 'c', Itemss.pneumaticCylinder, 'i', Names.INGOT_IRON_COMPRESSED, 'p', Itemss.printedCircuitBoard);
        addRecipe(new ItemStack(Blockss.assemblyPlatform), true, "a a", "ppp", "ici", 'a', Itemss.pneumaticCylinder, 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.PROPULSION_PLANT_DAMAGE), 'i', Names.INGOT_IRON_COMPRESSED, 'c', Itemss.printedCircuitBoard);
        addRecipe(new ItemStack(Blockss.assemblyController), true, " c ", "tcc", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'c', Itemss.printedCircuitBoard, 't', new ItemStack(Blockss.pressureTube, 1, 0));

        GameRegistry.addSmelting(Itemss.failedPCB, new ItemStack(Itemss.emptyPCB, 1, Itemss.emptyPCB.getMaxDamage()), 0);

        addRecipe(new ItemStack(Blockss.pneumaticDoor), "cc", "cc", "cc", 'c', Names.INGOT_IRON_COMPRESSED);
        addRecipe(new ItemStack(Blockss.pneumaticDoorBase), true, " #c", "cct", "ccc", '#', Itemss.pneumaticCylinder, 'c', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.pressureTube, 1, 0));

        for(int i = 0; i < 16; i++) {
            addShapelessRecipe(new ItemStack(Items.dye, 1, i), new ItemStack(Itemss.plasticPlant, 1, i));
        }
        addRecipe(new ItemStack(Blockss.universalSensor), "plp", "lpl", "pcp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.ENDER_PLANT_DAMAGE), 'l', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.CHOPPER_PLANT_DAMAGE), 'c', "dustRedstone");
        addRecipe(new ItemStack(Blockss.aerialInterface), "whw", "ese", "wtw", 'w', Blockss.pressureChamberWall, 'h', Blocks.hopper, 'e', Items.ender_pearl, 's', new ItemStack(Items.skull, 1, 1), 't', new ItemStack(Blockss.advancedPressureTube, 1, 0));
        addRecipe(new ItemStack(Blockss.omnidirectionalHopper), "i i", "ici", " i ", 'i', Names.INGOT_IRON_COMPRESSED, 'c', Blocks.chest);
        addRecipe(new ItemStack(Blockss.liquidHopper), "i i", "ici", " i ", 'i', "blockGlass", 'c', Blocks.hopper);

        addRecipe(new ItemStack(Blockss.plasticMixer), "igi", "g g", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass");
        addRecipe(new ItemStack(Blockss.keroseneLamp), " i ", "g g", "ibi", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass", 'b', Items.bucket);

        addRecipe(new ItemStack(Itemss.drone), " b ", "bcb", " b ", 'b', Itemss.turbineRotor, 'c', Itemss.printedCircuitBoard);
        addRecipe(new ItemStack(Blockss.programmableController), "iri", "cdp", "ini", 'i', Names.INGOT_IRON_COMPRESSED, 'c', Itemss.printedCircuitBoard, 'r', Itemss.remote, 'd', Itemss.drone, 'p', Blockss.advancedPressureTube, 'n', new ItemStack(Itemss.networkComponent, 1, ItemNetworkComponents.NETWORK_REGISTRY));

        addRecipe(new ItemStack(Blockss.programmer), "gbg", "tpt", "ggg", 'g', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE), 'b', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE), 't', Itemss.turbineRotor, 'p', Itemss.printedCircuitBoard);

        addRecipe(new ItemStack(Itemss.logisticsDrone), " b ", "bcb", " b ", 'b', Itemss.turbineRotor, 'c', "dustRedstone");
        addRecipe(new ItemStack(Itemss.logisticsFramePassiveProvider), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE));
        addRecipe(new ItemStack(Itemss.logisticsFrameActiveProvider), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.ENDER_PLANT_DAMAGE));
        addRecipe(new ItemStack(Itemss.logisticsFrameRequester), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.RAIN_PLANT_DAMAGE));
        addRecipe(new ItemStack(Itemss.logisticsFrameStorage), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.HELIUM_PLANT_DAMAGE));
        addRecipe(new ItemStack(Itemss.logisticsFrameDefaultStorage), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.REPULSION_PLANT_DAMAGE));

        addShapelessRecipe(new ItemStack(Itemss.assemblyProgram, 1, 2), new ItemStack(Itemss.assemblyProgram, 1, 0), new ItemStack(Itemss.assemblyProgram, 1, 1));

        addPressureChamberRecipes();
        addAssemblyRecipes();
        addThermopneumaticProcessingPlantRecipes();
        registerAmadronOffers();
        addCoolingRecipes();
    }

    public static void addProgrammingPuzzleRecipes(){
        List<ItemStack> widgets = new ArrayList<ItemStack>();
        ItemProgrammingPuzzle.addItems(widgets);
        for(ItemStack output : widgets) {
            output.stackSize = 4;
            addRecipe(output, "ppp", "pcp", "ppp", 'p', new ItemStack(Itemss.plastic, 1, output.getItemDamage()), 'c', Itemss.printedCircuitBoard);
        }
    }

    private static void addPressureChamberRecipes(){
        IPneumaticRecipeRegistry registry = PneumaticRegistry.getInstance().getRecipeRegistry();
        // diamond
        if(Config.enableCoalToDiamondsRecipe) registry.registerPressureChamberRecipe(new ItemStack[]{new ItemStack(Blocks.coal_block, 8, 0)}, 4.0F, new ItemStack[]{new ItemStack(Items.diamond, 1, 0)});
        // compressed iron
        registry.registerPressureChamberRecipe(new Object[]{new ImmutablePair("ingotIron", 1)}, 2F, new ItemStack[]{new ItemStack(Itemss.ingotIronCompressed, 1, 0)});
        registry.registerPressureChamberRecipe(new Object[]{new ImmutablePair("blockIron", 1)}, 2F, new ItemStack[]{new ItemStack(Blockss.compressedIron, 1, 0)});

        // turbine blade
        registry.registerPressureChamberRecipe(new Object[]{new ImmutablePair("dustRedstone", 2), new ImmutablePair("ingotGold", 1)}, 1F, new ItemStack[]{new ItemStack(Itemss.turbineBlade, 1, 0)});
        // plastic
        for(int i = 0; i < 16; i++) {
            registry.registerPressureChamberRecipe(new ItemStack[]{new ItemStack(Itemss.plasticPlant, 1, i)}, 0.5F, new ItemStack[]{new ItemStack(Itemss.plastic, 1, i)});
        }
        // Empty PCB
        registry.registerPressureChamberRecipe(new Object[]{new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.CREEPER_PLANT_DAMAGE), new ImmutablePair(Names.INGOT_IRON_COMPRESSED, 1)}, 1.5F, new ItemStack[]{new ItemStack(Itemss.emptyPCB, 1, Itemss.emptyPCB.getMaxDamage())});
        // Etching Acid Bucket
        registry.registerPressureChamberRecipe(new ItemStack[]{new ItemStack(Itemss.plastic, 2, ItemPlasticPlants.CREEPER_PLANT_DAMAGE), new ItemStack(Items.rotten_flesh, 2, 0), new ItemStack(Items.gunpowder, 2, 0), new ItemStack(Items.spider_eye, 2, 0), new ItemStack(Items.water_bucket)}, 1.0F, new ItemStack[]{new ItemStack(Fluids.getBucket(Fluids.etchingAcid))});
        // Transistor
        registry.registerPressureChamberRecipe(new Object[]{new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE), new ImmutablePair("ingotIronCompressed", 1), new ImmutablePair("dustRedstone", 1)}, 1.0F, new ItemStack[]{new ItemStack(Itemss.transistor)});
        // Capacitor
        registry.registerPressureChamberRecipe(new Object[]{new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.LIGHTNING_PLANT_DAMAGE), new ImmutablePair("ingotIronCompressed", 1), new ImmutablePair("dustRedstone", 1)}, 1.0F, new ItemStack[]{new ItemStack(Itemss.capacitor)});
        //Vacuum dis-enchanting
        registry.registerPressureChamberRecipe(new PressureChamberVacuumEnchantHandler());
    }

    private static void addAssemblyRecipes(){
        AssemblyRecipe.addLaserRecipe(new ItemStack(Itemss.emptyPCB, 1, Itemss.emptyPCB.getMaxDamage()), Itemss.unassembledPCB);
        AssemblyRecipe.addLaserRecipe(new ItemStack(Blockss.pressureChamberValve, 20, 0), new ItemStack(Blockss.advancedPressureTube, 8, 0));
        AssemblyRecipe.addLaserRecipe(Blocks.quartz_block, new ItemStack(Blockss.aphorismTile, 4, 0));

        AssemblyRecipe.addDrillRecipe(new ItemStack(Blockss.compressedIron, 1, 0), new ItemStack(Blockss.pressureChamberValve, 20, 0));
        AssemblyRecipe.addDrillRecipe(new ItemStack(Items.redstone, 1, 0), new ItemStack(Items.dye, 5, 1));
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

    public static void addThermopneumaticProcessingPlantRecipes(){
        PneumaticRecipeRegistry registry = PneumaticRecipeRegistry.getInstance();
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.lpg, 100), new ItemStack(Items.coal), new FluidStack(Fluids.plastic, 1000), 373, 0);
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.diesel, 1000), new ItemStack(Items.redstone), new FluidStack(Fluids.lubricant, 1000), 373, 0);
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.diesel, 100), null, new FluidStack(Fluids.kerosene, 80), 573, 2);
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.kerosene, 100), null, new FluidStack(Fluids.gasoline, 80), 573, 2);
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.gasoline, 100), null, new FluidStack(Fluids.lpg, 80), 573, 2);
    }

    private static void registerAmadronOffers(){
        PneumaticRecipeRegistry registry = PneumaticRecipeRegistry.getInstance();
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.emerald, 8), new ItemStack(Itemss.PCBBlueprint));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.emerald, 8), new ItemStack(Itemss.assemblyProgram, 1, 0));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.emerald, 8), new ItemStack(Itemss.assemblyProgram, 1, 1));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.emerald, 14), new ItemStack(Itemss.assemblyProgram, 1, 2));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.oil, 5000), new ItemStack(Items.emerald, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.diesel, 4000), new ItemStack(Items.emerald, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.lubricant, 2500), new ItemStack(Items.emerald, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.kerosene, 3000), new ItemStack(Items.emerald, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.gasoline, 2000), new ItemStack(Items.emerald, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.lpg, 1000), new ItemStack(Items.emerald, 1));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.emerald), new FluidStack(Fluids.oil, 1000));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.emerald, 5), new FluidStack(Fluids.lubricant, 1000));

        for(int i = 0; i < 256; i++) {
            try {
                for(int j = 0; j < 10; j++) {
                    EntityVillager villager = new EntityVillager(null, i);
                    MerchantRecipeList list = villager.getRecipes(null);
                    for(MerchantRecipe recipe : (List<MerchantRecipe>)list) {
                        if(recipe.getSecondItemToBuy() == null && recipe.getItemToBuy() != null && recipe.getItemToBuy().getItem() != null && recipe.getItemToSell() != null && recipe.getItemToSell().getItem() != null) {
                            registry.registerDefaultPeriodicAmadronOffer(recipe.getItemToBuy(), recipe.getItemToSell());
                        }
                    }
                }
            } catch(Throwable e) {}
        }
    }

    private static void addCoolingRecipes(){
        PneumaticRecipeRegistry registry = PneumaticRecipeRegistry.getInstance();
        registry.registerHeatFrameCoolRecipe(new ItemStack(Items.water_bucket), new ItemStack(Blocks.ice));
        registry.registerHeatFrameCoolRecipe(new ItemStack(Items.lava_bucket), new ItemStack(Blocks.obsidian));
    }

    private static void addRecipe(ItemStack result, Object... recipe){
        ShapedOreRecipe newRecipe = new ShapedOreRecipe(result, recipe);
        GameRegistry.addRecipe(newRecipe);
        scanForFluids(newRecipe);
    }

    public static void addShapelessRecipe(ItemStack result, Object... recipe){
        GameRegistry.addRecipe(new ShapelessOreRecipe(result, recipe));
    }

    private static void scanForFluids(ShapedOreRecipe recipe){
        for(int i = 0; i < recipe.getRecipeSize(); i++) {
            Object o = recipe.getInput()[i];
            if(o instanceof ItemStack) {
                ItemStack stack = (ItemStack)o;
                FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
                if(fluid != null) {
                    GameRegistry.addRecipe(new RecipeFluid(recipe, i));
                }
            }
        }
    }
}
