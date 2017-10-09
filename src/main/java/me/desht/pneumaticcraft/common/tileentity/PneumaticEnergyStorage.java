package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.EnergyStorage;

public class PneumaticEnergyStorage extends EnergyStorage {
    public PneumaticEnergyStorage(int capacity) {
        super(capacity);
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("Energy", energy);
    }

    public void readFromNBT(NBTTagCompound tag) {
        energy = tag.getInteger("Energy");
    }
}
