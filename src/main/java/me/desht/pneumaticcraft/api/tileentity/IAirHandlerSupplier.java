package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.nbt.CompoundNBT;

/**
 * Get an instance of this with {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getAirHandlerSupplier()}.
 */
public interface IAirHandlerSupplier {
    IAirHandlerMachine createTierOneAirHandler(int volume);

    IAirHandlerMachine createTierTwoAirHandler(int volume);

    /**
     * Returns a new instance of an IAirHandler. This handler handles everything pressurized air related: Air dispersion,
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
