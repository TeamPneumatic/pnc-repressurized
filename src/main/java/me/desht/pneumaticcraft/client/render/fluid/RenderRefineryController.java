package me.desht.pneumaticcraft.client.render.fluid;

import me.desht.pneumaticcraft.common.block.entity.RefineryControllerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.List;

public class RenderRefineryController extends AbstractFluidTER<RefineryControllerBlockEntity> {
    private static final AABB[] BOUNDS = new AABB[4];
    static {
        BOUNDS[0] = new AABB(2.1 / 16f, 1 / 16f, 3.1 / 16f, 13.9 / 16f, 15 / 16f, 14.9 / 16f);
        BOUNDS[1] = AbstractFluidTER.rotateY(BOUNDS[0], 90);
        BOUNDS[2] = AbstractFluidTER.rotateY(BOUNDS[0], 180);
        BOUNDS[3] = AbstractFluidTER.rotateY(BOUNDS[0], 270);
    }

    public RenderRefineryController(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(RefineryControllerBlockEntity te) {
        int rot = te.getRotation().get2DDataValue();
        return rot >= 0 && rot < 4 ?
                List.of(new TankRenderInfo(te.getInputTank(), BOUNDS[te.getRotation().get2DDataValue()]).without(Direction.DOWN)) :
                List.of();
    }
}
