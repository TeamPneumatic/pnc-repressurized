package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class ItemGunAmmoWeighted extends ItemGunAmmo {
    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().minigun.weightedAmmoCartridgeSize.get();
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00404040;
    }

    @Override
    public float getRangeMultiplier(ItemStack ammoStack) {
        return ConfigHelper.common().minigun.weightedAmmoRangeMultiplier.get().floatValue();
    }

    @Override
    public float getAirUsageMultiplier(Minigun minigun, ItemStack ammoStack) {
        return ConfigHelper.common().minigun.weightedAmmoAirUsageMultiplier.get().floatValue();
    }

    @Override
    public float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return ConfigHelper.common().minigun.weightedAmmoDamageMultiplier.get().floatValue();
    }
}
