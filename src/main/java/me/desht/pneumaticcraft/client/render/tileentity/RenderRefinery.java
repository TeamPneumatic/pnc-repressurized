package me.desht.pneumaticcraft.client.render.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collections;
import java.util.List;

public class RenderRefinery extends FastFluidTESR<TileEntityRefinery> {

    private static final AxisAlignedBB TANK_BOUNDS_1 = new AxisAlignedBB(4.25f/16f, 1f/16f, 0.25f/16f, 11.75f/16f, 15/16f, 3f/16f);
    private static final AxisAlignedBB TANK_BOUNDS_2 = new AxisAlignedBB(4.25f/16f, 1f/16f, 13.0f/16f, 11.75f/16f, 15/16f, 15.75f/16f);

    static final AxisAlignedBB[] BOUNDS_IN = new AxisAlignedBB[4];
    static {
        BOUNDS_IN[0] = TANK_BOUNDS_1;
        BOUNDS_IN[1] = FastFluidTESR.rotateY(BOUNDS_IN[0], 90);
        BOUNDS_IN[2] = FastFluidTESR.rotateY(BOUNDS_IN[1], 90);
        BOUNDS_IN[3] = FastFluidTESR.rotateY(BOUNDS_IN[2], 90);
    }
    static final AxisAlignedBB[] BOUNDS_OUT = new AxisAlignedBB[4];
    static {
        BOUNDS_OUT[0] = TANK_BOUNDS_2;
        BOUNDS_OUT[1] = FastFluidTESR.rotateY(BOUNDS_OUT[0], 90);
        BOUNDS_OUT[2] = FastFluidTESR.rotateY(BOUNDS_OUT[1], 90);
        BOUNDS_OUT[3] = FastFluidTESR.rotateY(BOUNDS_OUT[2], 90);
    }

    @Override
    List<TankRenderInfo> getTanksToRender(TileEntityRefinery te) {
        int rot = te.getRotation().getHorizontalIndex();
        if (rot >= 0 && rot < 4) {
            return ImmutableList.of(
                    new TankRenderInfo(te.getInputTank(), BOUNDS_IN[te.getRotation().getHorizontalIndex()]).without(Direction.DOWN),
                    new TankRenderInfo(te.getOutputTank(), BOUNDS_OUT[te.getRotation().getHorizontalIndex()]).without(Direction.DOWN)
            );
        } else {
            return Collections.emptyList();
        }
    }
}
