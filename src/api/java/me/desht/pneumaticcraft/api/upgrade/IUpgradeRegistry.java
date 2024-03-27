package me.desht.pneumaticcraft.api.upgrade;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The upgrade registry can be used to register custom upgrades to be accepted by block entities, entities and items.
 * Get an instance of it via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getUpgradeRegistry()}.
 * <p>
 * The {@code addApplicableUpgrades()} methods should be called from your
 * {@link net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent} handler.
 */
public interface IUpgradeRegistry {
    /**
     * Register the given upgrade builder with the given block entity type. Note that the upgrades in the given builder
     * will overwrite and/or augment any upgrades already registered for the block entity.
     * @param type the block entity type
     * @param builder the builder helper object
     */
    void addApplicableUpgrades(BlockEntityType<?> type, Builder builder);

    /**
     * Register the given upgrade builder with the given entity type. Note that the upgrades in the given builder
     * will overwrite and/or augment any upgrades already registered for the entity.
     * @param type the entity type
     * @param builder the builder helper object
     */
    void addApplicableUpgrades(EntityType<?> type, Builder builder);

    /**
     * Register the given upgrade builder with the given item. Note that the upgrades in the given builder
     * will overwrite and/or augment any upgrades already registered for the item.
     * @param item the item
     * @param builder the builder helper object
     */
    void addApplicableUpgrades(Item item, Builder builder);

    /**
     * Get the maximum number of upgrades of the given type accepted by the given block entity
     * @param te the block entity
     * @param upgrade the upgrade to check
     * @return maximum number of that upgrade which can be installed
     */
    int getMaxUpgrades(BlockEntity te, PNCUpgrade upgrade);

    /**
     * Get the maximum number of upgrades of the given type accepted by the given entity
     * @param entity the entity
     * @param upgrade the upgrade to check
     * @return maximum number of that upgrade which can be installed
     */
    int getMaxUpgrades(Entity entity, PNCUpgrade upgrade);

    /**
     * Get the maximum number of upgrades of the given type accepted by the given block entity
     * @param item the item
     * @param upgrade the upgrade to check
     * @return maximum number of that upgrade which can be installed
     */
    int getMaxUpgrades(Item item, PNCUpgrade upgrade);

    /**
     * Convenience method which adds a list of the items which accept the given upgrade to the upgrade item's tooltip.
     * This list is intended to be displayed while Shift is held down while hovering over the upgrade item, and will
     * scroll if larger than 12 lines.
     * <p>
     * This is automatically used by custom upgrades created via {@link #makeUpgradeItem(PNCUpgrade, int)}. You can also
     * call this yourself <strong>on the client only</strong> for custom upgrades that you create (i.e. items which
     * implement {@link IUpgradeItem}).
     *
     * @param upgrade the upgrade
     * @param infoList the tooltip to append to
     */
    void addUpgradeTooltip(PNCUpgrade upgrade, List<Component> infoList);

    /**
     * Register an upgrade. Note: this is a not a Forge or Minecraft registry object. It's OK to register upgrades
     * during item registration, i.e. when you register your upgrade item(s).
     *
     * @param id the unique upgrade ID
     * @param maxTier the maximum tier of this upgrade
     * @param depModIds zero or more mod ID which must be present for this upgrade to be relevant
     * @return an upgrade object
     * @throws IllegalStateException if this upgrade ID has already been registered
     */
    PNCUpgrade registerUpgrade(ResourceLocation id, int maxTier, String... depModIds);

    /**
     * Register an upgrade with just one tier.
     * @param id the unique upgrade ID
     * @return an upgrade object
     * @throws IllegalStateException if this upgrade ID has already been registered
     */
    default PNCUpgrade registerUpgrade(ResourceLocation id) {
        return registerUpgrade(id, 1);
    }

    /**
     * Retrieve an upgrade by its ID
     * @param upgradeId the upgrade ID, as used to register id
     * @return the registered upgrade, or null if the id is not known
     */
    PNCUpgrade getUpgradeById(ResourceLocation upgradeId);

    /**
     * Retrieve an unmodifiable collection of all known registered upgrade objects
     * @return all known upgrades
     */
    Collection<PNCUpgrade> getKnownUpgrades();

    /**
     * Convenience method to create an Item implementing the {@link IUpgradeItem} interface, which can be used as a
     * PneumaticCraft upgrade. This item has the default PneumaticCraft tooltip behaviour in that
     * {@link #addUpgradeTooltip(PNCUpgrade, List)} is called when Shift is held while hovering over the item.
     * <p>
     * You can use this method when registering upgrade items as an alternative to creating an Item which implements
     * {@link IUpgradeItem} yourself.
     * <p>
     * The item created by this method will be in the PneumaticCraft creative tab and have no other special
     * item properties; see {@link #makeUpgradeItem(PNCUpgrade, int, Item.Properties)} if you need custom behaviour here.
     *
     * @param upgrade the upgrade object, as returned by {@link #registerUpgrade(ResourceLocation)}
     * @param tier upgrade tier of this item
     * @return an item, which should be registered in the usual way
     */
    Item makeUpgradeItem(PNCUpgrade upgrade, int tier);

    /**
     * Same as {@link #makeUpgradeItem(PNCUpgrade, int)} but allows a custom item properties object to be supplied for
     * use when the {@code Item} is created.
     *
     * @param upgrade a supplier for the upgrade object, which will not yet be registered
     * @param tier upgrade tier of this item
     * @param properties an item properties object
     * @return an item, which should be registered in the usual way
     */
    Item makeUpgradeItem(PNCUpgrade upgrade, int tier, Item.Properties properties);

    /**
     * Helper method to get the number of the given upgrade which is installed in the given itemstack.
     *
     * @param stack the item holding the upgrades
     * @param upgrade the upgrade to check for
     * @return the number of that upgrade installed in the item
     */
    int getUpgradeCount(ItemStack stack, PNCUpgrade upgrade);

    /**
     * Get all the upgrades install in a given item.
     *
     * @param stack an item
     * @return all the upgrades installed in the item
     * @deprecated use {@link #getUpgradesInItem(ItemStack)}
     */
    @Deprecated(forRemoval = true)
    default Map<PNCUpgrade,Integer> getAllUpgrades(ItemStack stack) {
        return getUpgradesInItem(stack);
    }

    /**
     * Helper method to get all the upgrades currently installed in the given itemstack
     *
     * @param stack the item holding the upgrades
     * @return an immutable map of (upgrade->count)
     */
    Map<PNCUpgrade,Integer> getUpgradesInItem(ItemStack stack);

    /**
     * Helper class to collect a list of upgrades for adding to an object via one of the {@code addApplicableUpgrades()}
     * methods
     */
    class Builder {
        private final Map<PNCUpgrade, Integer> upgrades = new HashMap<>();

        /**
         * Create a new blank builder object
         */
        public Builder() {
        }

        private Builder(Builder copy) {
            upgrades.putAll(copy.upgrades);
        }

        /**
         * Create a new builder object which is a copy of an existing builder
         *
         * @param builder the existing builder
         * @return a new builder object
         */
        public static Builder copyOf(Builder builder) {
            return new Builder(builder);
        }

        /**
         * Convenience method: Create a builder object with the given upgrade and count added as an initial entry
         *
         * @param upgrade the upgrade
         * @param amount the maximum number of that upgrade accepted by the subject
         * @return a builder object
         */
        public static Builder of(PNCUpgrade upgrade, int amount) {
            return new Builder().with(upgrade, amount);
        }

        /**
         * Add another upgrade to an existing builder object.
         *
         * @param upgrade the upgrade
         * @param amount the maximum number of that upgrade accepted by the subject
         * @return a builder object
         */
        public Builder with(PNCUpgrade upgrade, int amount) {
            upgrades.put(upgrade, amount);
            return this;
        }

        /**
         * Get the upgrades already added.
         * @return a map of upgrade -> maximum number accepted
         */
        public Map<PNCUpgrade, Integer> getUpgrades() {
            return ImmutableMap.copyOf(upgrades);
        }
    }
}
