package me.desht.pneumaticcraft.common.progwidgets;

public interface IMaxActions {
    default boolean supportsMaxActions() { return true; }

    void setMaxActions(int maxActions);

    int getMaxActions();

    void setUseMaxActions(boolean useMaxActions);

    boolean useMaxActions();
}
