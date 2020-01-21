package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityHeatFrame;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicTicking;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HeatBehaviourHeatFrame extends HeatBehaviour<TileEntity> {
    private static final ResourceLocation ID = RL("heat_frame");

    private EntityHeatFrame semiBlock;

    @Override
    public void initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
        super.initialize(connectedHeatLogic, world, pos, direction);
        semiBlock = null;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    private EntityHeatFrame getHeatFrame() {
        if (semiBlock == null || !semiBlock.isAlive()) {
            ISemiBlock s = SemiblockTracker.getInstance().getSemiblock(getWorld(), getPos());
            if (s instanceof EntityHeatFrame) {
                semiBlock = (EntityHeatFrame) s;
            }
        }
        return semiBlock;
    }

    @Override
    public boolean isApplicable() {
        return getHeatFrame() != null;
    }

    @Override
    public void tick() {
        HeatExchangerLogicTicking.exchange(getHeatFrame().getHeatExchangerLogic(), getHeatExchanger());
    }
}
