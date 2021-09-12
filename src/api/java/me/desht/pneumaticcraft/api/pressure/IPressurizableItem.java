package me.desht.pneumaticcraft.api.pressure;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.item.ItemStack;

/**
 * Convenience interface; if your item implements this, you can take advantage of PneumaticCraft's built-in item
 * air handling functionality, while providing the
 * {@link me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem} capability to access the item's air information.
 * <p>
 * You don't <em>have to</em> implement this interface in your air-handling items, but it will make things easier;
 * the alternative is to provide a custom implementation of {@link me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem}
 * for your capability, which is more work.
 * <p>
 * @see me.desht.pneumaticcraft.api.item.IItemRegistry#makeItemAirHandlerProvider(ItemStack, float)
 */
public interface IPressurizableItem {
    /**
     * Get the base item volume before any volume upgrades are added.
     *
     * @return the base volume
     */
    int getBaseVolume();

    /**
     * Get the number of volume upgrades currently in this ItemStack.
     *
     * @param stack the ItemStack to check
     * @return the number of installed volume upgrades
     */
    int getVolumeUpgrades(ItemStack stack);

    /**
     * Get the amount of air currently held in this ItemStack.
     *
     * @param stack the ItemStack to check
     * @return the amount of air, in mL
     */
    int getAir(ItemStack stack);

    /**
     * The effective volume is the item's base volume, modified by both the volume upgrades installed in the item,
     * and any extra registered modifiers (e.g. CoFH Holding enchantment).
     *
     * @param stack the ItemStack to check
     * @return the effective, final, volume to use
     */
    default int getEffectiveVolume(ItemStack stack) {
        int upgradedVolume = PressureHelper.getUpgradedVolume(getBaseVolume(), getVolumeUpgrades(stack));
        return PneumaticRegistry.getInstance().getItemRegistry().getModifiedVolume(stack, upgradedVolume);
    }

    /**
     * Get the pressure for this item. This is simply the air in the item, divided by the effective volume of the item.
     *
     * @param stack the ItemStack to check
     * @return the item's current pressure, in bar
     */
    default float getPressure(ItemStack stack) {
        return (float) getAir(stack) / getEffectiveVolume(stack);
    }
}
