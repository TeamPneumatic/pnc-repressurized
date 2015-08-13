package pneumaticCraft.common.block.tubes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.common.util.TileEntityCache;

public class ModuleNetworkManager{
    private static ModuleNetworkManager INSTANCE = new ModuleNetworkManager();
    private final List<Set<TubeModule>> connectedModules = new ArrayList<Set<TubeModule>>();

    public static ModuleNetworkManager getInstance(){
        return INSTANCE;
    }

    public void addModule(TubeModule module){

    }

    public Set<TubeModule> getConnectedModules(TubeModule module){
        Set<TubeModule> modules = new HashSet<TubeModule>();
        Set<TileEntityPressureTube> traversedTubes = new HashSet<TileEntityPressureTube>();
        Stack<TileEntityPressureTube> pendingTubes = new Stack<TileEntityPressureTube>();
        pendingTubes.push((TileEntityPressureTube)module.getTube());
        while(!pendingTubes.isEmpty()) {
            TileEntityPressureTube tube = pendingTubes.pop();
            for(TubeModule m : tube.modules) {
                if(m != null) modules.add(m);
            }
            TileEntityCache[] cache = ((TileEntityPneumaticBase)((IPneumaticMachine)tube).getAirHandler()).getTileCache();
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                if(tube.sidesConnected[d.ordinal()]) {
                    TileEntityPressureTube newTube = ModInteractionUtils.getInstance().getTube(cache[d.ordinal()].getTileEntity());
                    if(newTube != null && !traversedTubes.contains(newTube)) {
                        pendingTubes.add(newTube);
                        traversedTubes.add(newTube);
                    }
                }
            }
        }
        return modules;
    }
}
