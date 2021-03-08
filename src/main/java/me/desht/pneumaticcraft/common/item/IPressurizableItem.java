package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

/**
 * For internal use only, not part of the API!  Use IAirHandler/IAirHandlerItem capability interface for that
 */
public interface IPressurizableItem {
    /**
     * Get the base item volume before any volume upgrades are added.
     *
     * @return the base volume
     */
    int getBaseVolume();

    default int getAir(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null ? tag.getInt(AirHandlerItemStack.AIR_NBT_KEY) : 0;
    }

    default int getUpgradedVolume(ItemStack stack) {
        int nUpgrades = UpgradableItemUtils.getUpgrades(stack, EnumUpgrade.VOLUME);
        int vol0 = ApplicableUpgradesDB.getInstance().getUpgradedVolume(getBaseVolume(), nUpgrades);
        return ItemRegistry.getInstance().getUpgradedVolume(stack, vol0);
    }

    default float getPressure(ItemStack stack) {
        return (float) getAir(stack) / getUpgradedVolume(stack);
    }
}
