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

import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

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
        Set<AbstractTubeModule> modules = new HashSet<>();
        Set<BlockEntity> traversedTubes = new HashSet<>();
        Stack<PressureTubeBlockEntity> pendingTubes = new Stack<>();
        pendingTubes.push(module.getTube());
        while (!pendingTubes.isEmpty()) {
            PressureTubeBlockEntity tube = pendingTubes.pop();
            tube.tubeModules()
                    .filter(tm -> tm instanceof INetworkedModule && module.getClass() == tm.getClass())
                    .forEach(modules::add);
            for (Direction dir : DirectionUtil.VALUES) {
                BlockEntity newTube = tube.getConnectedNeighbor(dir);
                if (newTube instanceof PressureTubeBlockEntity && !traversedTubes.contains(newTube)) {
                    pendingTubes.add((PressureTubeBlockEntity) newTube);
                    traversedTubes.add(newTube);
                }
            }
        }
        return modules;
    }
}
