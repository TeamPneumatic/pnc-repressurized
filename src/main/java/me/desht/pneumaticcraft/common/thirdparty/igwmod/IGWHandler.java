package me.desht.pneumaticcraft.common.thirdparty.igwmod;

import igwmod.api.WikiRegistry;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.item.ItemProgrammingPuzzle;
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
        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.PRESSURE_TUBE, "pneumaticcraft:menu/pressure_tubes");
        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.ADVANCED_PRESSURE_TUBE, "pneumaticcraft:menu/pressure_tubes");

        WikiRegistry.registerBlockAndItemPageEntry(ModItems.CANNON_BARREL, "pneumaticcraft:block/air_cannon");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.STONE_BASE, "pneumaticcraft:block/air_cannon");

        WikiRegistry.registerBlockAndItemPageEntry(ModItems.INGOT_IRON_COMPRESSED, "pneumaticcraft:menu/base_concepts");

        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.PRESSURE_CHAMBER_VALVE, "pneumaticcraft:menu/pressure_chamber");
        WikiRegistry.registerBlockAndItemPageEntry(new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL, 0, 0), "pneumaticcraft:menu/pressure_chamber");
        WikiRegistry.registerBlockAndItemPageEntry(new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL, 0, 6), "pneumaticcraft:menu/pressure_chamber");

        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.ELEVATOR_BASE, "pneumaticcraft:menu/elevator");
        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.ELEVATOR_FRAME, "pneumaticcraft:menu/elevator");

        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.PNEUMATIC_DOOR, "pneumaticcraft:menu/pneumatic_door");
        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.PNEUMATIC_DOOR_BASE, "pneumaticcraft:menu/pneumatic_door");

        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.ASSEMBLY_CONTROLLER, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.ASSEMBLY_DRILL, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.ASSEMBLY_IO_UNIT, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.ASSEMBLY_LASER, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.ASSEMBLY_PLATFORM, "pneumaticcraft:menu/assembly_machines");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.ASSEMBLY_PROGRAM, "pneumaticcraft:menu/assembly_machines");

        for (Item upgrade : ModItems.upgrades) {
            WikiRegistry.registerBlockAndItemPageEntry(upgrade, "pneumaticcraft:menu/machine_upgrades");
        }

        WikiRegistry.registerBlockAndItemPageEntry(ModItems.PCB_BLUEPRINT, "pneumaticcraft:menu/printed_circuit_boards");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.EMPTY_PCB, "pneumaticcraft:menu/printed_circuit_boards");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.UNASSEMBLED_PCB, "pneumaticcraft:menu/printed_circuit_boards");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.PRINTED_CIRCUIT_BOARD, "pneumaticcraft:menu/printed_circuit_boards");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.FAILED_PCB, "pneumaticcraft:item/etching_acid_bucket");

        WikiRegistry.registerBlockAndItemPageEntry(ModItems.NETWORK_COMPONENT, "pneumaticcraft:block/security_station");

        WikiRegistry.registerEntityPageEntry(EntityLogisticsDrone.class, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.LOGISTICS_FRAME_ACTIVE_PROVIDER, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.LOGISTICS_FRAME_REQUESTER, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.LOGISTICS_FRAME_STORAGE, "pneumaticcraft:item/logistic_drone");
        WikiRegistry.registerBlockAndItemPageEntry(ModItems.LOGISTICS_FRAME_DEFAULT_STORAGE, "pneumaticcraft:item/logistic_drone");

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
        return stack.getTranslationKey().replace("tile.", "block/").replace("item.", "item/");
    }
}
