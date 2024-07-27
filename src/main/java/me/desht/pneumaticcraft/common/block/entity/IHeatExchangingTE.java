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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Non-API interface for all heat-handling tile entities in the mod
 */
@FunctionalInterface
public interface IHeatExchangingTE {
    /**
     * Get the heat exchanger on the given face. This may return null iff the direction is not null.
     * @param dir the side of the block to check, or null for the default/primary heat exchanger
     * @return a heat exchanger
     */
    @Nullable
    IHeatExchangerLogic getHeatExchanger(Direction dir);

    /**
     * Get the heat exchanger on the null or default face.
     * @return the default heat exchanger, can be null
     */
    default IHeatExchangerLogic getHeatExchanger() {
        return getHeatExchanger(null);
    }

    /**
     * Should this (heat-using) machine lose heat to the surrounding air blocks? Most blocks do.
     * @return a bi-predicate mapping a level and blockpos to a boolean; true if heat would be lost here, false otherwise
     */
    default BiPredicate<LevelAccessor, BlockPos> heatExchangerBlockFilter() {
        return IHeatExchangerLogic.ALL_BLOCKS;
    }

    /**
     * Initialize this BE's hull heat exchanger to the ambient temperature, when the block is first placed down.
     * Override this in BE's which have multiple hull heat exchangers (e.g. vortex tube or thermal compressor)
     *
     * @param world the BE's world
     * @param pos the BE's block pos
     */
    default void initHeatExchangersOnPlacement(Level world, BlockPos pos) {
        IHeatExchangerLogic logic = getHeatExchanger();
        if (logic != null) logic.setTemperature(HeatExchangerLogicAmbient.getAmbientTemperature(world, pos));
    }

    /**
     * Discover heat exchangers by side for this block, and initialize each distinct heat exchanger with the side
     * that it is connected to.  Call this on first BE server tick, and when a neighboring block update occurs.
     * Don't override this method.
     *
     * @param world the BE's world
     * @param pos the BE's block pos
     */
    default void initializeHullHeatExchangers(Level world, BlockPos pos) {
        Map<IHeatExchangerLogic, List<Direction>> map = new IdentityHashMap<>();
        for (Direction side : DirectionUtil.VALUES) {
            IHeatExchangerLogic logic = getHeatExchanger(side);
            if (logic != null) map.computeIfAbsent(logic, k -> new ArrayList<>()).add(side);
        }
        map.forEach((logic, sides) ->
                logic.initializeAsHull(world, pos, heatExchangerBlockFilter(), sides.toArray(new Direction[0])));
    }

    default boolean shouldShowGuiHeatTab() {
        return true;
    }
}
