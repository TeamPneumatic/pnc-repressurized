package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockProgrammableController extends BlockPneumaticCraft {

    BlockProgrammableController() {
        super(Material.IRON, "programmable_controller");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityProgrammableController.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.PROGRAMMABLE_CONTROLLER;
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int getWeakPower(IBlockState state, IBlockAccess par1IBlockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = par1IBlockAccess.getTileEntity(pos);
        if (te instanceof TileEntityProgrammableController) {
            return ((TileEntityProgrammableController) te).getEmittingRedstone(side.getOpposite());
        }

        return 0;
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityProgrammableController && entity instanceof EntityPlayer) {
            ((TileEntityProgrammableController) te).setOwner((EntityPlayer) entity);
        }
    }
}
