package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockUVLightBox extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BLOCK_BOUNDS_NS = new AxisAlignedBB(
            BBConstants.UV_LIGHT_BOX_LENGTH_MIN, 0, BBConstants.UV_LIGHT_BOX_WIDTH_MIN,
            1 - BBConstants.UV_LIGHT_BOX_LENGTH_MIN, BBConstants.UV_LIGHT_BOX_TOP_MAX, 1 - BBConstants.UV_LIGHT_BOX_WIDTH_MIN);
    private static final AxisAlignedBB BLOCK_BOUNDS_EW = new AxisAlignedBB(
            BBConstants.UV_LIGHT_BOX_WIDTH_MIN, 0, BBConstants.UV_LIGHT_BOX_LENGTH_MIN,
            1 - BBConstants.UV_LIGHT_BOX_WIDTH_MIN, BBConstants.UV_LIGHT_BOX_TOP_MAX, 1 - BBConstants.UV_LIGHT_BOX_LENGTH_MIN
    );

    BlockUVLightBox() {
        super(Material.IRON, "uv_light_box");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (!source.getBlockState(pos).getPropertyKeys().contains(ROTATION)) {
            // getBoundingBox() can be called during placement (from World#mayPlace), before the
            // block is actually placed; handle this, or we'll crash with an IllegalArgumentException
            return BLOCK_BOUNDS_EW;
        }
        EnumFacing facing = getRotation(source, pos);
        return facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH ? BLOCK_BOUNDS_NS : BLOCK_BOUNDS_EW;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityUVLightBox.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.UV_LIGHT_BOX;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block != this) {
            return block.getLightValue(state, world, pos);
        }
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityUVLightBox ? ((TileEntityUVLightBox) te).getLightLevel() : 0;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
