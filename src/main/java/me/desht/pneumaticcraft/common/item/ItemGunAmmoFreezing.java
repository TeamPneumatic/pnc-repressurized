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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
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
        double knockback = -1;
        if (target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) target;
            living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, living.getRNG().nextInt(40) + 40, 3));
            if (minigun.dispenserWeightedPercentage(ConfigHandler.minigun.freezingAmmoEntityIceChance)) {
                // temporarily stop the target getting knocked back, since it might be knocked out of the freeze zone
                knockback = living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue();
                living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
                encaseInFakeIce(minigun, target);
            }
        }
        if (knockback != -1) {
            ((EntityLivingBase) target).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(knockback);
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    private void encaseInFakeIce(Minigun minigun, Entity target) {
        World world = target.world;
        Random rnd = world.rand;
        TemporaryBlockManager mgr = TemporaryBlockManager.getManager(world);
        AxisAlignedBB aabb = target.getEntityBoundingBox();
        for (int y = (int) aabb.minY; y <= aabb.maxY; y++) {
            for (int x = (int) Math.floor(aabb.minX); x <= aabb.maxX; x++) {
                for (int z = (int) Math.floor(aabb.minZ); z <= aabb.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
                        mgr.trySetBlock(minigun.getPlayer(), EnumFacing.UP, pos, Blockss.FAKE_ICE.getDefaultState(), 60 + rnd.nextInt(40));
                    }
                }
            }
        }
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockPos pos, EnumFacing face, Vec3d hitVec) {
        World world = minigun.getWorld();
        if (world.provider.getDimensionType() != DimensionType.NETHER && minigun.dispenserWeightedPercentage(ConfigHandler.minigun.freezingAmmoBlockIceChance)) {
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
