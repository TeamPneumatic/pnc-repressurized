package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationUpgradeManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiAmadronCharging extends GuiChargingUpgradeManager {
    public GuiAmadronCharging(ContainerChargingStationUpgradeManager container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info"), Textures.GUI_INFO_LOCATION, 0xFF8888FF, true)
                .setText(GuiUtils.xlateAndSplit("gui.tooltip.item.pneumaticcraft.amadron_tablet"));
        addUpgradeTabs(itemStack.getItem(), "amadron_tablet");
    }

    @Override
    protected int getDefaultVolume() {
        return PneumaticValues.AIR_CANISTER_VOLUME;
    }
}
