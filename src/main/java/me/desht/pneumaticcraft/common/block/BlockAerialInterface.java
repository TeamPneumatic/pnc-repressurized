package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
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

public class BlockAerialInterface extends BlockPneumaticCraft {
    BlockAerialInterface() {
        super(Material.IRON, "aerial_interface");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAerialInterface.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.AERIAL_INTERFACE;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack par6ItemStack) {
        TileEntity te = par1World.getTileEntity(pos);
        if (te instanceof TileEntityAerialInterface && entity instanceof EntityPlayer) {
            ((TileEntityAerialInterface) te).setPlayer(((EntityPlayer) entity));
        }
        super.onBlockPlacedBy(par1World, pos, state, entity, par6ItemStack);
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof TileEntityAerialInterface) {
            return ((TileEntityAerialInterface)te ).shouldEmitRedstone() ? 15 : 0;
        }
        return 0;
    }
}
