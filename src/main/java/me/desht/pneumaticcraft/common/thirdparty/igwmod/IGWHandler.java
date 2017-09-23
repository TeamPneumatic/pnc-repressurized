package me.desht.pneumaticcraft.common.thirdparty.igwmod;

import igwmod.api.WikiRegistry;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.item.ItemProgrammingPuzzle;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class IGWHandler {
    public static void init() {
        WikiRegistry.registerWikiTab(new PneumaticCraftWikiTab());

        NonNullList<ItemStack> programmingPuzzles = NonNullList.create();
        ItemProgrammingPuzzle.addItems(programmingPuzzles);
        for (ItemStack stack : programmingPuzzles) {
            WikiRegistry.registerBlockAndItemPageEntry(stack, "pneumaticcraft:block/programmer");
        }

        WikiRegistry.registerRecipeIntegrator(new IntegratorPressureChamber());
        WikiRegistry.registerRecipeIntegrator(new IntegratorAssembly());

        //Add redirections, so if you click on a Pressure Chamber Wall you'll see the general page about Pressure Chambers.
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.PRESSURE_TUBE, "pneumaticcraft:menu/pressureTubes");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ADVANCED_PRESSURE_TUBE, "pneumaticcraft:menu/pressureTubes");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.cannonBarrel, "pneumaticcraft:block/airCannon");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.STONE_BASE, "pneumaticcraft:block/airCannon");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.INGOT_IRON_COMPRESSED, "pneumaticcraft:menu/baseConcepts");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.PRESSURE_CHAMBER_VALVE, "pneumaticcraft:menu/pressureChamber");
        WikiRegistry.registerBlockAndItemPageEntry(new ItemStack(Blockss.PRESSURE_CHAMBER_WALL, 0, 0), "pneumaticcraft:menu/pressureChamber");
        WikiRegistry.registerBlockAndItemPageEntry(new ItemStack(Blockss.PRESSURE_CHAMBER_WALL, 0, 6), "pneumaticcraft:menu/pressureChamber");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ELEVATOR_BASE, "pneumaticcraft:menu/elevator");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ELEVATOR_FRAME, "pneumaticcraft:menu/elevator");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.PNEUMATIC_DOOR, "pneumaticcraft:menu/pneumaticDoor");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.PNEUMATIC_DOOR_BASE, "pneumaticcraft:menu/pneumaticDoor");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_CONTROLLER, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_DRILL, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_IO_UNIT, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_LASER, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_PLATFORM, "pneumaticcraft:menu/assemblyMachines");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.ASSEMBLY_PROGRAM, "pneumaticcraft:menu/assemblyMachines");

        for (Item upgrade : Itemss.upgrades.values()) {
            WikiRegistry.registerBlockAndItemPageEntry(upgrade, "pneumaticcraft:menu/machineUpgrades");
        }

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.PCB_BLUEPRINT, "pneumaticcraft:menu/printedCircuitBoards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.EMPTY_PCB, "pneumaticcraft:menu/printedCircuitBoards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.UNASSEMBLED_PCB, "pneumaticcraft:menu/printedCircuitBoards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.PRINTED_CIRCUIT_BOARD, "pneumaticcraft:menu/printedCircuitBoards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.FAILED_PCB, "pneumaticcraft:item/etchingAcidBucket");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.NETWORK_COMPONENT, "pneumaticcraft:block/securityStation");

        WikiRegistry.registerEntityPageEntry(EntityLogisticsDrone.class, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_ACTIVE_PROVIDER, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_PASSIVE_PROVIDER, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_REQUESTER, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_STORAGE, "pneumaticcraft:item/logisticDrone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_DEFAULT_STORAGE, "pneumaticcraft:item/logisticDrone");

        Log.info("Loaded PneumaticCraft IGW-Mod plug-in! Thanks IGW-Mod!");
    }
}
