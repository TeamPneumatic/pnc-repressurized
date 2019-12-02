package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.*;

public class ModuleNetworkManager {
    private static final Map<ResourceLocation, ModuleNetworkManager> INSTANCES = new HashMap<>();

    private final Map<TubeModule, Set<TubeModule>> connectionCache = new HashMap<>();
    private boolean needInvalidate = false;

    public static ModuleNetworkManager getInstance(World w) {
        return INSTANCES.computeIfAbsent(w.getDimension().getType().getRegistryName(), dimId -> new ModuleNetworkManager());
    }

    Set<TubeModule> getConnectedModules(TubeModule module) {
        if (needInvalidate) {
            connectionCache.clear();
            needInvalidate = false;
        }
        return connectionCache.computeIfAbsent(module, this::computeConnections);
    }

    public void invalidateCache() {
        needInvalidate = true;
    }

    private Set<TubeModule> computeConnections(TubeModule module) {
        Set<TubeModule> modules = new HashSet<>();
        Set<TileEntityPressureTube> traversedTubes = new HashSet<>();
        Stack<TileEntityPressureTube> pendingTubes = new Stack<>();
        pendingTubes.push((TileEntityPressureTube) module.getTube());
        while (!pendingTubes.isEmpty()) {
            TileEntityPressureTube tube = pendingTubes.pop();
            for (TubeModule m : tube.modules) {
                if (m instanceof INetworkedModule && module.getClass() == m.getClass()) {
                    modules.add(m);
                }
            }
            TileEntityCache[] cache = tube.getTileCache();//((AirHandler) tube.getAirHandler(null)).getTileCache();
            for (int dir = 0; dir < 6; dir++) {
                if (isTubeConnected(tube, dir)) {
                    TileEntityPressureTube newTube = TileEntityPressureTube.getTube(cache[dir].getTileEntity());
                    if (newTube != null && !traversedTubes.contains(newTube)) {
                        pendingTubes.add(newTube);
                        traversedTubes.add(newTube);
                    }
                }
            }
        }
        return modules;
    }

    private boolean isTubeConnected(TileEntityPressureTube tube, int dir) {
        return !tube.sidesClosed[dir] &&
                (tube.sidesConnected[dir] || (tube.modules[dir] != null && tube.modules[dir].isInline()));
    }
}
