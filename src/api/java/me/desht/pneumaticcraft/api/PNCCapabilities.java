package me.desht.pneumaticcraft.api;

import me.desht.pneumaticcraft.api.hacking.IHacking;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Public PneumaticCraft capability objects.
 */
public class PNCCapabilities {
    /**
     * Basic air handler; use this capability on entities which can be pressurized (drones by default)
     */
    @CapabilityInject(IAirHandler.class)
    public static final Capability<IAirHandler> AIR_HANDLER_CAPABILITY = null;

    /**
     * Machine air handler; use this on tile entities which can store air.
     */
    @CapabilityInject(IAirHandlerMachine.class)
    public static final Capability<IAirHandlerMachine> AIR_HANDLER_MACHINE_CAPABILITY = null;

    /**
     * Item air handler; use this on items which can be pressurized.
     */
    @CapabilityInject(IAirHandlerItem.class)
    public static final Capability<IAirHandlerItem> AIR_HANDLER_ITEM_CAPABILITY = null;

    /**
     * Hacking handler; use this on entities which can be hacked by the Pneumatic Helmet.
     */
    @CapabilityInject(IHacking.class)
    public static final Capability<IHacking> HACKING_CAPABILITY = null;

    /**
     * Heat exchanger capability
     */
    @CapabilityInject(IHeatExchangerLogic.class)
    public static final Capability<IHeatExchangerLogic> HEAT_EXCHANGER_CAPABILITY = null;
}
