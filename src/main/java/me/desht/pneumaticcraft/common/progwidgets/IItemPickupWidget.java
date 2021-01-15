package me.desht.pneumaticcraft.common.progwidgets;

public interface IItemPickupWidget extends IItemFiltering {
    /**
     * Should this widget ignore PreventRemoteMovement tags on item entities?
     * @return true if items can be "stolen" e.g. off conveyor belts, false to keep the drone honest
     */
    boolean canSteal();

    void setCanSteal(boolean canSteal);
}
