package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.recipes.RefineryRecipe;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockRefinery extends BlockPneumaticCraftModeled {

    public BlockRefinery() {
        super(Material.IRON, "refinery");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityRefinery.class;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float par7, float par8, float par9) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityRefinery) {
            // normally, activating any refinery block would open the master TE's gui, but if we
            // activate with a fluid tank in hand (which can actually transfer fluid either way),
            // then we should activate the actual refinery block that was clicked
            TileEntityRefinery master = ((TileEntityRefinery) te).getMasterRefinery();
            BlockPos actualPos = master.getPos();
            IFluidHandler heldHandler = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(player.getHeldItem(hand), 1));
            if (heldHandler != null ) {
                IFluidHandler refineryHandler = FluidUtil.getFluidHandler(world, pos, side);
                if (refineryHandler != null && couldTransferFluidEitherWay(heldHandler, refineryHandler)) {
                    actualPos = pos;
                }
            }
            return super.onBlockActivated(world, actualPos, state, player, hand, side, par7, par8, par9);
        }
        return false;
    }

    private boolean couldTransferFluidEitherWay(IFluidHandler h1, IFluidHandler h2) {
        FluidStack f = FluidUtil.tryFluidTransfer(h1, h2, 1000, false);
        if (f != null && f.amount > 0) return true;
        f = FluidUtil.tryFluidTransfer(h2, h1, 1000, false);
        return f != null && f.amount > 0;
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
