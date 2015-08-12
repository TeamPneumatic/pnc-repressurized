package pneumaticCraft.common.thirdparty.igwmod;

import igwmod.api.WikiRegistry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.entity.living.EntityLogisticsDrone;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Log;

public class IGWHandler{
    public static void init(){
        WikiRegistry.registerWikiTab(new PneumaticCraftWikiTab());

        List<ItemStack> programmingPuzzles = new ArrayList<ItemStack>();
        ItemProgrammingPuzzle.addItems(programmingPuzzles);
        for(ItemStack stack : programmingPuzzles) {
            WikiRegistry.registerBlockAndItemPageEntry(stack, "pneumaticcraft:block/programmer");
        }

        WikiRegistry.registerRecipeIntegrator(new IntegratorPressureChamber());
        WikiRegistry.registerRecipeIntegrator(new IntegratorAssembly());

        //Add redirections, so if you click on a Pressure Chamber Wall you'll see the general page about Pressure Chambers.
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.pressureTube, "pneumaticcraft:menu/pressureTubes");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.advancedPressureTube, "pneumaticcraft:menu/pressureTubes");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.cannonBarrel, "pneumaticcraft:block/airCannon");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.stoneBase, "pneumaticcraft:block/airCannon");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.ingotIronCompressed, "pneumaticcraft:menu/baseConcepts");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.pressureChamberValve, "pneumaticcraft:menu/pressureChamber");
        WikiRegistry.registerBlockAndItemPageEntry(new ItemStack(Blockss.pressureChamberWall, 0, 0), "pneumaticcraft:menu/pressureChamber");
        WikiRegistry.registerBlockAndItemPageEntry(new ItemStack(Blockss.pressureChamberWall, 0, 6), "pneumaticcraft:menu/pressureChamber");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.elevatorBase, "pneumaticcraft:menu/elevator");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.elevatorFrame, "pneumaticcraft:menu/elevator");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.pneumaticDoor, "pneumaticcraft:menu/pneumaticDoor");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.pneumaticDoorBase, "pneumaticcraft:menu/pneumaticDoor");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.assemblyController, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.assemblyDrill, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.assemblyIOUnit, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.assemblyLaser, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.assemblyPlatform, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.assemblyProgram, "pneumaticcraft:menu/assemblyMachines");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.machineUpgrade, "pneumaticcraft:menu/machineUpgrades");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.PCBBlueprint, "pneumaticcraft:menu/printedCircuitBoards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.emptyPCB, "pneumaticcraft:menu/printedCircuitBoards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.unassembledPCB, "pneumaticcraft:menu/printedCircuitBoards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.printedCircuitBoard, "pneumaticcraft:menu/printedCircuitBoards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.failedPCB, "pneumaticcraft:item/etchingAcidBucket");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.networkComponent, "pneumaticcraft:block/securityStation");

        WikiRegistry.registerEntityPageEntry(EntityLogisticsDrone.class, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.logisticsFrameActiveProvider, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.logisticsFramePassiveProvider, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.logisticsFrameRequester, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.logisticsFrameStorage, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.logisticsFrameDefaultStorage, "pneumaticcraft:item/logisticDrone");

        Log.info("Loaded PneumaticCraft IGW-Mod plug-in! Thanks IGW-Mod!");
    }
}
