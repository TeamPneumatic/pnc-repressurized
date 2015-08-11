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
        Set<IPneumaticPosProvider> traversedTubes = new HashSet<IPneumaticPosProvider>();
        Stack<IPneumaticPosProvider> pendingTubes = new Stack<IPneumaticPosProvider>();
        pendingTubes.push(module.getTube());
        while(!pendingTubes.isEmpty()) {
            IPneumaticPosProvider tube = pendingTubes.pop();
            for(TubeModule m : getTubeModules(tube)) {
                if(m != null) modules.add(m);
            }
            boolean[] sidesConnected = ModInteractionUtils.getInstance().getTubeConnections(tube);
            TileEntityCache[] cache = ((TileEntityPneumaticBase)((IPneumaticMachine)tube).getAirHandler()).getTileCache();
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                if(sidesConnected[d.ordinal()]) {
                    IPneumaticMachine machine = ModInteractionUtils.getInstance().getMachine(cache[d.ordinal()].getTileEntity());
                    if(ModInteractionUtils.getInstance().isPneumaticTube(machine) && !traversedTubes.contains(machine)) {
                        pendingTubes.add((IPneumaticPosProvider)machine);
                        traversedTubes.add(tube);
                    }
                }
            }
        }
        return modules;
    }

    private TubeModule[] getTubeModules(IPneumaticPosProvider tube){
        return tube instanceof TileEntityPressureTube ? ((TileEntityPressureTube)tube).modules : new TubeModule[6];
    }
}
