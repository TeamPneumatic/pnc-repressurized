/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Represents the entity types contained in a Spawner Core. Retrieve an instance of this with
 * {@link IItemRegistry#getSpawnerCoreStats(ItemStack)}.
 */
public interface ISpawnerCoreStats {
    /**
     * Get the entity types stored in this spawner core
     *
     * @return a set of entity types
     */
    Set<EntityType<?>> getEntities();

    /**
     * Get the percentage of the core that the given entity type occupies
     *
     * @param entityType an entity type
     * @return a percentage level
     */
    int getPercentage(EntityType<?> entityType);

    /**
     * Get the unoccupied percentage of the core
     *
     * @return a percentage level
     */
    int getUnusedPercentage();

    /**
     * Update the percentage level for the given entity type. The update level will be clamped so that does not go
     * below zero, or leaves the total occupation of the core greater than 100%.
     * <p>
     * The updated level is not automatically serialized to an ItemStack; see {@link #serialize(ItemStack)} for that.
     *
     * @param type an entity type
     * @param toAdd the amount to adjust by, may be negative
     * @return true if any change was made, false otherwise
     */
    boolean addAmount(EntityType<?> type, int toAdd);

    /**
     * Pick a weighted random entity from the core.
     *
     * @param includeUnused if true, the unused percentage will be taken into account, leading to a possible null
     *                      return value
     * @return an entity type, or null if no entity type was selected
     */
    EntityType<?> pickEntity(boolean includeUnused);

    /**
     * Serialize the current stats onto the given ItemStack, which must be a Spawner Core
     *
     * @param stack an ItemStack
     * @throws IllegalArgumentException if the ItemStack is not a Spawner Core
     */
    void serialize(ItemStack stack);
}
