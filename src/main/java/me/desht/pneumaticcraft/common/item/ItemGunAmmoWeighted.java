package me.desht.pneumaticcraft.common.item;

import net.minecraft.item.ItemStack;

public class ItemGunAmmoWeighted extends ItemGunAmmo {
    public ItemGunAmmoWeighted() {
        super("gun_ammo_weighted");
    }

    @Override
    protected int getCartridgeSize() {
        return 250;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00404040;
    }

    @Override
    public float getRangeMultiplier(ItemStack ammoStack) {
        return 0.2f;
    }

    @Override
    public float getAirUsageMultiplier(ItemStack ammoStack) {
        return 8.0f;
    }

    @Override
    public float getDamageMultiplier(ItemStack ammoStack) {
        return 3.0f;
    }
}
