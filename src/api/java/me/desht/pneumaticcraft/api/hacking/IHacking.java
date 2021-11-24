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

package me.desht.pneumaticcraft.api.hacking;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

/**
 * Manages the list of "hacks" on an entity. Hacks are added via the Pneumatic Helmet
 * hacking feature.  This interface is exposed via an entity capability; retrieve a
 * <code>Capability&lt;IHacking&gt;</code> via
 * {@link net.minecraftforge.common.capabilities.CapabilityInject capability injection}.
 */
public interface IHacking extends INBTSerializable<CompoundNBT> {
    /**
     * Called every tick on every entity which has been hacked (i.e. which has a non-empty list of hacks)
     *
     * @param entity the hacked entity
     */
    void update(Entity entity);

    /**
     * Add a new hack to the entity's list of hacks.
     * @param hackable a hack
     */
    void addHackable(IHackableEntity hackable);

    /**
     * Get a list of the hacks currently on the entity.
     *
     * @return a list of hacks
     */
    List<IHackableEntity> getCurrentHacks();
}
