package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.recipes.RefineryRecipe;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class BlockRefinery extends BlockPneumaticCraftModeled {

    BlockRefinery() {
        super(Material.IRON, "refinery");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityRefinery.class;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float par7, float par8, float par9) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityRefinery) {
            // normally, activating the block should open the master TE's gui
            // but if we activate with a fluid tank in hand (which can accept the fluid), then try to
            // extract from the actual refinery block that was activated
            TileEntityRefinery master = ((TileEntityRefinery) te).getMasterRefinery();
            BlockPos actualPos = master.getPos();
            IFluidHandler handler = FluidUtil.getFluidHandler(player.getHeldItem(hand));
            if (handler != null) {
                IFluidHandler srcHandler = FluidUtil.getFluidHandler(world, pos, side);
                if (srcHandler != null) {
                    FluidStack f = FluidUtil.tryFluidTransfer(handler, srcHandler, srcHandler.getTankProperties()[0].getCapacity(), false);
                    if (f == null || f.amount == 0) {
                        return false;
                    } else {
                        actualPos = pos;
                    }
                }
            }

            return super.onBlockActivated(world, actualPos, state, player, hand, side, par7, par8, par9);
        }
        return false;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.REFINERY;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        int nRefineries = 0;
        int up = 1, down = 1;
        while (worldIn.getBlockState(pos.up(up++)).getBlock() instanceof BlockRefinery) {
            nRefineries++;
        }
        while (worldIn.getBlockState(pos.down(down++)).getBlock() instanceof BlockRefinery) {
            nRefineries++;
        }
        return nRefineries < RefineryRecipe.MAX_OUTPUTS && super.canPlaceBlockAt(worldIn, pos);
    }
}
