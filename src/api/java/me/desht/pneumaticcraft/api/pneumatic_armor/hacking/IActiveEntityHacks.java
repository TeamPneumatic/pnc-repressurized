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

package me.desht.pneumaticcraft.api.pneumatic_armor.hacking;

import net.minecraft.world.entity.Entity;

import java.util.Collection;

/**
 * Manages the list of "hacks" on an entity. Hacks are added via the Pneumatic Helmet
 * hacking feature. See also {@link me.desht.pneumaticcraft.api.misc.IMiscHelpers#getHackingForEntity(Entity, boolean)}.
 */
public interface IActiveEntityHacks {
    /**
     * Called every tick on every entity which has been hacked (i.e. which has a non-empty list of hacks)
     *
     * @param entity the hacked entity
     */
    void tick(Entity entity);

    /**
     * Add a new hack to the entity's list of hacks.
     * @param hackable a hack
     */
    void addHackable(IHackableEntity<?> hackable);

    /**
     * Get the hacks currently on the entity.
     *
     * @return a list of hacks
     */
    Collection<IHackableEntity<?>> getCurrentHacks();

    /**
     * Clear the entity's list of hacks.
     */
    void clear();
}
