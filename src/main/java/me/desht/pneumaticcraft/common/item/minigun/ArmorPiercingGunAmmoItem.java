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

import me.desht.pneumaticcraft.common.PNCDamageSource;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class ArmorPiercingGunAmmoItem extends AbstractGunAmmoItem {
    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().minigun.armorPiercingAmmoCartridgeSize.get();
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x0000FFFF;
    }

    @Override
    public float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return ConfigHelper.common().minigun.apAmmoDamageMultiplier.get().floatValue();
    }

    @Override
    protected DamageSource getDamageSource(Minigun minigun) {
        return minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.apAmmoIgnoreArmorChance.get()) ?
                PNCDamageSource.minigunAP(minigun.getPlayer().getLevel(), minigun.getPlayer()) :
                super.getDamageSource(minigun);
    }
}
