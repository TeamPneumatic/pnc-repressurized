package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import mekanism.api.radiation.capability.IRadiationShielding;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public record RadiationShieldingProvider(ItemStack stack, ArmorItem.Type type) implements IRadiationShielding {
    @Override
    public double getRadiationShielding() {
        boolean upgrade = UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.RADIATION_SHIELDING.get()) > 0;
        if (!upgrade) return 0d;
        return switch (type) {
            case HELMET -> 0.25;
            case CHESTPLATE -> 0.4;
            case LEGGINGS -> 0.2;
            case BOOTS -> 0.15;
            case BODY -> 0.0;  // there is not a body piece for pneumatic armor
        };
    }
}
