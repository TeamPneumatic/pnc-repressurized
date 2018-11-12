package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
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
            setDamageBypassesArmor();
        }
    }

    public ItemGunAmmoArmorPiercing() {
        super("gun_ammo_ap");
    }

    @Override
    protected int getCartridgeSize() {
        return ConfigHandler.minigun.armorPiercingAmmoCartridgeSize;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x0000FFFF;
    }

    @Override
    public float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return ConfigHandler.minigun.apAmmoDamageMultiplier;
    }

    @Override
    protected DamageSource getDamageSource(Minigun minigun) {
        return minigun.dispenserWeightedPercentage(ConfigHandler.minigun.apAmmoIgnoreArmorChance) ?
                new DamageSourceArmorPiercing(minigun.getPlayer()) :
                super.getDamageSource(minigun);
    }
}
