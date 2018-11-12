package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.TemporaryBlockManager;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class ItemGunAmmoFreezing extends ItemGunAmmo {
    public ItemGunAmmoFreezing() {
        super("gun_ammo_freezing");
    }

    @Override
    protected int getCartridgeSize() {
        return ConfigHandler.minigun.freezingAmmoCartridgeSize;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x0040A0FF;
    }

    @Override
    protected float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        float mul = super.getDamageMultiplier(target, ammoStack);
        if (target != null && target.isImmuneToFire()) {
            mul *= 1.5;
        }
        return mul;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (target instanceof EntityLivingBase) {
            Random rnd = ((EntityLivingBase) target).getRNG();
            int duration = rnd.nextInt(40) + 40;
            int amplifier = rnd.nextInt(3) + 1;
            ((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, amplifier));
            if (minigun.dispenserWeightedPercentage(ConfigHandler.minigun.freezingAmmoEntityIceChance)) {
                BlockPos pos = target.getPosition();
                TemporaryBlockManager mgr = TemporaryBlockManager.getManager(minigun.getWorld());
                if (minigun.getWorld().isAirBlock(pos)) {
                    mgr.trySetBlock(minigun.getPlayer(), EnumFacing.UP, pos, Blockss.FAKE_ICE.getDefaultState(), 60 + rnd.nextInt(40));
                }
                if (target.getEyeHeight() >= 1f && minigun.getWorld().isAirBlock(pos.up())) {
                    mgr.trySetBlock(minigun.getPlayer(), EnumFacing.UP, pos.up(), Blockss.FAKE_ICE.getDefaultState(), 60 + rnd.nextInt(40));
                }
            }
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockPos pos, EnumFacing face, Vec3d hitVec) {
        World world = minigun.getWorld();
        if (minigun.dispenserWeightedPercentage(ConfigHandler.minigun.freezingAmmoBlockIceChance)) {
            BlockPos pos1;
            if (world.getBlockState(pos).isFullCube() || face != EnumFacing.UP) {
                pos1 = pos.offset(face);
            } else {
                pos1 = pos;
            }
            IBlockState newState = null;
            if (world.isAirBlock(pos1) && !world.isAirBlock(pos1.down())) {
                // form snow layers on solid blocks
                newState = Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 1);
            } else if (world.getBlockState(pos1).getBlock() == Blocks.SNOW_LAYER) {
                // grow existing snow layers
                IBlockState state = world.getBlockState(pos1);
                int level = state.getValue(BlockSnow.LAYERS);
                if (level < 8) {
                    newState = Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, level + 1);
                } else {
                    newState = Blocks.SNOW.getDefaultState();
                }
            } else if (world.getBlockState(pos1).getBlock() == Blocks.WATER) {
                // freeze surface water
                Vec3d eye = minigun.getPlayer().getPositionEyes(0f);
                RayTraceResult res = world.rayTraceBlocks(eye, hitVec, true, false, false);
                if (res!= null && res.typeOfHit == RayTraceResult.Type.BLOCK) {
                    pos1 = res.getBlockPos();
                    newState = Blocks.ICE.getDefaultState();
                }
            }
            if (newState != null) {
                PneumaticCraftUtils.tryPlaceBlock(world, pos1, minigun.getPlayer(), face, newState);
            }
        }
        return super.onBlockHit(minigun, ammo, pos, face, hitVec);
    }
}
