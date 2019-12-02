package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRefineryController extends BlockPneumaticCraftModeled {

    public BlockRefineryController() {
        super("refinery");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityRefineryController.class;
    }

//    @Override
//    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof TileEntityRefinery) {
//            // normally, activating any refinery block would open the master TE's gui, but if we
//            // activate with a fluid tank in hand (which can actually transfer fluid either way),
//            // then we should activate the actual refinery block that was clicked
//            TileEntityRefinery master = ((TileEntityRefinery) te).getMasterRefinery();
//            BlockPos actualPos = master.getPos();
//            boolean canTransferFluid = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(player.getHeldItem(hand), 1))
//                    .map(heldHandler -> FluidUtil.getFluidHandler(world, pos, brtr.getFace())
//                            .map(refineryHandler -> couldTransferFluidEitherWay(heldHandler, refineryHandler))
//                            .orElse(false))
//                    .orElse(false);
//            if (canTransferFluid) actualPos = pos;
//            return super.onBlockActivated(state, world, actualPos, player, hand, brtr);
//        }
//        return false;
//    }
//
//    private boolean couldTransferFluidEitherWay(IFluidHandler h1, IFluidHandler h2) {
//        FluidStack f = FluidUtil.tryFluidTransfer(h1, h2, 1000, false);
//        if (!f.isEmpty()) return true;
//        f = FluidUtil.tryFluidTransfer(h2, h1, 1000, false);
//        return !f.isEmpty();
//    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean b) {
        super.neighborChanged(state, world, pos, block, fromPos, b);

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityRefineryController) {
            ((TileEntityRefineryController) te).cacheRefineryOutputs();
        }
    }
//    @Override
//    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
//        int nRefineries = 0;
//        int up = 1, down = 1;
//        while (worldIn.getBlockState(pos.up(up++)).getBlock() instanceof BlockRefinery) {
//            nRefineries++;
//        }
//        while (worldIn.getBlockState(pos.down(down++)).getBlock() instanceof BlockRefinery) {
//            nRefineries++;
//        }
//        return nRefineries < RefineryRecipe.MAX_OUTPUTS  && super.isValidPosition(state, worldIn, pos);
//    }

}
