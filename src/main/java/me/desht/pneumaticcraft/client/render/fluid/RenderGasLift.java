package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.block.entity.GasLiftBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Collection;

public class RenderGasLift extends AbstractFluidTER<GasLiftBlockEntity> {
    private static final AABB TANK_BOUNDS = new AABB(0.1/16f, 4.1/16f, 0.1/16f, 15.9/16f, 12.9/16f, 15.9/16f);

    public RenderGasLift(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(GasLiftBlockEntity te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), TANK_BOUNDS).without(Direction.DOWN));
    }
}
