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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

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
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.PRESSURE_TUBE, "pneumaticcraft:menu/pressure_tubes");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ADVANCED_PRESSURE_TUBE, "pneumaticcraft:menu/pressure_tubes");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.CANNON_BARREL, "pneumaticcraft:block/air_cannon");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.STONE_BASE, "pneumaticcraft:block/air_cannon");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.INGOT_IRON_COMPRESSED, "pneumaticcraft:menu/base_concepts");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.PRESSURE_CHAMBER_VALVE, "pneumaticcraft:menu/pressure_chamber");
        WikiRegistry.registerBlockAndItemPageEntry(new ItemStack(Blockss.PRESSURE_CHAMBER_WALL, 0, 0), "pneumaticcraft:menu/pressure_chamber");
        WikiRegistry.registerBlockAndItemPageEntry(new ItemStack(Blockss.PRESSURE_CHAMBER_WALL, 0, 6), "pneumaticcraft:menu/pressure_chamber");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ELEVATOR_BASE, "pneumaticcraft:menu/elevator");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ELEVATOR_FRAME, "pneumaticcraft:menu/elevator");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.PNEUMATIC_DOOR, "pneumaticcraft:menu/pneumatic_door");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.PNEUMATIC_DOOR_BASE, "pneumaticcraft:menu/pneumatic_door");

        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_CONTROLLER, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_DRILL, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_IO_UNIT, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_LASER, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(Blockss.ASSEMBLY_PLATFORM, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.ASSEMBLY_PROGRAM, "pneumaticcraft:menu/assembly_machines");

        for (Item upgrade : Itemss.upgrades) {
            WikiRegistry.registerBlockAndItemPageEntry(upgrade, "pneumaticcraft:menu/machine_upgrades");
        }

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.PCB_BLUEPRINT, "pneumaticcraft:menu/printed_circuit_boards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.EMPTY_PCB, "pneumaticcraft:menu/printed_circuit_boards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.UNASSEMBLED_PCB, "pneumaticcraft:menu/printed_circuit_boards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.PRINTED_CIRCUIT_BOARD, "pneumaticcraft:menu/printed_circuit_boards");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.FAILED_PCB, "pneumaticcraft:item/etching_acid_bucket");

        WikiRegistry.registerBlockAndItemPageEntry(Itemss.NETWORK_COMPONENT, "pneumaticcraft:block/security_station");

        WikiRegistry.registerEntityPageEntry(EntityLogisticsDrone.class, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_ACTIVE_PROVIDER, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_PASSIVE_PROVIDER, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_REQUESTER, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_STORAGE, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(Itemss.LOGISTICS_FRAME_DEFAULT_STORAGE, "pneumaticcraft:item/logistic_drone");

        Log.info("Loaded PneumaticCraft IGW-Mod plug-in! Thanks IGW-Mod!");
    }

    /**
     * TODO: raise a PR to get this into IGW.  This adds support for Forge filled buckets (use "item/bucket/{fluid_name}")
     *
     * @param stack
     * @return
     */
    static String getNameFromStack(ItemStack stack) {
        if (stack.getItem().getRegistryName().toString().equals("forge:bucketfilled")) {
            FluidStack fluidStack = FluidUtil.getFluidContained(stack);
            if (fluidStack != null) {
                return "item/bucket/" + fluidStack.getFluid().getName();
            } else {
                return "item/bucket";
            }
        }
        return stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/");
    }
}
