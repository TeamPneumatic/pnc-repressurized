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

/**
 * Get an instance of this with {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getSensorRegistry()}.
 */
public interface ISensorRegistry {
    /**
     * Registry for IPollSensorSetting, EntityPollSensor and IEventSensorSetting, and any other instance of ISensorSetting.
     *
     * @param sensor
     */
    void registerSensor(ISensorSetting sensor);

    /**
     * Registry for IBlockAndCoordinateEventSensor
     *
     * @param sensor
     */
    void registerSensor(IBlockAndCoordinateEventSensor sensor);

    /**
     * Registry for IBlockAndCoordinatePollSensor
     *
     * @param sensor
     */
    void registerSensor(IBlockAndCoordinatePollSensor sensor);
}
