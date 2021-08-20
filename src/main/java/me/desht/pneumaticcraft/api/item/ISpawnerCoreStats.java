package me.desht.pneumaticcraft.api.item;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;

import java.util.Collection;

/**
 * Represents the entity types contained in a Spawner Core. Retrieve an instance of this with
 * {@link IItemRegistry#getSpawnerCoreStats(ItemStack)}.
 */
public interface ISpawnerCoreStats {
    /**
     * Get a collection of the entities stored in this spawner core
     * @return a collection of entity types
     */
    Collection<EntityType<?>> getEntities();

    /**
     * Get the percentage of the core that the given entity type occupies
     * @param entityType an entity type
     * @return a percentage level
     */
    int getPercentage(EntityType<?> entityType);

    /**
     * Get the unoccupied percentage of the core
     * @return a percentage level
     */
    int getUnusedPercentage();

    /**
     * Update the percentage level for the given entity type. The update level will be clamped so that does not go
     * below zero, or leaves the total occupation of the core greater than 100%.
     * <p>
     * The updated level is not automatically serialized to any item; see {@link #serialize(ItemStack)} for that.
     *
     * @param type an entity type
     * @param toAdd the amount to adjust by, may be negative
     * @return true if any change was made, false otherwise
     */
    boolean addAmount(EntityType<?> type, int toAdd);

    /**
     * Pick a weighted random entity from the core.
     *
     * @param includeUnused if true, the unused percentage will be taken into account, leading a possible null return value
     * @return an entity type, or null if no entity type was selected
     */
    EntityType<?> pickEntity(boolean includeUnused);

    /**
     * Serialize the current stats onto the given ItemStack, which must be a spawner core
     *
     * @param stack an ItemStack
     * @throws IllegalArgumentException if the ItemStack is not a spawner core
     */
    void serialize(ItemStack stack);
}
