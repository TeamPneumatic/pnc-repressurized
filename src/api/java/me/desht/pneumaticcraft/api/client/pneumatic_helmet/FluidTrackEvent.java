package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

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

    public IFluidHandler getFluidHandler(Direction face) {
        return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face).map(iFluidHandler -> iFluidHandler).orElse(null);
    }
}
