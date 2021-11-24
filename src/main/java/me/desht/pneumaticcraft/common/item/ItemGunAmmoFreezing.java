/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
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
        return ConfigHelper.common().minigun.freezingAmmoCartridgeSize.get();
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x0040A0FF;
    }

    @Override
    protected float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        double mul = super.getDamageMultiplier(target, ammoStack);
        if (target != null && target.fireImmune()) {
            mul *= 1.5;
        }
        return (float) mul;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;
            living.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, living.getRandom().nextInt(40) + 40, 3));
            if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.freezingAmmoEntityIceChance.get())) {
                createFreezeCloud(minigun, target);
            }
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    private void createFreezeCloud(Minigun minigun, Entity target) {
        World world = target.getCommandSenderWorld();
        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, target.getX(), target.getY(), target.getZ());
        cloud.setPotion(Potions.SLOWNESS);
        cloud.setOwner(minigun.getPlayer());
        cloud.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, 3));
        cloud.addEffect(new EffectInstance(Effects.WITHER, 20, 1));
        cloud.setRadius(2.0f);
        cloud.setDuration(60);
        cloud.setRadiusOnUse(-0.5f);
        cloud.setWaitTime(20);
        cloud.setFixedColor(0xFF00C0FF);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        world.addFreshEntity(cloud);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        World world = minigun.getWorld();
        BlockPos pos = brtr.getBlockPos();
        if (!world.dimensionType().ultraWarm() && minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.freezingAmmoBlockIceChance.get())) {
            BlockPos pos1;
            if (world.getBlockState(pos).getShape(world, pos) == VoxelShapes.block() || brtr.getDirection() != Direction.UP) {
                pos1 = pos.relative(brtr.getDirection());
            } else {
                pos1 = pos;
            }
            BlockState newState = null;
            if (world.isEmptyBlock(pos1) && !world.isEmptyBlock(pos1.below())) {
                // form snow layers on solid blocks
                newState = Blocks.SNOW.defaultBlockState().setValue(SnowBlock.LAYERS, 1);
            } else if (world.getBlockState(pos1).getBlock() == Blocks.SNOW) {
                // grow existing snow layers
                BlockState state = world.getBlockState(pos1);
                int level = state.getValue(SnowBlock.LAYERS);
                if (level < 8) {
                    newState = Blocks.SNOW.defaultBlockState().setValue(SnowBlock.LAYERS, level + 1);
                } else {
                    newState = Blocks.SNOW_BLOCK.defaultBlockState();
                }
            } else if (world.getBlockState(pos1).getBlock() == Blocks.WATER) {
                // freeze surface water
                Vector3d eye = minigun.getPlayer().getEyePosition(0f);
                RayTraceContext ctx = new RayTraceContext(eye, brtr.getLocation(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.SOURCE_ONLY, minigun.getPlayer());
                BlockRayTraceResult res = world.clip(ctx);
                if (res.getType() == RayTraceResult.Type.BLOCK) {
                    pos1 = res.getBlockPos();
                    newState = Blocks.ICE.defaultBlockState();
                }
            }
            if (newState != null) {
                PneumaticCraftUtils.tryPlaceBlock(world, pos1, minigun.getPlayer(), brtr.getDirection(), newState);
            }
        }
        return super.onBlockHit(minigun, ammo, brtr);
    }

}
