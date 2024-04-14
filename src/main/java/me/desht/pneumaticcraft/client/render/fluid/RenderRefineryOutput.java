package me.desht.pneumaticcraft.client.render.fluid;

import me.desht.pneumaticcraft.common.block.entity.processing.RefineryOutputBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.List;

public class RenderRefineryOutput extends AbstractFluidTER<RefineryOutputBlockEntity> {
    private static final AABB TANK_BOUNDS = new AABB(4.1 / 16f, 1 / 16f, 0.1 / 16f, 11.9 / 16f, 13.9 / 16f, 3 / 16f);
//    private static final AxisAlignedBB BOUNDS_EW = new AxisAlignedBB(13.9 / 16f, 1 / 16f, 4.1 / 16f, 15.9 / 16f, 13.9 / 16f, 11.9 / 16f);

    public RenderRefineryOutput(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    private static final AABB[] BOUNDS = new AABB[4];
    static {
        BOUNDS[0] = TANK_BOUNDS;
        BOUNDS[1] = AbstractFluidTER.rotateY(BOUNDS[0], 90);
        BOUNDS[2] = AbstractFluidTER.rotateY(BOUNDS[1], 90);
        BOUNDS[3] = AbstractFluidTER.rotateY(BOUNDS[2], 90);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(RefineryOutputBlockEntity te) {
        return List.of(new TankRenderInfo(te.getOutputTank(), BOUNDS[te.getRotation().get2DDataValue()]).without(Direction.DOWN));
    }
}
