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

package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.api.block.ITubeNetworkConnector;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class ModuleNetworkManager {
    private static final Map<ResourceLocation, ModuleNetworkManager> INSTANCES = new HashMap<>();

    private final Map<AbstractTubeModule, Set<AbstractTubeModule>> connectionCache = new HashMap<>();
    private boolean needInvalidate = false;

    public static ModuleNetworkManager getInstance(Level w) {
        return INSTANCES.computeIfAbsent(w.dimension().location(), dimId -> new ModuleNetworkManager());
    }

    Set<AbstractTubeModule> getConnectedModules(AbstractTubeModule module) {
        if (needInvalidate) {
            connectionCache.clear();
            needInvalidate = false;
        }
        return connectionCache.computeIfAbsent(module, this::computeConnections);
    }

    public void invalidateCache() {
        needInvalidate = true;
    }

    private Set<AbstractTubeModule> computeConnections(AbstractTubeModule module) {
        Level level = module.getTube().nonNullLevel();
        Set<AbstractTubeModule> modules = new HashSet<>();
        Set<BlockPos> traversedTubes = new HashSet<>();
        Deque<BlockPos> pendingPositions = new ArrayDeque<>(List.of(module.getTube().getBlockPos()));

        while (!pendingPositions.isEmpty()) {
            BlockPos pos = pendingPositions.pop();
            PneumaticCraftUtils.getTileEntityAt(level, pos, PressureTubeBlockEntity.class).ifPresent(tube -> tube.tubeModules()
                    .filter(tm -> tm instanceof INetworkedModule)
                    .forEach(modules::add));
            for (Direction dir : DirectionUtil.VALUES) {
                BlockPos pos1 = pos.relative(dir);
                if (!level.isLoaded(pos1)) continue;
                BlockState state = level.getBlockState(pos1);
                if (state.getBlock() instanceof ITubeNetworkConnector nc
                        && nc.canConnectToNetwork(level, pos1, dir.getOpposite(), state)
                        && traversedTubes.add(pos1)) {
                    pendingPositions.add(pos1);
                }
            }
        }
        return modules;
    }
}
