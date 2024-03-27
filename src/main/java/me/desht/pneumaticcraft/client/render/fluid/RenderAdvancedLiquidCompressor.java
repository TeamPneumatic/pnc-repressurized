package me.desht.pneumaticcraft.client.render.fluid;

import me.desht.pneumaticcraft.common.block.entity.AdvancedLiquidCompressorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.List;

public class RenderAdvancedLiquidCompressor extends AbstractFluidTER<AdvancedLiquidCompressorBlockEntity> {
    private static final AABB TANK_BOUNDS = new AABB(0.1/16f, 11.1/16f, 0.1/16f, 15.9/16f, 15.9/16f, 15.9/16f);

    public RenderAdvancedLiquidCompressor(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(AdvancedLiquidCompressorBlockEntity te) {
        return List.of(new TankRenderInfo(te.getTank(), TANK_BOUNDS).without(Direction.DOWN));
    }
}
