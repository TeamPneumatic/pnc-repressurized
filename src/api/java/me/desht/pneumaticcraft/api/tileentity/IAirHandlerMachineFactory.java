package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.nbt.CompoundNBT;

/**
 * Use this interface to get instances of air handlers for your tile entities.  You can then expose those air handler
 * instances via the {@link IAirHandlerMachine} capability interface; {@link me.desht.pneumaticcraft.api.PNCCapabilities#AIR_HANDLER_MACHINE_CAPABILITY} can be used for this.
 * <p>
 * Get an instance of this factory with
 * {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getAirHandlerMachineFactory()}.
 */
public interface IAirHandlerMachineFactory {
    /**
     * Create a standard tier one air handler.
     *
     * @param volume the air handler volume, in mL.
     * @return a new tier one air handler
     */
    IAirHandlerMachine createTierOneAirHandler(int volume);

    /**
     * Create a standard tier two air handler.
     *
     * @param volume the air handler volume, in mL.
     * @return a new tier two air handler
     */
    IAirHandlerMachine createTierTwoAirHandler(int volume);

    /**
     * Returns a new instance of an IAirHandler. This handler handles everything pressurized air related: air dispersion,
     * blowing up when the pressure gets too high, providing a method for releasing air into the atmosphere...
     * <strong>provided that the following methods are forwarded to the IAirHandler object:</strong>
     * <ul>
     * <li>{@link net.minecraft.tileentity.ITickableTileEntity#tick()}</li>
     * <li>{@link net.minecraft.tileentity.TileEntity#write(CompoundNBT)}</li>
     * <li>{@link net.minecraft.tileentity.TileEntity#read(CompoundNBT)}</li>
     * <li>{@link net.minecraft.tileentity.TileEntity#validate()}</li>
     * </ul>
     *
     * @param dangerPressure   minimum pressure at which this machine can explode (the yellow to red transition)
     * @param criticalPressure the absolute maximum pressure the machine can take; 7 bar in tier 1 machines, 25 bar in tier 2 machines
     * @param volume           volume of the machine's internal storage; the pressure (in bar) is the actual amount of air in the machine divided by its volume
     * @return the air handler object
     */
    IAirHandlerMachine createAirHandler(float dangerPressure, float criticalPressure, int volume);
}
