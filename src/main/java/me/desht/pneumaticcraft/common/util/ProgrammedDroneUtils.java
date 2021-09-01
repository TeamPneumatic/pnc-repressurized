package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone;
import me.desht.pneumaticcraft.common.progwidgets.*;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.item.ItemStack;
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
        BlockPos pos = gPos.pos();

        if (world == null || world.isClientSide) return null;
        Validate.isTrue(deliveredStacks.length > 0 && deliveredStacks.length <= 36,
                "You can only deliver between 0 & 36 stacks at once!");
        Arrays.stream(deliveredStacks).forEach(stack -> Validate.isTrue(!stack.isEmpty(),
                "You can't supply an empty stack to be delivered!"));

        EntityAmadrone drone = EntityAmadrone.makeAmadrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetInventoryExport inventoryExport = new ProgWidgetInventoryExport();
        inventoryExport.setSides(ISidedWidget.ALL_SIDES);
        builder.add(inventoryExport, ProgWidgetArea.fromPosition(pos));
        ProgWidgetArea area = ProgWidgetArea.fromPosition(pos);
        if (drone.isBlockValidPathfindBlock(pos)) {
            for (int i = 0; i < 5 && drone.isBlockValidPathfindBlock(new BlockPos(area.x1, area.y1, area.z1)); i++) {
                area.y1 = pos.getY() + i;
            }
        } else {
            area.y1 = world.getHeightmapPos(Heightmap.Type.WORLD_SURFACE, pos).getY() + 10;
            if (!drone.isBlockValidPathfindBlock(new BlockPos(area.x1, area.y1, area.z1)))
                area.y1 = 260; // Worst case scenario; there are definitely no blocks here.
        }
        builder.add(new ProgWidgetDropItem(), area);
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.blockPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        for (int i = 0; i < deliveredStacks.length; i++) {
            drone.getInv().setStackInSlot(i, deliveredStacks[i].copy());
        }
        world.addFreshEntity(drone);
        return drone;
    }

    public static CreatureEntity deliverFluidAmazonStyle(GlobalPos gPos, FluidStack deliveredFluid) {
        World world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.pos();

        if (world == null || world.isClientSide) return null;
        Validate.notNull(deliveredFluid, "Can't deliver a null FluidStack");
        Validate.isTrue(deliveredFluid.getAmount() > 0, "Can't deliver a FluidStack with an amount of <= 0");

        EntityAmadrone drone = EntityAmadrone.makeAmadrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetLiquidExport liquidExport = new ProgWidgetLiquidExport();
        liquidExport.setSides(ISidedWidget.ALL_SIDES);
        builder.add(liquidExport, ProgWidgetArea.fromPosition(pos));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.blockPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        drone.getFluidTank().fill(deliveredFluid, IFluidHandler.FluidAction.EXECUTE);
        world.addFreshEntity(drone);
        return drone;
    }

    public static CreatureEntity retrieveItemsAmazonStyle(GlobalPos gPos, ItemStack... queriedStacks) {
        World world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.pos();

        if (world == null || world.isClientSide) return null;
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
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.blockPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        world.addFreshEntity(drone);
        return drone;
    }

    public static CreatureEntity retrieveFluidAmazonStyle(GlobalPos gPos, FluidStack queriedFluid) {
        World world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.pos();

        if (world == null || world.isClientSide) return null;
        Validate.notNull(queriedFluid, "Can't retrieve a null FluidStack");
        Validate.isTrue(queriedFluid.getAmount() > 0, "Can't retrieve a FluidStack with an amount of <= 0");

        EntityAmadrone drone = EntityAmadrone.makeAmadrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetLiquidImport liquidImport = new ProgWidgetLiquidImport();
        liquidImport.setUseCount(true);
        liquidImport.setCount(queriedFluid.getAmount());
        liquidImport.setSides(ISidedWidget.ALL_SIDES);
        builder.add(liquidImport, ProgWidgetArea.fromPosition(pos), ProgWidgetLiquidFilter.withFilter(queriedFluid.getFluid()));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.blockPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        world.addFreshEntity(drone);
        return drone;
    }
}
