package me.desht.pneumaticcraft.client.render.fluid;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidTank;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.Collections;
import java.util.List;

public class RenderFluidTank extends FastFluidTESR<TileEntityFluidTank> {
    private static final AxisAlignedBB BOUNDS_NONE = new AxisAlignedBB(2 / 16f, 1.01 / 16f, 2 / 16f, 14 / 16f, 14.99 / 16f, 14 / 16f);
    private static final AxisAlignedBB BOUNDS_UP = new AxisAlignedBB(2 / 16f, 1.01 / 16f, 2 / 16f, 14 / 16f, 16 / 16f, 14 / 16f);
    private static final AxisAlignedBB BOUNDS_DOWN = new AxisAlignedBB(2 / 16f, 0 / 16f, 2 / 16f, 14 / 16f, 14.99 / 16f, 14 / 16f);
    private static final AxisAlignedBB BOUNDS_BOTH = new AxisAlignedBB(2 / 16f, 0 / 16f, 2 / 16f, 14 / 16f, 16 / 16f, 14 / 16f);

    @Override
    List<TankRenderInfo> getTanksToRender(TileEntityFluidTank te) {
        boolean up = te.getBlockState().get(BlockPneumaticCraft.UP);
        boolean down = te.getBlockState().get(BlockPneumaticCraft.DOWN);
        AxisAlignedBB bounds;
        if (up && down)
            bounds = BOUNDS_BOTH;
        else if (up)
            bounds = BOUNDS_UP;
        else if (down)
            bounds = BOUNDS_DOWN;
        else
            bounds = BOUNDS_NONE;
        return Collections.singletonList(new TankRenderInfo(te.getTank(), bounds));
    }

    public static class ItemInfoProvider extends FluidItemRenderInfoProvider {
        @Override
        public List<TankRenderInfo> getTanksToRender(ItemStack stack) {
            return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(h -> {
                FluidTank tank = new FluidTank(h.getTankCapacity(0));
                tank.setFluid(h.getFluidInTank(0));
                return ImmutableList.of(new TankRenderInfo(tank, BOUNDS_NONE));
            }).orElse(null);
        }
    }
}
