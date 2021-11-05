package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
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
                new DamageSourceArmorPiercing(minigun.getPlayer()) :
                super.getDamageSource(minigun);
    }
}
