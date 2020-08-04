package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidMixer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;

public class RenderFluidMixer extends AbstractFluidTER<TileEntityFluidMixer> {
    private static final AxisAlignedBB[] TANK_BOUNDS_BASE = new AxisAlignedBB[]{
            new AxisAlignedBB(1 / 16f,  1 / 16f, 10 / 16f, 5 / 16f,  7 / 16f,  14 / 16f),  // in1
            new AxisAlignedBB(11 / 16f, 1 / 16f, 10 / 16f, 15 / 16f, 7 / 16f,  14 / 16f),  // in2
            new AxisAlignedBB(6 / 16f,  9 / 16f, 3 / 16f,  10 / 16f, 15 / 16f, 7 / 16f)    // out
    };
    private static final AxisAlignedBB[][] BOUNDS = new AxisAlignedBB[3][4];
    static {
        for (int i = 0; i < TANK_BOUNDS_BASE.length; i++) {
            BOUNDS[i][0] = TANK_BOUNDS_BASE[i];
            BOUNDS[i][1] = AbstractFluidTER.rotateY(BOUNDS[i][0], 90);
            BOUNDS[i][2] = AbstractFluidTER.rotateY(BOUNDS[i][1], 90);
            BOUNDS[i][3] = AbstractFluidTER.rotateY(BOUNDS[i][2], 90);
        }
    }

    public RenderFluidMixer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityFluidMixer te) {
        return ImmutableList.of(
                new TankRenderInfo(te.getInputTank1(), BOUNDS[0][te.getRotation().getHorizontalIndex()]).without(Direction.DOWN),
                new TankRenderInfo(te.getInputTank2(), BOUNDS[1][te.getRotation().getHorizontalIndex()]).without(Direction.DOWN),
                new TankRenderInfo(te.getOutputTank(), BOUNDS[2][te.getRotation().getHorizontalIndex()]).without(Direction.DOWN)
        );
    }
}
