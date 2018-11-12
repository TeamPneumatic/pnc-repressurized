package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class ItemGunAmmoWeighted extends ItemGunAmmo {
    public ItemGunAmmoWeighted() {
        super("gun_ammo_weighted");
    }

    @Override
    protected int getCartridgeSize() {
        return ConfigHandler.minigun.weightedAmmoCartridgeSize;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00404040;
    }

    @Override
    public float getRangeMultiplier(ItemStack ammoStack) {
        return ConfigHandler.minigun.weightedAmmoRangeMultiplier;
    }

    @Override
    public float getAirUsageMultiplier(Minigun minigun, ItemStack ammoStack) {
        return ConfigHandler.minigun.weightedAmmoAirUsageMultiplier;
    }

    @Override
    public float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return ConfigHandler.minigun.weightedAmmoDamageMultiplier;
    }
}
