package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityEtchingTank;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class RenderEtchingTank extends FastFluidTESR<TileEntityEtchingTank> {
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(2.5/16f, 2/16f, 2.5/16f, 13.5/16f, 15/16f, 13.5/16f);

    @Override
    List<TankRenderInfo> getTanksToRender(TileEntityEtchingTank te) {
        return ImmutableList.of(new TankRenderInfo(te.getAcidTank(), TANK_BOUNDS).without(Direction.DOWN));
    }
}
