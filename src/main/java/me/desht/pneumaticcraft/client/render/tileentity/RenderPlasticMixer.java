package me.desht.pneumaticcraft.client.render.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class RenderPlasticMixer extends FastFluidTESR<TileEntityPlasticMixer> {
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(2.5/16f, 1/16f, 2.5/16f, 13.5/16f, 15/16f, 13.5/16f);

    @Override
    List<TankRenderInfo> getTanksToRender(TileEntityPlasticMixer te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), TANK_BOUNDS).without(EnumFacing.DOWN));
    }
}
