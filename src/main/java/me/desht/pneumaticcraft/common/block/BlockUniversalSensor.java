package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockUniversalSensor extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(
            BBConstants.UNIVERSAL_SENSOR_MIN_POS, 0F, BBConstants.UNIVERSAL_SENSOR_MIN_POS,
            BBConstants.UNIVERSAL_SENSOR_MAX_POS, BBConstants.UNIVERSAL_SENSOR_MAX_POS_TOP, BBConstants.UNIVERSAL_SENSOR_MAX_POS
    );
    private static final AxisAlignedBB COLLISION_BOUNDS = new AxisAlignedBB(
            BBConstants.UNIVERSAL_SENSOR_MIN_POS, BBConstants.UNIVERSAL_SENSOR_MIN_POS, BBConstants.UNIVERSAL_SENSOR_MIN_POS,
            BBConstants.UNIVERSAL_SENSOR_MAX_POS, BBConstants.UNIVERSAL_SENSOR_MAX_POS_TOP, BBConstants.UNIVERSAL_SENSOR_MAX_POS
    );

    public BlockUniversalSensor() {
        super(Material.IRON, "universal_sensor");
        setBlockBounds(BLOCK_BOUNDS);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this,
                BlockPneumaticCraft.DOWN, BlockPneumaticCraft.NORTH, BlockPneumaticCraft.SOUTH, BlockPneumaticCraft.WEST, BlockPneumaticCraft.EAST);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return 0;
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        if (te instanceof TileEntityUniversalSensor) {
            for (int i = 0; i < 6; i++) {
                if (i == 1) continue;  // never connects on the UP face
                state = state.withProperty(BlockPneumaticCraft.CONNECTION_PROPERTIES[i], ((TileEntityUniversalSensor) te).sidesConnected[i]);
            }
        }
        return state;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLLISION_BOUNDS;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityUniversalSensor.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.UNIVERSAL_SENSOR;
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        return te instanceof TileEntityUniversalSensor ? ((TileEntityUniversalSensor) te).redstoneStrength : 0;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }
}
