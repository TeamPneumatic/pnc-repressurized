package me.desht.pneumaticcraft.common.pressure;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerSupplier;
import me.desht.pneumaticcraft.lib.PneumaticValues;

public class AirHandlerSupplier implements IAirHandlerSupplier {
    private static final AirHandlerSupplier INSTANCE = new AirHandlerSupplier();

    public static AirHandlerSupplier getInstance() {
        return INSTANCE;
    }

    @Override
    public IAirHandler createTierOneAirHandler(int volume) {
        return createAirHandler(PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE, volume);
    }

    @Override
    public IAirHandler createTierTwoAirHandler(int volume) {
        return createAirHandler(PneumaticValues.DANGER_PRESSURE_TIER_TWO, PneumaticValues.MAX_PRESSURE_TIER_TWO, volume);
    }

    @Override
    public IAirHandler createAirHandler(float dangerPressure, float criticalPressure, int volume) {
        return new AirHandler(dangerPressure, criticalPressure, volume);
    }
}
