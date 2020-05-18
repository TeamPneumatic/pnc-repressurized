package me.desht.pneumaticcraft.common.progwidgets;

public interface IItemDropper {
    boolean dropStraight();

    void setDropStraight(boolean dropStraight);

    boolean hasPickupDelay();

    void setPickupDelay(boolean pickupDelay);
}
