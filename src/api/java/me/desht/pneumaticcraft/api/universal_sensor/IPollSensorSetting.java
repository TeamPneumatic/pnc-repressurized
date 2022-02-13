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

package me.desht.pneumaticcraft.api.universal_sensor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public interface IPollSensorSetting extends ISensorSetting {
    /**
     * The value returned here is the interval between every check in ticks (i.e. how frequently
     * {@link #getRedstoneValue(Level, BlockPos, int, String)} should be called.
     * Consider increasing the interval when that method is resource-intensive.
     *
     * @param te universal sensor
     * @return the interval in ticks between polling operations
     */
    int getPollFrequency(BlockEntity te);

    /**
     * Called regularly by the Universal Sensor block entity to calculate the output redstone value 0-15 of this sensor.
     * When this sensor is digital, just return 0 or 15.
     *
     * @param level the world
     * @param pos the blockpos to test
     * @param sensorRange range of the sensor, based on the number of Range Upgrades inserted in the Universal Sensor.
     * @param textBoxText any text typed in the textfield of the Universal Sensor GUI.
     * @return level of the redstone signal that the Universal Sensor block should emit
     */
    int getRedstoneValue(Level level, BlockPos pos, int sensorRange, String textBoxText);

    /**
     * Called immediately before {@link #getRedstoneValue(Level, BlockPos, int, String)} to set up the player context,
     * if necessary. If this sensor doesn't care about player context, there's no need to override this.
     *
     * @param playerID unique ID for the player who placed down the calling Universal Sensor
     */
    default void setPlayerContext(UUID playerID) {
    }
}
