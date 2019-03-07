package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.pressure.AirHandler;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.*;

public class ModuleNetworkManager {
    private static final Map<Integer, ModuleNetworkManager> INSTANCES = new HashMap<>();

    private final Map<TubeModule, Set<TubeModule>> connectionCache = new HashMap<>();

    public static ModuleNetworkManager getInstance(World w) {
        return INSTANCES.computeIfAbsent(w.provider.getDimension(), dimId -> new ModuleNetworkManager());
    }

    Set<TubeModule> getConnectedModules(TubeModule module) {
        return connectionCache.computeIfAbsent(module, this::computeConnections);
    }

    public void invalidateCache() {
        connectionCache.clear();
    }

    private Set<TubeModule> computeConnections(TubeModule module) {
        Set<TubeModule> modules = new HashSet<>();
        Set<TileEntityPressureTube> traversedTubes = new HashSet<>();
        Stack<TileEntityPressureTube> pendingTubes = new Stack<>();
        pendingTubes.push((TileEntityPressureTube) module.getTube());
        while (!pendingTubes.isEmpty()) {
            TileEntityPressureTube tube = pendingTubes.pop();
            for (TubeModule m : tube.modules) {
                if (m != null) modules.add(m);
            }
            TileEntityCache[] cache = ((AirHandler) tube.getAirHandler(null)).getTileCache();
            for (EnumFacing d : EnumFacing.VALUES) {
                if (tube.sidesConnected[d.ordinal()] && !tube.sidesClosed[d.ordinal()]) {
                    TileEntityPressureTube newTube = ModInteractionUtils.getInstance().getTube(cache[d.ordinal()].getTileEntity());
                    if (newTube != null && !traversedTubes.contains(newTube)) {
                        pendingTubes.add(newTube);
                        traversedTubes.add(newTube);
                    }
                }
            }
        }
        return modules;
    }
}
