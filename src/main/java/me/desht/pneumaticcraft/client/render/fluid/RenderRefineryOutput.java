package me.desht.pneumaticcraft.client.render.fluid;

import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryOutput;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collections;
import java.util.List;

public class RenderRefineryOutput extends AbstractFluidTESR<TileEntityRefineryOutput> {
    private static final AxisAlignedBB BOUNDS_NS = new AxisAlignedBB(5 / 16f, 1 / 16f, 1 / 16f, 11 / 16f, 15 / 16f, 15 / 16f);
    private static final AxisAlignedBB BOUNDS_EW = new AxisAlignedBB(1 / 16f, 1 / 16f, 5 / 16f, 15 / 16f, 15 / 16f, 11 / 16f);

    public RenderRefineryOutput(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    List<TankRenderInfo> getTanksToRender(TileEntityRefineryOutput te) {
        return Collections.singletonList(new TankRenderInfo(te.getOutputTank(), te.getRotation().getAxis() == Direction.Axis.Z ? BOUNDS_NS : BOUNDS_EW));
    }
}
