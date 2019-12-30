package me.desht.pneumaticcraft.api;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class PNCCapabilities {
    @CapabilityInject(IAirHandler.class)
    public static Capability<IAirHandler> AIR_HANDLER_CAPABILITY = null;

    @CapabilityInject(IAirHandlerMachine.class)
    public static Capability<IAirHandlerMachine> AIR_HANDLER_MACHINE_CAPABILITY = null;

    @CapabilityInject(IAirHandlerItem.class)
    public static Capability<IAirHandlerItem> AIR_HANDLER_ITEM_CAPABILITY = null;
}
