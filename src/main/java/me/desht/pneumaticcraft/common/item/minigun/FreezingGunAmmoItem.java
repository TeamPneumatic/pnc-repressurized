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

package me.desht.pneumaticcraft.common.item.minigun;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.List;
import java.util.Optional;

public class FreezingGunAmmoItem extends AbstractGunAmmoItem {
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
        if (target instanceof LivingEntity living) {
            living.setIsInPowderSnow(true);
            living.setTicksFrozen(living.getTicksFrozen() + 35);
            if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.freezingAmmoEntityIceChance.get())) {
                createFreezeCloud(minigun, target);
            }
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    private void createFreezeCloud(Minigun minigun, Entity target) {
        Level world = target.getCommandSenderWorld();
        AreaEffectCloud cloud = new AreaEffectCloud(world, target.getX(), target.getY(), target.getZ());
        cloud.setPotionContents(new PotionContents(Optional.of(Potions.SLOWNESS), Optional.of(0xFF00C0FF), List.of()));
        cloud.setOwner(minigun.getPlayer());
        cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
        cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 20, 1));
        cloud.setRadius(2.0f);
        cloud.setDuration(60);
        cloud.setRadiusOnUse(-0.5f);
        cloud.setWaitTime(20);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        world.addFreshEntity(cloud);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockHitResult brtr) {
        Level world = minigun.getWorld();
        BlockPos pos = brtr.getBlockPos();
        if (!world.dimensionType().ultraWarm() && minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.freezingAmmoBlockIceChance.get())) {
            BlockPos pos1;
            if (world.getBlockState(pos).getShape(world, pos) == Shapes.block() || brtr.getDirection() != Direction.UP) {
                pos1 = pos.relative(brtr.getDirection());
            } else {
                pos1 = pos;
            }
            BlockState newState = null;
            if (world.isEmptyBlock(pos1) && !world.isEmptyBlock(pos1.below())) {
                // form snow layers on solid blocks
                newState = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1);
            } else if (world.getBlockState(pos1).getBlock() == Blocks.SNOW) {
                // grow existing snow layers
                BlockState state = world.getBlockState(pos1);
                int level = state.getValue(SnowLayerBlock.LAYERS);
                if (level < 8) {
                    newState = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, level + 1);
                } else {
                    newState = Blocks.SNOW_BLOCK.defaultBlockState();
                }
            } else if (world.getBlockState(pos1).getBlock() == Blocks.WATER) {
                // freeze surface water
                Vec3 eye = minigun.getPlayer().getEyePosition(0f);
                ClipContext ctx = new ClipContext(eye, brtr.getLocation(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, minigun.getPlayer());
                BlockHitResult res = world.clip(ctx);
                if (res.getType() == HitResult.Type.BLOCK) {
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
