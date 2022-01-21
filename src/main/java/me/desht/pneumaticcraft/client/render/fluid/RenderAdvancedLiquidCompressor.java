package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedLiquidCompressor;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Collection;

public class RenderAdvancedLiquidCompressor extends AbstractFluidTER<TileEntityAdvancedLiquidCompressor> {
    private static final AABB TANK_BOUNDS = new AABB(0.1/16f, 11.1/16f, 0.1/16f, 15.9/16f, 15.9/16f, 15.9/16f);

    public RenderAdvancedLiquidCompressor(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityAdvancedLiquidCompressor te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), TANK_BOUNDS).without(Direction.DOWN));
    }
}
