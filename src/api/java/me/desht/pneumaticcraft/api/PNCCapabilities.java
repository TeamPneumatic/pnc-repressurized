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

package me.desht.pneumaticcraft.api;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHacking;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Public PneumaticCraft capability objects.
 */
public class PNCCapabilities {
    /**
     * Basic air handler; use this capability on entities which can be pressurized (drones by default)
     */
    public static final Capability<IAirHandler> AIR_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * Machine air handler; use this on tile entities which can store air.
     */
    public static final Capability<IAirHandlerMachine> AIR_HANDLER_MACHINE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * Item air handler; use this on items which can be pressurized.
     */
    public static final Capability<IAirHandlerItem> AIR_HANDLER_ITEM_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * Hacking handler; use this on entities which can be hacked by the Pneumatic Helmet.
     */
    public static final Capability<IHacking> HACKING_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * Heat exchanger capability
     */
    public static final Capability<IHeatExchangerLogic> HEAT_EXCHANGER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
}
