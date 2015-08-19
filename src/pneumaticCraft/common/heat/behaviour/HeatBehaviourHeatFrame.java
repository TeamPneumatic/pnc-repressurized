package pneumaticCraft.common.heat.behaviour;

import net.minecraft.world.World;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.tileentity.HeatBehaviour;
import pneumaticCraft.common.heat.HeatExchangerLogic;
import pneumaticCraft.common.semiblock.ISemiBlock;
import pneumaticCraft.common.semiblock.SemiBlockHeatFrame;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import pneumaticCraft.lib.Names;

public class HeatBehaviourHeatFrame extends HeatBehaviour{
    private ISemiBlock semiBlock;

    @Override
    public void initialize(IHeatExchangerLogic connectedHeatLogic, World world, int x, int y, int z){
        super.initialize(connectedHeatLogic, world, x, y, z);
        semiBlock = null;
    }

    @Override
    public String getId(){
        return Names.MOD_ID + ":heatFrame";
    }

    private ISemiBlock getSemiBlock(){
        if(semiBlock == null) {
            semiBlock = SemiBlockManager.getInstance(getWorld()).getSemiBlock(getWorld(), getX(), getY(), getZ());
        }
        return semiBlock;
    }

    @Override
    public boolean isApplicable(){
        return getSemiBlock() instanceof SemiBlockHeatFrame;
    }

    @Override
    public void update(){
        SemiBlockHeatFrame frame = (SemiBlockHeatFrame)getSemiBlock();
        HeatExchangerLogic.exchange(frame.getHeatExchangerLogic(null), getHeatExchanger());
    }

}
