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
    @SuppressWarnings("FieldMayBeFinal")
    @CapabilityInject(IAirHandler.class)
    public static Capability<IAirHandler> AIR_HANDLER_CAPABILITY = null;

    /**
     * Machine air handler; use this on tile entities which can store air.
     */
    @SuppressWarnings("FieldMayBeFinal")
    @CapabilityInject(IAirHandlerMachine.class)
    public static Capability<IAirHandlerMachine> AIR_HANDLER_MACHINE_CAPABILITY = null;

    /**
     * Item air handler; use this on items which can be pressurized.
     */
    @SuppressWarnings("FieldMayBeFinal")
    @CapabilityInject(IAirHandlerItem.class)
    public static Capability<IAirHandlerItem> AIR_HANDLER_ITEM_CAPABILITY = null;

    /**
     * Hacking handler; use this on entities which can be hacked by the Pneumatic Helmet.
     */
    @SuppressWarnings("FieldMayBeFinal")
    @CapabilityInject(IHacking.class)
    public static Capability<IHacking> HACKING_CAPABILITY = null;

    /**
     * Heat exchanger capability
     */
    @SuppressWarnings("FieldMayBeFinal")
    @CapabilityInject(IHeatExchangerLogic.class)
    public static Capability<IHeatExchangerLogic> HEAT_EXCHANGER_CAPABILITY = null;
}
