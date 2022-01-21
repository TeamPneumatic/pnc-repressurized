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

package me.desht.pneumaticcraft.api.actuator;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * What was this interface ever for? Some planned functionality lost to the mists of time?
 */
@Deprecated
public interface IActuator {
    /**
     * Same as {@link me.desht.pneumaticcraft.api.universal_sensor.ISensorSetting#getSensorPath()}
     *
     * @return a path
     */
    String getSensorPath();

    /**
     * When returned true, the GUI will enable the textbox writing, otherwise not.
     *
     * @return true if a text box is needed
     */
    boolean needsTextBox();

    /**
     * Should return the description of this sensor displayed in the GUI stat. Information should at least include
     * when this sensor emits redstone and how (analog (1 through 15), or digital).
     *
     * @return a sensor description
     */
    List<String> getDescription();

    /**
     * @param universalActuator an actuator tile entity
     */
    void actuate(BlockEntity universalActuator);
}
