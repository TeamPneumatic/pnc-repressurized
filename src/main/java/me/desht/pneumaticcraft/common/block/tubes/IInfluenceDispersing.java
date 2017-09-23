package me.desht.pneumaticcraft.common.block.tubes;

public interface IInfluenceDispersing {
    int getMaxDispersion();

    void onAirDispersion(int amount);
}
