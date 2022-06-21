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

package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.drone.IPathfindHandler;
import me.desht.pneumaticcraft.common.util.ProgrammedDroneUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum DroneRegistry implements IDroneRegistry {
    INSTANCE;

    public final Map<Block, IPathfindHandler> pathfindableBlocks = new HashMap<>();

    public static DroneRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void addPathfindableBlock(Block block, IPathfindHandler handler) {
        Validate.notNull(block);
        pathfindableBlocks.put(block, handler);
    }

//    @Override
//    public void registerCustomBlockInteractor(RegisterEvent event, ICustomBlockInteract interactor) {
//        if (event.getRegistryKey().equals(ModProgWidgets.PROG_WIDGETS.get().getRegistryKey())) {
//            ProgWidgetType<?> type = ProgWidgetType.createType(() -> new ProgWidgetCustomBlockInteract().setInteractor(interactor));
//            event.register(ModProgWidgets.PROG_WIDGETS.get(), interactor.getID(), () -> type);
//        }
//        ProgWidgetType<?> type = ProgWidgetType.createType(() ->
//                new ProgWidgetCustomBlockInteract().setInteractor(interactor)).setRegistryName(interactor.getID());
//        event.getRegistry().register(type);
//    }

    @Override
    public PathfinderMob deliverItemsAmazonStyle(GlobalPos globalPos, ItemStack... deliveredStacks) {
        return ProgrammedDroneUtils.deliverItemsAmazonStyle(globalPos, deliveredStacks);
    }

    @Override
    public PathfinderMob retrieveItemsAmazonStyle(GlobalPos globalPos, ItemStack... queriedStacks) {
        return ProgrammedDroneUtils.retrieveItemsAmazonStyle(globalPos, queriedStacks);
    }

    @Override
    public PathfinderMob deliverFluidAmazonStyle(GlobalPos globalPos, FluidStack deliveredFluid) {
        return ProgrammedDroneUtils.deliverFluidAmazonStyle(globalPos, deliveredFluid);
    }

    @Override
    public PathfinderMob retrieveFluidAmazonStyle(GlobalPos globalPos, FluidStack queriedFluid) {
        return ProgrammedDroneUtils.retrieveFluidAmazonStyle(globalPos, queriedFluid);
    }

    @Override
    public Optional<IDrone> getDrone(Level level, int entityID) {
        Entity e = level.getEntity(entityID);
        return e instanceof IDrone d ? Optional.of(d) : Optional.empty();
    }

    @Override
    public Optional<IDrone> getDrone(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof IDrone d ? Optional.of(d) : Optional.empty();
    }

}
