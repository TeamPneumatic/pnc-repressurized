package pneumaticCraft.common.thirdparty.cofh;

import cofh.api.energy.EnergyStorage;

public interface IRFConverter{
    public int getRFRate();

    public int getAirRate();

    public EnergyStorage getEnergyStorage();
}
