package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicTicking;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockHeatFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HeatBehaviourHeatFrame extends HeatBehaviour<TileEntity> {
    private static final ResourceLocation ID = RL("heat_frame");

    private SemiBlockHeatFrame semiBlock;

    @Override
    public void initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
        super.initialize(connectedHeatLogic, world, pos, direction);
        semiBlock = null;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
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
    public void tick() {
        HeatExchangerLogicTicking.exchange(getSemiBlock().getHeatExchangerLogic(null), getHeatExchanger());
    }

}
