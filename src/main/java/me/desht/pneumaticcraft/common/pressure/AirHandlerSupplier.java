package me.desht.pneumaticcraft.common.pressure;

import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerSupplier;
import me.desht.pneumaticcraft.lib.PneumaticValues;

public class AirHandlerSupplier implements IAirHandlerSupplier {
    private static final AirHandlerSupplier INSTANCE = new AirHandlerSupplier();

    public static AirHandlerSupplier getInstance() {
        return INSTANCE;
    }

    @Override
    public IAirHandlerMachine createTierOneAirHandler(int volume) {
        return createAirHandler(PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE, volume);
    }

    @Override
    public IAirHandlerMachine createTierTwoAirHandler(int volume) {
        return createAirHandler(PneumaticValues.DANGER_PRESSURE_TIER_TWO, PneumaticValues.MAX_PRESSURE_TIER_TWO, volume);
    }

    @Override
    public IAirHandlerMachine createAirHandler(float dangerPressure, float criticalPressure, int volume) {
        return new AirHandlerMachine(dangerPressure, criticalPressure, volume);
    }
}
