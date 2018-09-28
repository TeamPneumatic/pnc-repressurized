package me.desht.pneumaticcraft.api.universalSensor;

import me.desht.pneumaticcraft.api.PneumaticRegistry;

/**
 * Get an instance of this with {@link PneumaticRegistry.IPneumaticCraftInterface#getSensorRegistry()}.
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
