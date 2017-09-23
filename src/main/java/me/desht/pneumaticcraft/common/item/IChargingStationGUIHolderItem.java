package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;

/**
 * Represents an item which has its own GUI, openable via the "inv." button in the Charging Station GUI
 */
public interface IChargingStationGUIHolderItem {
    /**
     * Get the GuiHandler ID for this item.
     *
     * @return the GuiHandler ID
     */
    EnumGuiId getGuiID();
}
