package me.desht.pneumaticcraft.client.render.fluid;

import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidTank;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

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
        return Collections.singletonList(new FluidTankRenderInfo(te.getTank(), up, down, bounds));
    }

    public static class ItemInfoProvider extends FluidItemRenderInfoProvider {
        @Override
        public List<TankRenderInfo> getTanksToRender(ItemStack stack) {
            return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                    .map(h -> Collections.singletonList(new TankRenderInfo(h.getFluidInTank(0), h.getTankCapacity(0), BOUNDS_NONE)))
                    .orElse(Collections.emptyList());
        }
    }

    private static class FluidTankRenderInfo extends TankRenderInfo {
        private final boolean up;
        private final boolean down;

        FluidTankRenderInfo(IFluidTank tank, boolean up, boolean down, AxisAlignedBB bounds) {
            super(tank, bounds);
            this.up = up;
            this.down = down;
        }

        @Override
        public boolean shouldRender(Direction face) {
            switch (face) {
                case UP: return up
                        || getTank().getFluid().getAmount() < getTank().getCapacity()
                        && !getTank().getFluid().getFluid().getAttributes().isLighterThanAir();
                case DOWN: return down
                        || getTank().getFluid().getAmount() < getTank().getCapacity()
                        && getTank().getFluid().getFluid().getAttributes().isLighterThanAir();
                default:
                    return true;
            }
        }
    }
}
