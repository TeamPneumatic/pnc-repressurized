package me.desht.pneumaticcraft.api.upgrade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an upgrade which can be inserted into a PNC machine or item. Upgrades are not Forge registry objects, but
 * have a 1:1 relationship to items; a {@code PNCUpgrade} object should <em>only</em> be created during item registration.
 * <p>
 * Upgrades can have multiple tiers; each upgrade <em>must</em> have one corresponding item registered per upgrade tier.
 * Such items <em>must</em> implement {@link IUpgradeItem}.
 */
public interface PNCUpgrade {
    /**
     * Get the upgrade's unique ID
     * @return the upgrade ID
     */
    ResourceLocation getId();

    /**
     * A numeric id for the upgrade which is not guaranteed to be persistent across game restarts. Used internally
     * for performance; <strong>do not depend on the value of this</strong>.
     *
     * @return a numeric ID, for internal use
     */
    @ApiStatus.Internal
    public int getCacheId();

    /**
     * Get the max upgrade tier allowable for this upgrade.
     *
     * @return the max tier
     */
    int getMaxTier();

    /**
     * Check if this upgrade's dependent mods are loaded. Used to control whether the upgrade is added to the creative
     * item list (and thus JEI), and whether any upgrade info is shown for it in GUI side tabs. Note that upgrades
     * are always registered in Forge registries, even if dependent mods are missing.
     *
     * @return true if this upgrade's dependencies are satisfied, false otherwise
     */
    boolean isDependencyLoaded();

    /**
     * Get the corresponding item registry name for this upgrade and tier. The item ID is determined by appending "_upgrade"
     * to the upgrade's ID, followed by "_{tier-number}" if this upgrade has more than one tier.
     *
     * @param tier the upgrade tier
     * @return an item registry name
     * @throws IllegalArgumentException if the tier is less than 1 or greater than the upgrade's max tier
     */
    ResourceLocation getItemRegistryName(int tier);

    /**
     * Get the corresponding item registry name for this upgrade, assuming tier 1.
     *
     * @return an item registry name
     */
    default ResourceLocation getItemRegistryName() {
        return getItemRegistryName(1);
    }

    /**
     * Get the corresponding item for this upgrade and tier. The item ID is determined by appending "_upgrade" to th
     * upgrade's ID, followed by "_{tier-number}" if this upgrade has more than one tier.
     *
     * @param tier the upgrade tier
     * @return a Minecraft item, or air if no corresponding item can be found
     * @throws IllegalArgumentException if the tier is less than 1 or greater than the upgrade's max tier
     */
    Item getItem(int tier);

    /**
     * Get the corresponding item for this upgrade, assuming tier 1
     * @return a Minecraft item
     */
    default Item getItem() {
        return getItem(1);
    }

    /**
     * Get an itemstack for the given upgrade
     *
     * @return an upgrade itemstack, with a single item
     * @throws NullPointerException if called before the upgrade is registered
     */
    default ItemStack getItemStack() {
        return getItemStack(1);
    }

    /**
     * Get an itemstack for the given upgrade
     *
     * @param count number of items in the stack
     * @return an upgrade itemstack
     * @throws NullPointerException if called before the upgrade is registered
     */
    ItemStack getItemStack(int count);

    /**
     * Convenience method to get an upgrade from its corresponding item
     * @param stack the item
     * @return the upgrade, or null if the item isn't an upgrade
     */
    static PNCUpgrade from(ItemStack stack) {
        return stack.getItem() instanceof IUpgradeItem u ? u.getUpgradeType() : null;
    }
}
