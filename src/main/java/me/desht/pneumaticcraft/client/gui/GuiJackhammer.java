package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiJackhammer extends GuiPneumaticInventoryItem {
    public GuiJackhammer(ContainerChargingStationItemInventory container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info"), Textures.GUI_INFO_LOCATION, 0xFF8888FF, true)
                .setText("gui.tooltip.item.pneumaticcraft.jackhammer");
        addUpgradeTabs(itemStack.getItem(), "jackhammer");
    }

    @Override
    protected int getDefaultVolume() {
        return PneumaticValues.VOLUME_JACKHAMMER;
    }

}
