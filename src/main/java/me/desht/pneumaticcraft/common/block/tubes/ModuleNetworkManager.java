package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
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
        Set<TileEntity> traversedTubes = new HashSet<>();
        Stack<TileEntityPressureTube> pendingTubes = new Stack<>();
        pendingTubes.push(module.getTube());
        while (!pendingTubes.isEmpty()) {
            TileEntityPressureTube tube = pendingTubes.pop();
            for (TubeModule m : tube.modules) {
                if (m instanceof INetworkedModule && module.getClass() == m.getClass()) {
                    modules.add(m);
                }
            }
            for (Direction dir : Direction.VALUES) {
                TileEntity newTube = tube.getConnectedNeighbor(dir);
                if (newTube instanceof TileEntityPressureTube && !traversedTubes.contains(newTube)) {
                    pendingTubes.add((TileEntityPressureTube) newTube);
                    traversedTubes.add(newTube);
                }
            }
        }
        return modules;
    }
}
