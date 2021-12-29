package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedLiquidCompressor;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;

public class RenderAdvancedLiquidCompressor extends AbstractFluidTER<TileEntityAdvancedLiquidCompressor> {
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(0.1/16f, 11.1/16f, 0.1/16f, 15.9/16f, 15.9/16f, 15.9/16f);

    public RenderAdvancedLiquidCompressor(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityAdvancedLiquidCompressor te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), TANK_BOUNDS).without(Direction.DOWN));
    }
}
