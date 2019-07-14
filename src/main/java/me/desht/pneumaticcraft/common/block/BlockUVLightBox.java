package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockUVLightBox extends BlockPneumaticCraftModeled {
    private static final PropertyBool LOADED = PropertyBool.create("loaded");

    private static final AxisAlignedBB BLOCK_BOUNDS_NS = new AxisAlignedBB(
            BBConstants.UV_LIGHT_BOX_LENGTH_MIN, 0, BBConstants.UV_LIGHT_BOX_WIDTH_MIN,
            1 - BBConstants.UV_LIGHT_BOX_LENGTH_MIN, BBConstants.UV_LIGHT_BOX_TOP_MAX, 1 - BBConstants.UV_LIGHT_BOX_WIDTH_MIN);
    private static final AxisAlignedBB BLOCK_BOUNDS_EW = new AxisAlignedBB(
            BBConstants.UV_LIGHT_BOX_WIDTH_MIN, 0, BBConstants.UV_LIGHT_BOX_LENGTH_MIN,
            1 - BBConstants.UV_LIGHT_BOX_WIDTH_MIN, BBConstants.UV_LIGHT_BOX_TOP_MAX, 1 - BBConstants.UV_LIGHT_BOX_LENGTH_MIN
    );

    public BlockUVLightBox() {
        super(Material.IRON, "uv_light_box");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION, LOADED);
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        if (te instanceof TileEntityUVLightBox) {
            state = state.withProperty(LOADED, ((TileEntityUVLightBox) te).hasLoadedPCB);
        }
        return state;
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        if (!source.getBlockState(pos).getPropertyKeys().contains(ROTATION)) {
            // getBoundingBox() can be called during placement (from World#mayPlace), before the
            // block is actually placed; handle this, or we'll crash with an IllegalArgumentException
            return BLOCK_BOUNDS_EW;
        }
        Direction facing = getRotation(source, pos);
        return facing == Direction.NORTH || facing == Direction.SOUTH ? BLOCK_BOUNDS_NS : BLOCK_BOUNDS_EW;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
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
    public int getLightValue(BlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityUVLightBox ? ((TileEntityUVLightBox) te).getLightLevel() : 0;
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
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof TileEntityUVLightBox) {
            return ((TileEntityUVLightBox) te).shouldEmitRedstone() ? 15 : 0;
        }
        return 0;
    }
}
