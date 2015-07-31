package pneumaticCraft.common.item;

import pneumaticCraft.proxy.CommonProxy.EnumGuiId;


public interface IChargingStationGUIHolderItem{

    /**
     * Should return the GuiHandler's ID for this item.
     * @return
     */
    public EnumGuiId getGuiID();

}
