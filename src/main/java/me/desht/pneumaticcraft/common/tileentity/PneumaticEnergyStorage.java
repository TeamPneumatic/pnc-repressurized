package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.energy.EnergyStorage;

public class PneumaticEnergyStorage extends EnergyStorage {
    public PneumaticEnergyStorage(int capacity) {
        super(capacity);
    }

    public void writeToNBT(CompoundNBT tag) {
        tag.putInt("Energy", energy);
    }

    public void readFromNBT(CompoundNBT tag) {
        energy = tag.getInt("Energy");
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
