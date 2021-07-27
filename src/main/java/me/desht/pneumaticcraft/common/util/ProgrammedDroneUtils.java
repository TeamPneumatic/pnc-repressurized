package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone;
import me.desht.pneumaticcraft.common.progwidgets.*;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

public class ProgrammedDroneUtils {
    public static CreatureEntity deliverItemsAmazonStyle(GlobalPos gPos, ItemStack... deliveredStacks) {
        World world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos deliveryPos = gPos.getPos();

        if (world == null || world.isRemote) return null;
        Validate.isTrue(deliveredStacks.length > 0 && deliveredStacks.length <= 36,
                "You can only deliver between 0 & 36 stacks at once!");
        Arrays.stream(deliveredStacks).forEach(stack -> Validate.isTrue(!stack.isEmpty(),
                "You can't supply an empty stack to be delivered!"));

        EntityAmadrone drone = EntityAmadrone.makeAmadrone(world, deliveryPos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetInventoryExport inventoryExport = new ProgWidgetInventoryExport();
        inventoryExport.setSides(ISidedWidget.ALL_SIDES);
        builder.add(inventoryExport, ProgWidgetArea.fromPosition(deliveryPos));
        builder.add(new ProgWidgetDropItem(), makeDropArea(deliveryPos, drone));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        for (int i = 0; i < deliveredStacks.length; i++) {
            drone.getInv().setStackInSlot(i, deliveredStacks[i].copy());
        }
        world.addEntity(drone);
        return drone;
    }

    private static ProgWidgetArea makeDropArea(BlockPos deliveryPos, EntityAmadrone drone) {
        // this is just a suitable place to drop items at if for any reason they can't be delivered
        // (inventory full, missing, etc.)
        ProgWidgetArea area = ProgWidgetArea.fromPosition(deliveryPos);
        if (drone.isBlockValidPathfindBlock(deliveryPos)) {
            // probably means the inventory is no longer there - drop as close as possible, moving upward
            BlockPos.Mutable pos1 = deliveryPos.toMutable();
            for (int i = 0; i < 5 && drone.isBlockValidPathfindBlock(pos1); i++) {
                pos1.move(Direction.UP);
            }
            area.setPos(0, pos1.toImmutable());
        } else {
            // otherwise, try to drop 10 blocks above the ground height
            BlockPos pos1 = new BlockPos(deliveryPos.getX(), drone.world.getHeight(Heightmap.Type.WORLD_SURFACE, deliveryPos).getY() + 10, deliveryPos.getZ());
            if (drone.isBlockValidPathfindBlock(pos1)) {
                area.setPos(0, pos1);
            } else {
                // worst case scenario, go to world height and drop there
                area.setPos(0, new BlockPos(pos1.getX(), drone.world.getHeight() + 1, pos1.getZ()));
            }
        }
        return area;
    }

    public static CreatureEntity deliverFluidAmazonStyle(GlobalPos gPos, FluidStack deliveredFluid) {
        World world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.getPos();

        if (world == null || world.isRemote) return null;
        Validate.notNull(deliveredFluid, "Can't deliver a null FluidStack");
        Validate.isTrue(deliveredFluid.getAmount() > 0, "Can't deliver a FluidStack with an amount of <= 0");

        EntityAmadrone drone = EntityAmadrone.makeAmadrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetLiquidExport liquidExport = new ProgWidgetLiquidExport();
        liquidExport.setSides(ISidedWidget.ALL_SIDES);
        builder.add(liquidExport, ProgWidgetArea.fromPosition(pos));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        drone.getFluidTank().fill(deliveredFluid, IFluidHandler.FluidAction.EXECUTE);
        world.addEntity(drone);
        return drone;
    }

    public static CreatureEntity retrieveItemsAmazonStyle(GlobalPos gPos, ItemStack... queriedStacks) {
        World world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.getPos();

        if (world == null || world.isRemote) return null;
        Validate.isTrue(queriedStacks.length > 0 && queriedStacks.length <= 36, "Must retrieve between 1 & 36 itemstacks!");
        Arrays.stream(queriedStacks).forEach(stack -> Validate.isTrue(!stack.isEmpty(), "Cannot retrieve an empty stack!"));

        EntityAmadrone drone = EntityAmadrone.makeAmadrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        for (ItemStack stack : queriedStacks) {
            ProgWidgetInventoryImport widgetImport = new ProgWidgetInventoryImport();
            widgetImport.setUseCount(true);
            widgetImport.setCount(stack.getCount());
            ProgWidgetItemFilter filter = ProgWidgetItemFilter.withFilter(stack);
            filter.useNBT = stack.hasTag();
            builder.add(widgetImport, ProgWidgetArea.fromPosition(pos), filter);
        }
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        world.addEntity(drone);
        return drone;
    }

    public static CreatureEntity retrieveFluidAmazonStyle(GlobalPos gPos, FluidStack queriedFluid) {
        World world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.getPos();

        if (world == null || world.isRemote) return null;
        Validate.notNull(queriedFluid, "Can't retrieve a null FluidStack");
        Validate.isTrue(queriedFluid.getAmount() > 0, "Can't retrieve a FluidStack with an amount of <= 0");

        EntityAmadrone drone = EntityAmadrone.makeAmadrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetLiquidImport liquidImport = new ProgWidgetLiquidImport();
        liquidImport.setUseCount(true);
        liquidImport.setCount(queriedFluid.getAmount());
        builder.add(liquidImport, ProgWidgetArea.fromPosition(pos), ProgWidgetLiquidFilter.withFilter(queriedFluid.getFluid()));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        world.addEntity(drone);
        return drone;
    }
}
