package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatSink;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockHeatSink extends BlockPneumaticCraftModeled {

    BlockHeatSink() {
        super(Material.IRON, "heat_sink");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityHeatSink.class;
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
                dir.getXOffset() <= 0 ? 0 : 1F - BBConstants.HEAT_SINK_THICKNESS,
                dir.getYOffset() <= 0 ? 0 : 1F - BBConstants.HEAT_SINK_THICKNESS,
                dir.getZOffset() <= 0 ? 0 : 1F - BBConstants.HEAT_SINK_THICKNESS,
                dir.getXOffset() >= 0 ? 1 : BBConstants.HEAT_SINK_THICKNESS,
                dir.getYOffset() >= 0 ? 1 : BBConstants.HEAT_SINK_THICKNESS,
                dir.getZOffset() >= 0 ? 1 : BBConstants.HEAT_SINK_THICKNESS
        );
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(ROTATION, facing.getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
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
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityHeatSink && entity instanceof EntityLivingBase) {
            IHeatExchangerLogic heat = ((TileEntityHeatSink) te).getHeatExchangerLogic(null);
            int temp = (int) ((TileEntityHeatSink) te).getHeatExchangerLogic(null).getTemperature();
            if (temp > 323) { // +50C
                entity.attackEntityFrom(DamageSource.HOT_FLOOR, 2);
                if (temp > 373) { // +100C
                    entity.setFire(3);
                }
            } else if (temp < 243) { // -30C
                int durationSec = (243 - (int)heat.getTemperature()) / 10;
                int amplifier = (243 - (int) heat.getTemperature()) / 80;
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, durationSec * 20, amplifier));
                if (temp < 213) { // -60C
                    entity.attackEntityFrom(DamageSourcePneumaticCraft.FREEZING, 2);
                }
            }
        }
    }
}
