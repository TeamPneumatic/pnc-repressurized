package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockAphorismTile extends BlockPneumaticCraft {
    BlockAphorismTile() {
        super(Material.ROCK, "aphorism_tile");
        setHardness(1.5f);
        setResistance(4.0f);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (!source.getBlockState(pos).getPropertyKeys().contains(ROTATION)) {
            // getBoundingBox() can be called during placement (from World#mayPlace), before the
            // block is actually placed; handle this, or we'll crash with an IllegalArgumentException
            return FULL_BLOCK_AABB;
        }
        EnumFacing dir = getRotation(source, pos);
        return new AxisAlignedBB(
                dir.getFrontOffsetX() <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetY() <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetZ() <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetX() >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetY() >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS,
                dir.getFrontOffsetZ() >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAphorismTile.class;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

//    @Override
//    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, BlockPos pos) {
//        EnumFacing dir = getRotation(blockAccess, pos);
//        setBlockBounds(dir.getFrontOffsetX() <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS, dir.getFrontOffsetY() <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS, dir.getFrontOffsetZ() <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS, dir.getFrontOffsetX() >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS, dir.getFrontOffsetY() >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS, dir.getFrontOffsetZ() >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS);
//    }
//
//    @Override
//    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
//        setBlockBoundsBasedOnState(world, pos);
//        super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
//    }
//
//    @Override
//    public void setBlockBoundsForItemRender() {
//        setBlockBounds(0, 0, 0.5F - BBConstants.APHORISM_TILE_THICKNESS / 2, 1, 1, 0.5F + BBConstants.APHORISM_TILE_THICKNESS / 2);
//    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack iStack) {
        super.onBlockPlacedBy(world, pos, state, entityLiving, iStack);
        EnumFacing rotation = getRotation(world, pos);
        if (rotation.getAxis() == Axis.Y) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityAphorismTile) {
                ((TileEntityAphorismTile) te).textRotation = (((int) entityLiving.rotationYaw + 45) / 90 + 2) % 4;
            }
        }
        if (world.isRemote && entityLiving instanceof EntityPlayer) {
            ((EntityPlayer) entityLiving).openGui(PneumaticCraftRepressurized.instance, EnumGuiId.APHORISM_TILE.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing face) {
        if (player.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityAphorismTile) {
                TileEntityAphorismTile teAt = (TileEntityAphorismTile) tile;
                if (++teAt.textRotation > 3) teAt.textRotation = 0;
                return true;
            } else {
                return false;
            }
        } else {
            return super.rotateBlock(world, player, pos, face);
        }
    }

    @Override
    protected boolean rotateForgeWay() {
        return false;
    }
}
