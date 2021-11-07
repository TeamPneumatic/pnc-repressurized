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

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockRayTraceResult;

public class ItemGunAmmoIncendiary extends ItemGunAmmo {

    @Override
    public int getMaxDamage(ItemStack stack) {
        return PNCConfig.Common.Minigun.incendiaryAmmoCartridgeSize;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FF8000;
    }

    @Override
    protected DamageSource getDamageSource(Minigun minigun) {
        return super.getDamageSource(minigun).setIsFire();
    }

    @Override
    protected float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        double mult = super.getDamageMultiplier(target, ammoStack);
        if (target != null && target.fireImmune()) {
            mult *= 0.1;
        }
        return (float) mult;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (minigun.dispenserWeightedPercentage(PNCConfig.Common.Minigun.incendiaryAmmoEntityIgniteChance)) {
            target.setSecondsOnFire(PNCConfig.Common.Minigun.incendiaryAmmoFireDuration);
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        if (minigun.dispenserWeightedPercentage(PNCConfig.Common.Minigun.incendiaryAmmoBlockIgniteChance)) {
            PneumaticCraftUtils.tryPlaceBlock(minigun.getWorld(), brtr.getBlockPos().relative(brtr.getDirection()), minigun.getPlayer(), brtr.getDirection(), Blocks.FIRE.defaultBlockState());
        }
        return super.onBlockHit(minigun, ammo, brtr);
    }
}
