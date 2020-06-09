package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.item.ItemDrone;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiDrone extends GuiPneumaticInventoryItem {

    public GuiDrone(ContainerChargingStationItemInventory container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        if (!(itemStack.getItem() instanceof ItemDrone)) {
            return; // should never happen...
        }

        addAnimatedStat("pneumaticcraft.gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true)
                .setText("pneumaticcraft.gui.tab.info.item.drone");
        addUpgradeTabs(itemStack.getItem(), itemStack.getItem().getRegistryName().getPath(), "drone");
    }

    @Override
    protected int getDefaultVolume() {
        return PneumaticValues.DRONE_VOLUME;
    }
}
