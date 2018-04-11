package me.desht.pneumaticcraft.client.render.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class RenderLiquidHopper extends FastFluidTESR<TileEntityLiquidHopper> {
    private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[6];
    static {
        BOUNDS[EnumFacing.DOWN.getIndex()] = new AxisAlignedBB(1 / 16f, 1.01 / 16f, 1 / 16f, 15 / 16f, 5 / 16f, 15 / 16f);
        BOUNDS[EnumFacing.UP.getIndex()] = new AxisAlignedBB(1 / 16f, 11.01 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
        BOUNDS[EnumFacing.NORTH.getIndex()] = new AxisAlignedBB(1 / 16f, 1.01 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 5 / 16f);
        BOUNDS[EnumFacing.SOUTH.getIndex()] = new AxisAlignedBB(1 / 16f, 1.01 / 16f, 11 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
        BOUNDS[EnumFacing.WEST.getIndex()] = new AxisAlignedBB(1 / 16f, 1.01 / 16f, 1 / 16f, 5 / 16f, 15 / 16f, 15 / 16f);
        BOUNDS[EnumFacing.EAST.getIndex()] = new AxisAlignedBB(11 / 16f, 1.01 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
    }

    @Override
    List<TankRenderInfo> getTanksToRender(TileEntityLiquidHopper te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), BOUNDS[te.getInputDirection().getIndex()]).without(te.getInputDirection().getOpposite()));
    }
}
