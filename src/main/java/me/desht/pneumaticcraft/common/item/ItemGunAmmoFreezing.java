package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potions;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemGunAmmoFreezing extends ItemGunAmmo {
    private static final UUID KNOCKBACK_UUID = UUID.fromString("49b2f9a8-228d-4be5-96ce-6f9ce2877145");
    private static final AttributeModifier KNOCKBACK = new AttributeModifier(KNOCKBACK_UUID, "Temp. Knockback Resist",1.0, AttributeModifier.Operation.ADDITION);

    @Override
    public int getMaxDamage(ItemStack stack) {
        return PNCConfig.Common.Minigun.freezingAmmoCartridgeSize;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x0040A0FF;
    }

    @Override
    protected float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        double mul = super.getDamageMultiplier(target, ammoStack);
        if (target != null && target.isImmuneToFire()) {
            mul *= 1.5;
        }
        return (float) mul;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;
            living.addPotionEffect(new EffectInstance(Effects.SLOWNESS, living.getRNG().nextInt(40) + 40, 3));
            if (minigun.dispenserWeightedPercentage(PNCConfig.Common.Minigun.freezingAmmoEntityIceChance)) {
                createFreezeCloud(minigun, target);
            }
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    private void createFreezeCloud(Minigun minigun, Entity target) {
        World world = target.getEntityWorld();
        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, target.getPosX(), target.getPosY(), target.getPosZ());
        cloud.setPotion(Potions.SLOWNESS);
        cloud.setOwner(minigun.getPlayer());
        cloud.addEffect(new EffectInstance(Effects.SLOWNESS, 100, 3));
        cloud.addEffect(new EffectInstance(Effects.WITHER, 20, 1));
        cloud.setRadius(2.0f);
        cloud.setDuration(60);
        cloud.setRadiusOnUse(-0.5f);
        cloud.setWaitTime(20);
        cloud.setColor(0xFF00C0FF);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        world.addEntity(cloud);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        World world = minigun.getWorld();
        BlockPos pos = brtr.getPos();
        // world.getDimensionType().doesFluidVaporize()
        if (!world.func_230315_m_().func_236040_e_() && minigun.dispenserWeightedPercentage(PNCConfig.Common.Minigun.freezingAmmoBlockIceChance)) {
            BlockPos pos1;
            if (world.getBlockState(pos).getShape(world, pos) == VoxelShapes.fullCube() || brtr.getFace() != Direction.UP) {
                pos1 = pos.offset(brtr.getFace());
            } else {
                pos1 = pos;
            }
            BlockState newState = null;
            if (world.isAirBlock(pos1) && !world.isAirBlock(pos1.down())) {
                // form snow layers on solid blocks
                newState = Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, 1);
            } else if (world.getBlockState(pos1).getBlock() == Blocks.SNOW) {
                // grow existing snow layers
                BlockState state = world.getBlockState(pos1);
                int level = state.get(SnowBlock.LAYERS);
                if (level < 8) {
                    newState = Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, level + 1);
                } else {
                    newState = Blocks.SNOW_BLOCK.getDefaultState();
                }
            } else if (world.getBlockState(pos1).getBlock() == Blocks.WATER) {
                // freeze surface water
                Vector3d eye = minigun.getPlayer().getEyePosition(0f);
                RayTraceContext ctx = new RayTraceContext(eye, brtr.getHitVec(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, minigun.getPlayer());
                BlockRayTraceResult res = world.rayTraceBlocks(ctx);
                if (res.getType() == RayTraceResult.Type.BLOCK) {
                    pos1 = res.getPos();
                    newState = Blocks.ICE.getDefaultState();
                }
            }
            if (newState != null) {
                PneumaticCraftUtils.tryPlaceBlock(world, pos1, minigun.getPlayer(), brtr.getFace(), newState);
            }
        }
        return super.onBlockHit(minigun, ammo, brtr);
    }

}
