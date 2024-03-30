/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block.entity;

import net.minecraft.core.Direction;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a block entity whose sides can be reconfigured, i.e. connected to different capability handlers,
 * typically controlled by GUI side tabs.  ISideConfigurable block entity GUI's will automatically get a side tab for
 * each side configurator they have.  Each side configurator handles one capability (items, fluids, energy...)
 * <p>
 * Any BE which supports side configuration must also be rotatable, since configurable sides are relative to the
 * block's facing direction.
 */
public interface ISideConfigurable {
    /**
     * Get a collection of all the side configurators this BE has
     *
     * @return the BE's side configurators
     */
    List<SideConfigurator<?>> getSideConfigurators();

    /**
     * Check if the given handler is OK for the given face
     *
     * @param face relative face of the block
     * @param handler the capability to check, may be null to indicate no connectivity on this face
     * @return true if this handler is allowed on this side, false otherwise
     */
    default boolean isValid(SideConfigurator.RelativeFace face, @Nullable Object handler) {
        return true;
    }

    /**
     * Return the (absolute) direction that this BE is facing.  Required to determine how to map absolute to relative
     * faces of the block.
     *
     * @return the BE facing direction
     */
    Direction byIndex();
}
