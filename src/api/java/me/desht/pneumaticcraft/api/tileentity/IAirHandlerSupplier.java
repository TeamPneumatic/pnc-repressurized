package me.desht.pneumaticcraft.api.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;

/**
 * Get an instance of this with {@link PneumaticRegistry.IPneumaticCraftInterface#getAirHandlerSupplier()}.
 */
public interface IAirHandlerSupplier {
    IAirHandler createTierOneAirHandler(int volume);

    IAirHandler createTierTwoAirHandler(int volume);

    /**
     * Returns a new instance of an IAirHandler. This handler handles everything pressurized air related: Air dispersion,
     * blowing up when the pressure gets too high, providing a method for releasing air into the atmosphere...
     * <strong>provided that the following methods are forwarded to the IAirHandler object:</strong>
     * <ul>
     * <li>{@link net.minecraft.util.ITickable#update()}</li>
     * <li>{@link net.minecraft.tileentity.TileEntity#writeToNBT(net.minecraft.nbt.NBTTagCompound)}</li>
     * <li>{@link net.minecraft.tileentity.TileEntity#readFromNBT(net.minecraft.nbt.NBTTagCompound)}</li>
     * <li>{@link net.minecraft.tileentity.TileEntity#validate()}</li>
     * </ul>
     *
     * @param dangerPressure   minimum pressure at which this machine can explode (the yellow to red transition)
     * @param criticalPressure the absolute maximum pressure the machine can take; 7 bar in tier 1 machines, 25 bar in tier 2 machines
     * @param volume           volume of the machine's internal storage; the pressure (in bar) is the actual amount of air in the machine divided by its volume
     * @return the air handler object
     */
    IAirHandler createAirHandler(float dangerPressure, float criticalPressure, int volume);
}
