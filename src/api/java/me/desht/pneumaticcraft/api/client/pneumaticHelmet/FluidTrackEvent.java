package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when a helmet Block Tracker is about to track a fluid tank. Can be canceled to prevent tracking.
 * Posted on MinecraftForge.EVENT_BUS
 *
 * The tile inventory is known to support CapabilityFluidHandler.FLUID_CAPABILITY on at least one face
 * when the event is received.
 *
 * @author MineMaarten
 */
@Cancelable
public class FluidTrackEvent extends Event {
    private final TileEntity te;

    public FluidTrackEvent(TileEntity te) {
        this.te = te;
    }

    public IFluidHandler getFluidHandler() {
        return getFluidHandler(null);
    }

    public IFluidHandler getFluidHandler(EnumFacing face) {
        return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face);
    }
}
