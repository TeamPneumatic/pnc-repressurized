package me.desht.pneumaticcraft.client.render.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class RenderKeroseneLamp extends FastFluidTESR<TileEntityKeroseneLamp> {
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(6/16f, 1/16f, 6/16f, 10/16f, 9/16f, 10/16f);

    @Override
    List<TankRenderInfo> getTanksToRender(TileEntityKeroseneLamp te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), TANK_BOUNDS).without(EnumFacing.DOWN));
    }
}
