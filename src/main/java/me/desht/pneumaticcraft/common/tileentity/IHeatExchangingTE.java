package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

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
     * @return true if heat will be lost to the air on exposed faces, false otherwise
     */
    default BiPredicate<IWorld, BlockPos> heatExchangerBlockFilter() {
        return IHeatExchangerLogic.ALL_BLOCKS;
    }

    /**
     * Initialize this TE's hull heat exchanger to the ambient temperature, when the block is first placed down.
     * Override this in TE's which have multiple hull heat exchangers (e.g. vortex tube or thermal compressor)
     *
     * @param world the TE's world
     * @param pos the TE's block pos
     */
    default void initHeatExchangersOnPlacement(World world, BlockPos pos) {
        IHeatExchangerLogic logic = getHeatExchanger();
        if (logic != null) logic.setTemperature(HeatExchangerLogicAmbient.getAmbientTemperature(world, pos));
    }

    /**
     * Discover heat exchangers by side for this block, and initialize each distinct heat exchanger with the side
     * that it is connected to.  Call this on first TE server tick, and when a neighboring block update occurs.
     * Don't override this method.
     *
     * @param world the TE's world
     * @param pos the TE's block pos
     */
    default void initializeHullHeatExchangers(World world, BlockPos pos) {
        Map<IHeatExchangerLogic, List<Direction>> map = new IdentityHashMap<>();
        for (Direction side : DirectionUtil.VALUES) {
            IHeatExchangerLogic logic = getHeatExchanger(side);
            if (logic != null) map.computeIfAbsent(logic, k -> new ArrayList<>()).add(side);
        }
        map.forEach((logic, sides) ->
                logic.initializeAsHull(world, pos, heatExchangerBlockFilter(), sides.toArray(new Direction[0])));
    }
}
