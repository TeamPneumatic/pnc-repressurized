package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockChargingStation extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(
            BBConstants.CHARGING_STATION_MIN_POS, 0F, BBConstants.CHARGING_STATION_MIN_POS,
            BBConstants.CHARGING_STATION_MAX_POS, BBConstants.CHARGING_STATION_MAX_POS_TOP, BBConstants.CHARGING_STATION_MAX_POS);

    BlockChargingStation() {
        super(Material.IRON, "charging_station");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof TileEntityChargingStation && ((TileEntityChargingStation) te).getUpgrades(EnumUpgrade.DISPENSER) > 0) {
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float par7, float par8, float par9) {
        if (!world.isRemote && player.isSneaking()) {
            TileEntityChargingStation station = (TileEntityChargingStation) world.getTileEntity(pos);
            station.setCamoStack(player.getHeldItemMainhand());
            return player.getHeldItemMainhand().getItem() instanceof ItemBlock;
        } else {
            return super.onBlockActivated(world, pos, state, player, hand, side, par7, par8, par9);
        }
    }
}
