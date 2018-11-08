package me.desht.pneumaticcraft.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

import javax.annotation.Nullable;

public class ItemGunAmmoArmorPiercing extends ItemGunAmmo {
    public static class DamageSourceArmorPiercing extends EntityDamageSource {
        DamageSourceArmorPiercing(@Nullable Entity damageSourceEntityIn) {
            super("armor_piercing", damageSourceEntityIn);
            setDamageBypassesArmor();
        }
    }

    public ItemGunAmmoArmorPiercing() {
        super("gun_ammo_ap");
    }

    @Override
    protected int getCartridgeSize() {
        return 250;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x0000FFFF;
    }

    @Override
    public float getDamageMultiplier(ItemStack ammoStack) {
        return 1.5f;
    }

    @Override
    protected DamageSource getDamageSource(EntityPlayer shooter) {
        return new DamageSourceArmorPiercing(shooter);
    }

}
