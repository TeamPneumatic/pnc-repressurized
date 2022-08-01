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

package me.desht.pneumaticcraft.common.drone;

import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Keeps track of the positions the drones are working on, and allows the drones to pick a coordinate in a smart way.
 */
public class DroneClaimManager {

    private static final Map<ResourceLocation, DroneClaimManager> claimManagers = new HashMap<>();
    private final Map<BlockPos, Integer> currentPositions = new HashMap<>();
    private static final int TIMEOUT = DroneAIManager.TICK_RATE + 1;

    public static DroneClaimManager getInstance(Level world) {
        return claimManagers.computeIfAbsent(world.dimension().location(), k -> new DroneClaimManager());
    }

    /**
     * unclaim any positions that have been claimed too long. this prevents positions being claimed forever by died drones.
     */
    public void tick() {
        Iterator<Map.Entry<BlockPos, Integer>> iterator = currentPositions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            if (entry.getValue() < TIMEOUT) {
                entry.setValue(entry.getValue() + 1);
            } else {
                iterator.remove();
            }
        }
    }

    public boolean isClaimed(BlockPos pos) {
        return currentPositions.containsKey(pos);
    }

    public void claim(BlockPos pos) {
        currentPositions.put(pos, 0);
    }
}
