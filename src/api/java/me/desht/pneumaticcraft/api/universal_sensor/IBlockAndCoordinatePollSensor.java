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

import java.util.Set;

public interface IBlockAndCoordinatePollSensor extends IBaseSensor {
    /**
     * Similar to {@link IPollSensorSetting#getRedstoneValue(Level, BlockPos, int, String)}, but this has the GPS tracked
     * coordinates as extra parameters. This method will only invoke with a valid GPS tool, and when all the coordinates
     * are within range.
     *
     * @param world the sensor's world
     * @param pos the sensor's position
     * @param sensorRange the sensor's current range, based on installed range upgrades
     * @param textBoxText text in the sensor GUI's textbox (may be an empty string)
     * @param positions   When only one GPS Tool is inserted this contains the position of just that tool. If two GPS Tools are inserted, These are both corners of a box, and every coordinate in this box is added to the positions argument.
     * @return the redstone signal level to be emitted
     */
    int getRedstoneValue(Level world, BlockPos pos, int sensorRange, String textBoxText, Set<BlockPos> positions);

    /**
     * See {@link IPollSensorSetting#getPollFrequency(BlockEntity)}
     *
     * @return a poll frequency, in ticks
     */
    int getPollFrequency();
}
