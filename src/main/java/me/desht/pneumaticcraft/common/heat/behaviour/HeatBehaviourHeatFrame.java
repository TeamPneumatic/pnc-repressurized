package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogic;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockHeatFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HeatBehaviourHeatFrame extends HeatBehaviour<TileEntity> {
    private SemiBlockHeatFrame semiBlock;

    @Override
    public void initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos) {
        super.initialize(connectedHeatLogic, world, pos);
        semiBlock = null;
    }

    @Override
    public String getId() {
        return Names.MOD_ID + ":heatFrame";
    }

    private SemiBlockHeatFrame getSemiBlock() {
        if (semiBlock == null) {
            semiBlock = SemiBlockManager.getInstance(getWorld()).getSemiBlock(SemiBlockHeatFrame.class, getWorld(), getPos());
        }
        return semiBlock;
    }

    @Override
    public boolean isApplicable() {
        return getSemiBlock() != null;
    }

    @Override
    public void update() {
        HeatExchangerLogic.exchange(getSemiBlock().getHeatExchangerLogic(null), getHeatExchanger());
    }

}
