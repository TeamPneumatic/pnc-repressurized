/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.entity.drone.AmadroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

public class ProgrammedDroneUtils {
    public static PathfinderMob deliverItemsAmazonStyle(GlobalPos gPos, ItemStack... deliveredStacks) {
        Level world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        if (world == null || world.isClientSide) return null;

        BlockPos deliveryPos = gPos.pos();

        Validate.isTrue(deliveredStacks.length > 0 && deliveredStacks.length <= 36,
                "You can only deliver between 0 & 36 stacks at once!");
        Arrays.stream(deliveredStacks).forEach(stack -> Validate.isTrue(!stack.isEmpty(),
                "You can't supply an empty stack to be delivered!"));

        AmadroneEntity drone = AmadroneEntity.makeAmadrone(world, deliveryPos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetInventoryExport inventoryExport = new ProgWidgetInventoryExport();
        inventoryExport.setSides(ISidedWidget.ALL_SIDES);
        builder.add(inventoryExport, ProgWidgetArea.fromPosition(deliveryPos));
        builder.add(new ProgWidgetDropItem(), makeDropArea(deliveryPos, drone));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.blockPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets = builder.build();

        for (int i = 0; i < deliveredStacks.length; i++) {
            drone.getInv().setStackInSlot(i, deliveredStacks[i].copy());
        }
        world.addFreshEntity(drone);
        return drone;
    }

    private static ProgWidgetArea makeDropArea(BlockPos deliveryPos, AmadroneEntity drone) {
        // this is just a suitable place to drop items at if for any reason they can't be delivered
        // (inventory full, missing, etc.)
        if (drone.isBlockValidPathfindBlock(deliveryPos)) {
            // probably means the inventory is no longer there - just drop there
            return ProgWidgetArea.fromPosition(deliveryPos);
        }
        // otherwise drop adjacent to the delivery pos if possible
        ProgWidgetArea dropOffArea = null;
        for (Direction d: DirectionUtil.VALUES) {
            if (drone.isBlockValidPathfindBlock(deliveryPos.relative(d))) {
                dropOffArea = ProgWidgetArea.fromPosition(deliveryPos.relative(d));
            }
        }
        if (dropOffArea == null) {
            // still no? scan up 10 blocks from the delivery pos
            dropOffArea = ProgWidgetArea.fromPosition(deliveryPos);
            for (int i = 2; i < 10 && !drone.isBlockValidPathfindBlock(deliveryPos.relative(Direction.UP, i)); i++) {
                dropOffArea.setPos(0, dropOffArea.getPos(0).orElseThrow().above());
            }
            BlockPos pos1 = dropOffArea.getPos(0).orElseThrow();
            if (!drone.isBlockValidPathfindBlock(pos1)) {
                // Worst case scenario; there are definitely no blocks here.
                dropOffArea.setPos(0, new BlockPos(pos1.getZ(), drone.getDroneLevel().getMaxBuildHeight() + 5, pos1.getZ()));
            }
        }
        return dropOffArea;
    }

    public static PathfinderMob deliverFluidAmazonStyle(GlobalPos gPos, FluidStack deliveredFluid) {
        Level world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.pos();

        if (world == null || world.isClientSide) return null;
        Validate.notNull(deliveredFluid, "Can't deliver a null FluidStack");
        Validate.isTrue(deliveredFluid.getAmount() > 0, "Can't deliver a FluidStack with an amount of <= 0");

        AmadroneEntity drone = AmadroneEntity.makeAmadrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetLiquidExport liquidExport = new ProgWidgetLiquidExport();
        liquidExport.setSides(ISidedWidget.ALL_SIDES);
        builder.add(liquidExport, ProgWidgetArea.fromPosition(pos));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.blockPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets = builder.build();

        drone.getFluidTank().fill(deliveredFluid, IFluidHandler.FluidAction.EXECUTE);
        world.addFreshEntity(drone);
        return drone;
    }

    public static PathfinderMob retrieveItemsAmazonStyle(GlobalPos gPos, ItemStack... queriedStacks) {
        Level world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.pos();

        if (world == null || world.isClientSide) return null;
        Validate.isTrue(queriedStacks.length > 0 && queriedStacks.length <= 36, "Must retrieve between 1 & 36 itemstacks!");
        Arrays.stream(queriedStacks).forEach(stack -> Validate.isTrue(!stack.isEmpty(), "Cannot retrieve an empty stack!"));

        AmadroneEntity drone = AmadroneEntity.makeAmadrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        for (ItemStack stack : queriedStacks) {
            ProgWidgetInventoryImport widgetImport = new ProgWidgetInventoryImport();
            widgetImport.setUseCount(true);
            widgetImport.setCount(stack.getCount());
            ProgWidgetItemFilter filter = ProgWidgetItemFilter.withFilter(stack);
            filter.setMatchComponents(!stack.getComponentsPatch().isEmpty());
            builder.add(widgetImport, ProgWidgetArea.fromPosition(pos), filter);
        }
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.blockPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets = builder.build();

        world.addFreshEntity(drone);
        return drone;
    }

    public static PathfinderMob retrieveFluidAmazonStyle(GlobalPos gPos, FluidStack queriedFluid) {
        Level world = GlobalPosHelper.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.pos();

        if (world == null || world.isClientSide) return null;
        Validate.notNull(queriedFluid, "Can't retrieve a null FluidStack");
        Validate.isTrue(queriedFluid.getAmount() > 0, "Can't retrieve a FluidStack with an amount of <= 0");

        AmadroneEntity drone = AmadroneEntity.makeAmadrone(world, pos);

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
        drone.progWidgets = builder.build();

        world.addFreshEntity(drone);
        return drone;
    }
}
