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
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

/**
 * Represents the entity types contained in a Spawner Core. Retrieve an instance of this with
 * {@link IItemRegistry#getSpawnerCoreStats(ItemStack)}.
 *
 * @implNote implementations of this can be used as item data components, and as such should be treated
 * as immutable objects.
 */
@ApiStatus.NonExtendable
public interface ISpawnerCoreStats {
    /**
     * Get an unmodifiable set of the entity types stored in this spawner core.
     *
     * @return a map of entity type to percentage occupied
     */
    Map<EntityType<?>,Integer> getEntities();

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
     * Update the percentage occupation for the given entity type. The update level will be clamped so that does not go
     * below zero, or leaves the total occupation of the core greater than 100%.
     *
     * @param type an entity type
     * @param toAdd the amount to adjust by, may be negative
     * @return a new stats object if a modification was made, otherwise this object
     */
    ISpawnerCoreStats addAmount(EntityType<?> type, int toAdd);

    /**
     * Serialize this object onto a ItemStack via data component.
     *
     * @param stack the stack to save onto
     */
    void save(ItemStack stack);

    /**
     * Pick a weighted random entity from the core.
     *
     * @param includeUnused if true, the unused percentage will be taken into account, leading to a possible null
     *                      return value
     * @return an entity type, or null if no entity type was selected
     */
    EntityType<?> pickEntity(boolean includeUnused);

    /**
     * {@return an empty spawner core stats object}
     */
    ISpawnerCoreStats empty();
}
