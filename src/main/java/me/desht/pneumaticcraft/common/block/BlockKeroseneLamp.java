package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockKeroseneLamp extends BlockPneumaticCraftModeled {
    private static final AxisAlignedBB BLOCK_BOUNDS_NS = new AxisAlignedBB(
            BBConstants.KEROSENE_LAMP_LENGTH_MIN, 0, BBConstants.KEROSENE_LAMP_WIDTH_MIN,
            1 - BBConstants.KEROSENE_LAMP_LENGTH_MIN, BBConstants.KEROSENE_LAMP_TOP_MAX, 1 - BBConstants.KEROSENE_LAMP_WIDTH_MIN
    );
    private static final AxisAlignedBB BLOCK_BOUNDS_EW = new AxisAlignedBB(
            BBConstants.KEROSENE_LAMP_WIDTH_MIN, 0, BBConstants.KEROSENE_LAMP_LENGTH_MIN,
            1 - BBConstants.KEROSENE_LAMP_WIDTH_MIN, BBConstants.KEROSENE_LAMP_TOP_MAX, 1 - BBConstants.KEROSENE_LAMP_LENGTH_MIN
    );

    BlockKeroseneLamp() {
        super(Material.IRON, "kerosene_lamp");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (!source.getBlockState(pos).getPropertyKeys().contains(ROTATION)) {
            // getBoundingBox() can be called during placement (from World#mayPlace), before the
            // block is actually placed; handle this, or we'll crash with an IllegalArgumentException
            return BLOCK_BOUNDS_EW;
        }

        EnumFacing facing = getRotation(source, pos);
        if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
            return BLOCK_BOUNDS_NS;
        } else {
            return BLOCK_BOUNDS_EW;
        }
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.KEROSENE_LAMP;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityKeroseneLamp.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityKeroseneLamp lamp = (TileEntityKeroseneLamp) world.getTileEntity(pos);
        return lamp != null && lamp.getRange() > 0 ? 15 : 0;
    }

}
