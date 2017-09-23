package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogic;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockHeatFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HeatBehaviourHeatFrame extends HeatBehaviour {
    private ISemiBlock semiBlock;

    @Override
    public void initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos) {
        super.initialize(connectedHeatLogic, world, pos);
        semiBlock = null;
    }

    @Override
    public String getId() {
        return Names.MOD_ID + ":heatFrame";
    }

    private ISemiBlock getSemiBlock() {
        if (semiBlock == null) {
            semiBlock = SemiBlockManager.getInstance(getWorld()).getSemiBlock(getWorld(), getPos());
        }
        return semiBlock;
    }

    @Override
    public boolean isApplicable() {
        return getSemiBlock() instanceof SemiBlockHeatFrame;
    }

    @Override
    public void update() {
        SemiBlockHeatFrame frame = (SemiBlockHeatFrame) getSemiBlock();
        HeatExchangerLogic.exchange(frame.getHeatExchangerLogic(null), getHeatExchanger());
    }

}
