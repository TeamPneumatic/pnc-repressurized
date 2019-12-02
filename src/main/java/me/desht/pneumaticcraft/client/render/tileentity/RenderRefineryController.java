package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collections;
import java.util.List;

public class RenderRefineryController extends FastFluidTESR<TileEntityRefineryController> {
    private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[4];
    static {
        BOUNDS[0] = new AxisAlignedBB(2 / 16f, 1 / 16f, 1.5 / 16f, 14 / 16f, 15 / 16f, 8 / 16f);
        BOUNDS[1] = FastFluidTESR.rotateY(BOUNDS[0], 90);
        BOUNDS[2] = FastFluidTESR.rotateY(BOUNDS[0], 180);
        BOUNDS[3] = FastFluidTESR.rotateY(BOUNDS[0], 270);
    }

    @Override
    List<TankRenderInfo> getTanksToRender(TileEntityRefineryController te) {
        int rot = te.getRotation().getHorizontalIndex();
        if (rot >= 0 && rot < 4) {
            return Collections.singletonList(new TankRenderInfo(te.getInputTank(), BOUNDS[te.getRotation().getHorizontalIndex()], te.getRotation().getOpposite(), Direction.UP));
        } else {
            return Collections.emptyList();
        }
    }
}
