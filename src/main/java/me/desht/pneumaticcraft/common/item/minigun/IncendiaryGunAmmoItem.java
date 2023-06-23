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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class IncendiaryGunAmmoItem extends AbstractGunAmmoItem {

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().minigun.incendiaryAmmoCartridgeSize.get();
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FF8000;
    }

    @Override
    protected DamageSource getDamageSource(Minigun minigun) {
        return minigun.getPlayer().damageSources().onFire();
//        return super.getDamageSource(minigun).setIsFire();
    }

    @Override
    protected float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        float mul = super.getDamageMultiplier(target, ammoStack);
        return target != null && target.fireImmune() ? mul * 0.1f : mul;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.incendiaryAmmoEntityIgniteChance.get())) {
            target.setSecondsOnFire(ConfigHelper.common().minigun.incendiaryAmmoFireDuration.get());
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockHitResult brtr) {
        if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.incendiaryAmmoBlockIgniteChance.get())) {
            BlockPos firePos = brtr.getBlockPos().relative(brtr.getDirection());
            if (minigun.getWorld().getBlockState(firePos).isAir()) {
                PneumaticCraftUtils.tryPlaceBlock(minigun.getWorld(), firePos, minigun.getPlayer(), brtr.getDirection(),
                        Blocks.FIRE.defaultBlockState());
            }
        }
        return super.onBlockHit(minigun, ammo, brtr);
    }
}
