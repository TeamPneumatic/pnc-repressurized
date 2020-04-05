package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.List;

public class RenderLiquidHopper extends AbstractFluidTESR<TileEntityLiquidHopper> {
    private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[6];
    static {
        BOUNDS[Direction.DOWN.getIndex()] = new AxisAlignedBB(1 / 16f, 1.01 / 16f, 1 / 16f, 15 / 16f, 5 / 16f, 15 / 16f);
        BOUNDS[Direction.UP.getIndex()] = new AxisAlignedBB(1 / 16f, 11.01 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
        BOUNDS[Direction.NORTH.getIndex()] = new AxisAlignedBB(1 / 16f, 1.01 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 5 / 16f);
        BOUNDS[Direction.SOUTH.getIndex()] = new AxisAlignedBB(1 / 16f, 1.01 / 16f, 11 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
        BOUNDS[Direction.WEST.getIndex()] = new AxisAlignedBB(1 / 16f, 1.01 / 16f, 1 / 16f, 5 / 16f, 15 / 16f, 15 / 16f);
        BOUNDS[Direction.EAST.getIndex()] = new AxisAlignedBB(11 / 16f, 1.01 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
    }

    public RenderLiquidHopper(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    List<TankRenderInfo> getTanksToRender(TileEntityLiquidHopper te) {
        return ImmutableList.of(new TankRenderInfo(te.getTank(), BOUNDS[te.getInputDirection().getIndex()]).without(te.getInputDirection().getOpposite()));
    }

    public static class ItemInfoProvider extends FluidItemRenderInfoProvider {
        @Override
        public List<TankRenderInfo> getTanksToRender(ItemStack stack) {
            return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                    .map(h -> ImmutableList.of(new TankRenderInfo(h.getFluidInTank(0), h.getTankCapacity(0), BOUNDS[Direction.UP.getIndex()]).without(Direction.DOWN)))
                    .orElse(null);
        }
    }

}
