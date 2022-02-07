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

package me.desht.pneumaticcraft.common.pressure;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachineFactory;
import me.desht.pneumaticcraft.common.capabilities.MachineAirHandler;

public class AirHandlerMachineFactory implements IAirHandlerMachineFactory {
    private static final AirHandlerMachineFactory INSTANCE = new AirHandlerMachineFactory();

    public static AirHandlerMachineFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public IAirHandlerMachine createTierOneAirHandler(int volume) {
        return createAirHandler(PressureTier.TIER_ONE, volume);
    }

    @Override
    public IAirHandlerMachine createTierTwoAirHandler(int volume) {
        return createAirHandler(PressureTier.TIER_TWO, volume);
    }

    @Override
    public IAirHandlerMachine createAirHandler(PressureTier tier, int volume) {
        return new MachineAirHandler(tier, volume);
    }
}
