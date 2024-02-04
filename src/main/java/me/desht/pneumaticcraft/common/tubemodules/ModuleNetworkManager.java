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
        if (module.getTube().nonNullLevel().isClientSide()) throw new IllegalCallerException("ModuleNetworkManager.getConnectedModules() was called on client side");

        if (needInvalidate) {
            connectionCache.clear();
            needInvalidate = false;
        }
        return connectionCache.computeIfAbsent(module, this::computeConnections);
    }

    public void invalidateCache() {
        needInvalidate = true;
    }

    private Set<PressureTubeBlockEntity> computeConnectedTubes(Level level, BlockPos initialPos) {
        Set<PressureTubeBlockEntity> tubes = new HashSet<>();
        Set<BlockPos> traversedPositions = new HashSet<>();
        Deque<BlockPos> pendingPositions = new ArrayDeque<>(List.of(initialPos));

        while (!pendingPositions.isEmpty()) {
            BlockPos pos = pendingPositions.pop();
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ITubeNetworkConnector nc) {
                PneumaticCraftUtils.getTileEntityAt(level, pos, PressureTubeBlockEntity.class).ifPresent(tubes::add);
                for (Direction dir : DirectionUtil.VALUES) {
                    BlockPos pos1 = pos.relative(dir);
                    if (!level.isLoaded(pos1)) continue;
                    BlockState state1 = level.getBlockState(pos1);
                    if (state1.getBlock() instanceof ITubeNetworkConnector nc1
                            && nc1.canConnectToNetwork(level, pos1, dir.getOpposite(), state1)
                            && nc.canConnectToNetwork(level, pos, dir, state)
                            && traversedPositions.add(pos1)){
                        pendingPositions.add(pos1);
                    }
                }
            }
        }
        return tubes;
    }

    /**
     * @return all {@link INetworkedModule}s that are connected to {@code module}, and satisfies {@link AbstractTubeModule#canConnectTo(AbstractTubeModule)}.
     * This includes the {@code module} itself.
     */
    public Set<AbstractTubeModule> computeConnections(AbstractTubeModule module) {
        var tubes = computeConnectedTubes(module.getTube().nonNullLevel(), module.getTube().getBlockPos());
        Set<AbstractTubeModule> modules = new HashSet<>();
        tubes.forEach(tube -> tube.tubeModules()
                .filter(tm -> tm instanceof INetworkedModule && tm.canConnectTo(module))
                .forEach(modules::add));
        return modules;
    }

    /**
     * @return all {@link INetworkedModule}s that are connected to {@code pos}
     */
    public Set<AbstractTubeModule> computeConnections(Level level, BlockPos pos) {
        var tubes = computeConnectedTubes(level, pos);
        Set<AbstractTubeModule> modules = new HashSet<>();
        tubes.forEach(tube -> tube.tubeModules()
                .filter(tm -> tm instanceof INetworkedModule)
                .forEach(modules::add));
        return modules;
    }
}
