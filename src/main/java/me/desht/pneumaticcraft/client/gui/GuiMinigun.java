package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiMinigun extends GuiPneumaticInventoryItem {
    public GuiMinigun(ContainerChargingStationItemInventory container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tooltip.item.pneumaticcraft.minigun");

        List<EnumUpgrade> upgrades = getApplicableUpgrades();
        for (int i = 0; i < upgrades.size(); i++) {
            addUpgradeStat(upgrades.get(i), i <= upgrades.size() / 2);
        }
    }

    private void addUpgradeStat(EnumUpgrade upgrade, boolean leftSided) {
        ItemStack stack = upgrade.getItemStack();
        String key ="gui.tab.info.item.minigun." + upgrade.getName() + "Upgrade";
        addAnimatedStat(stack.getDisplayName().getFormattedText(), stack, 0xFF4040FF, leftSided).setText(key);
    }

    @Override
    protected int getDefaultVolume() {
        return PneumaticValues.AIR_CANISTER_VOLUME;
    }
}
