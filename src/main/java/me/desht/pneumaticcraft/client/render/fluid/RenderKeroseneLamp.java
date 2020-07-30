package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;

public class RenderKeroseneLamp extends AbstractFluidTER<TileEntityKeroseneLamp> {
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(6/16f, 1/16f, 6/16f, 10/16f, 9/16f, 10/16f);

    public RenderKeroseneLamp(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(TileEntityKeroseneLamp te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), TANK_BOUNDS).without(Direction.DOWN));
    }
}
