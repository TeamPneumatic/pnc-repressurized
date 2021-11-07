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
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

import javax.annotation.Nullable;

public class ItemGunAmmoArmorPiercing extends ItemGunAmmo {
    public static class DamageSourceArmorPiercing extends EntityDamageSource {
        DamageSourceArmorPiercing(@Nullable Entity damageSourceEntityIn) {
            super("armor_piercing", damageSourceEntityIn);
            bypassArmor();
        }
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return PNCConfig.Common.Minigun.armorPiercingAmmoCartridgeSize;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x0000FFFF;
    }

    @Override
    public float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return (float) PNCConfig.Common.Minigun.apAmmoDamageMultiplier;
    }

    @Override
    protected DamageSource getDamageSource(Minigun minigun) {
        return minigun.dispenserWeightedPercentage(PNCConfig.Common.Minigun.apAmmoIgnoreArmorChance) ?
                new DamageSourceArmorPiercing(minigun.getPlayer()) :
                super.getDamageSource(minigun);
    }
}
