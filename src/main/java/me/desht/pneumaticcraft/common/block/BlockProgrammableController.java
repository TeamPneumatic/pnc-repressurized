package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockProgrammableController extends BlockPneumaticCraft {

    public BlockProgrammableController() {
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

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int getWeakPower(BlockState state, IBlockAccess par1IBlockAccess, BlockPos pos, Direction side) {
        TileEntity te = par1IBlockAccess.getTileEntity(pos);
        if (te instanceof TileEntityProgrammableController) {
            return ((TileEntityProgrammableController) te).getEmittingRedstone(side.getOpposite());
        }

        return 0;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, IBlockAccess world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityProgrammableController && entity instanceof PlayerEntity) {
            ((TileEntityProgrammableController) te).setOwner((PlayerEntity) entity);
        }
        super.onBlockPlacedBy(world, pos, state, entity, stack);
    }
}
