package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;

public class BlockChargingStation extends BlockPneumaticCraftCamo {
    private static final PropertyBool CHARGE_PAD = PropertyBool.create("charge_pad");

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(
            BBConstants.CHARGING_STATION_MIN_POS, 0F, BBConstants.CHARGING_STATION_MIN_POS,
            BBConstants.CHARGING_STATION_MAX_POS, BBConstants.CHARGING_STATION_MAX_POS_TOP, BBConstants.CHARGING_STATION_MAX_POS);

    BlockChargingStation() {
        super(Material.IRON, "charging_station");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[] { ROTATION, CHARGE_PAD }, UNLISTED_CAMO_PROPERTIES);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        if (te instanceof TileEntityChargingStation) {
            return state.withProperty(CHARGE_PAD, ((TileEntityChargingStation) te).dispenserUpgradeInserted);
        }
        return state;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof TileEntityChargingStation && ((TileEntityChargingStation) te).dispenserUpgradeInserted) {
            return FULL_BLOCK_AABB;
        } else {
            return BOUNDS;
        }
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityChargingStation.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.CHARGING_STATION;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof TileEntityChargingStation) {
            return ((TileEntityChargingStation) te).shouldEmitRedstone() ? 15 : 0;
        }
        return 0;
    }
}
