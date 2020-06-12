package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;

public class RenderThermopneumaticProcessingPlant extends AbstractFluidTESR<TileEntityThermopneumaticProcessingPlant> {
    private static final AxisAlignedBB TANK_BOUNDS_1 = new AxisAlignedBB(10 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, 11 / 16f, 6 / 16f);
    private static final AxisAlignedBB TANK_BOUNDS_2 = new AxisAlignedBB(2 / 16f, 1 / 16f, 1 / 16f, 7 / 16f, 11 / 16f, 6 / 16f);

    private static final AxisAlignedBB[] BOUNDS_IN = new AxisAlignedBB[4];
    static {
        BOUNDS_IN[0] = TANK_BOUNDS_1;
        BOUNDS_IN[1] = AbstractFluidTESR.rotateY(BOUNDS_IN[0], 90);
        BOUNDS_IN[2] = AbstractFluidTESR.rotateY(BOUNDS_IN[1], 90);
        BOUNDS_IN[3] = AbstractFluidTESR.rotateY(BOUNDS_IN[2], 90);
    }
    private static final AxisAlignedBB[] BOUNDS_OUT = new AxisAlignedBB[4];
    static {
        BOUNDS_OUT[0] = TANK_BOUNDS_2;
        BOUNDS_OUT[1] = AbstractFluidTESR.rotateY(BOUNDS_OUT[0], 90);
        BOUNDS_OUT[2] = AbstractFluidTESR.rotateY(BOUNDS_OUT[1], 90);
        BOUNDS_OUT[3] = AbstractFluidTESR.rotateY(BOUNDS_OUT[2], 90);
    }

    public RenderThermopneumaticProcessingPlant(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityThermopneumaticProcessingPlant te) {
        return ImmutableList.of(
                new TankRenderInfo(te.getInputTank(), BOUNDS_IN[te.getRotation().getHorizontalIndex()]).without(Direction.DOWN),
                new TankRenderInfo(te.getOutputTank(), BOUNDS_OUT[te.getRotation().getHorizontalIndex()]).without(Direction.DOWN)
        );
    }
}
