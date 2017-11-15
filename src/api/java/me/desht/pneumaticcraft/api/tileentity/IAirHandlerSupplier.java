package me.desht.pneumaticcraft.api.tileentity;

public interface IAirHandlerSupplier {
    IAirHandler createTierOneAirHandler(int volume);

    IAirHandler createTierTwoAirHandler(int volume);

    /**
     * Returns a new instance of an IAirHandler. This handler handles everything pressurized air related: Air dispersion,
     * blowing up when the pressure gets too high, providing a method for releasing air into the atmosphere...
     * PROVIDED THAT THE FOLLOWING METHODS ARE FORWARDED TO THIS INSTANCE:
     * {@link net.minecraft.tileentity.TileEntity#update()},
     * {@link net.minecraft.tileentity.TileEntity#writeToNBT(net.minecraft.nbt.NBTTagCompound)}
     * {@link net.minecraft.tileentity.TileEntity#readFromNBT(net.minecraft.nbt.NBTTagCompound)}
     * {@link net.minecraft.tileentity.TileEntity#validate()}
     *
     * @param dangerPressure   minimal pressure on which this machine can explode (the yellow to red transition)
     * @param criticalPressure the absolute maximum pressure the machine can take 7 bar in tier 1 machines.
     * @param maxFlow          maximum mL/tick that this machine can disperse. Tier one machines do 50mL/tick while Tier two have 200mL/tick.
     * @param volume           Volume of the machine's internal storage. These vary from 1000mL for small machines to 10,000mL for the big ones.
     *                         The higher the volume the slower the machine will charge/discharge.
     * @return
     */
    IAirHandler createAirHandler(float dangerPressure, float criticalPressure, int volume);
}
