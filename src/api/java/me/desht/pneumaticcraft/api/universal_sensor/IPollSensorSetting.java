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

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPollSensorSetting extends ISensorSetting {
    /**
     * The value returned here is the interval between every check in ticks (the interval of calling getRedstoneValue()).
     * Consider increasing the interval when your sensor method is resource intensive.
     *
     * @param te universal sensor
     * @return the interval in ticks between polling operations
     */
    int getPollFrequency(TileEntity te);

    /**
     * This method should return the outputted redstone value 0-15 of this sensor. When this sensor is
     * digital, just return 0 or 15.
     *
     * @param world the world
     * @param pos the blockpos to test
     * @param sensorRange range of the sensor, based on the number of Range Upgrades inserted in the Universal Sensor.
     * @param textBoxText any text typed in the textfield of the Universal Sensor.
     * @return level of the redstone signal to emit
     */
    int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText);

}
