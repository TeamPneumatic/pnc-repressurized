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

package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * Implement this class and register it with {@link IPneumaticHelmetRegistry#registerEntityTrackEntry(java.util.function.Supplier)}.
 * Your implementation must provide a no-parameter constructor.
 * <p>
 * An instance of this tracker is created for each entity for which the tracker is applicable.
 */
public interface IEntityTrackEntry {
    /**
     * Return true if you want to add a tooltip for the given entity.
     *
     * @param entity the candidate entity
     * @return true if this tracker is applicable to the given entity
     */
    boolean isApplicable(Entity entity);

    /**
     * Add info to the tab. This is only called when isApplicable returned true.
     *
     * @param entity the tracked entity
     * @param curInfo list of text component to append information to
     * @param isLookingAtTarget true if the player is focused on the tracked entity
     */
    void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget);

    /**
     * Called every client tick; can be used to update something like a timer (e.g. used for the Creeper
     * explosion countdown).
     *
     * @param entity the tracked entity
     */
    default void tick(Entity entity) { }

    /**
     * Called every render tick, this method can be used to render additional info. Used for Drone AI visualisation,
     * for example.
     *
     * @param entity the tracked entity
     * @param partialTicks partial ticks since last full ticks
     */
    default void render(PoseStack matrixStack, MultiBufferSource buffer, Entity entity, float partialTicks) { }
}
