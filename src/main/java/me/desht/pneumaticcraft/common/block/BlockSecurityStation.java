package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockSecurityStation extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(
            BBConstants.SECURITY_STATION_MIN_POS, 0F, BBConstants.SECURITY_STATION_MIN_POS,
            BBConstants.SECURITY_STATION_MAX_POS, BBConstants.SECURITY_STATION_MAX_POS_TOP, BBConstants.SECURITY_STATION_MAX_POS
    );
    private static final AxisAlignedBB COLLISION_BOUNDS = new AxisAlignedBB(
            BBConstants.SECURITY_STATION_MIN_POS, BBConstants.SECURITY_STATION_MIN_POS, BBConstants.SECURITY_STATION_MIN_POS,
            BBConstants.SECURITY_STATION_MAX_POS, BBConstants.SECURITY_STATION_MAX_POS_TOP, BBConstants.SECURITY_STATION_MAX_POS
    );

    BlockSecurityStation() {
        super(Material.IRON, "security_station");
        setBlockBounds(BLOCK_BOUNDS);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLLISION_BOUNDS;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntitySecurityStation.class;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack iStack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntitySecurityStation && entityLiving != null) {
            ((TileEntitySecurityStation) te).sharedUsers.add(((EntityPlayer) entityLiving).getGameProfile());
        }
        super.onBlockPlacedBy(world, pos, state, entityLiving, iStack);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float par7, float par8, float par9) {
        if (player.isSneaking()) return false;
        else {
            if (!world.isRemote) {
                TileEntitySecurityStation te = (TileEntitySecurityStation) world.getTileEntity(pos);
                if (te != null) {
                    if (te.isPlayerOnWhiteList(player)) {
                        player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.SECURITY_STATION_INVENTORY.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
                    } else if (!te.hasValidNetwork()) {
                        player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "This Security Station is out of order: Its network hasn't been properly configured."), false);
                    } else if (te.hasPlayerHacked(player)) {
                        player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "You've already hacked this Security Station!"), false);
                    } else if (getPlayerHackLevel(player) < te.getSecurityLevel()) {
                        player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You can't access or hack this Security Station. To hack it you need at least a Pneumatic Helmet upgraded with " + te.getSecurityLevel() + " Security upgrade(s)."), false);
                    } else {
                        player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.HACKING.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
                    }
                }
            }
            return true;
        }
    }

    private int getPlayerHackLevel(EntityPlayer player) {
        ItemStack armorStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return armorStack.getItem() == Itemss.PNEUMATIC_HELMET ? ItemPneumaticArmor.getUpgrades(EnumUpgrade.SECURITY, armorStack) : 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof TileEntitySecurityStation) {
            return ((TileEntitySecurityStation) te).shouldEmitRedstone() ? 15 : 0;
        }
        return 0;
    }
}
