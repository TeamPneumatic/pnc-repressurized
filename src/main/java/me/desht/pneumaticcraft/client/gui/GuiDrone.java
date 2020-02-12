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

        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true)
                .setText("gui.tab.info.item.drone");
        addUpgradeTabs(itemStack.getItem(), "drone");
    }

//    private void addUpgradeTabs(ItemDrone itemDrone) {
//        boolean leftSided = true;
//        for (EnumUpgrade upgrade : EnumUpgrade.values()) {
//            int max = ApplicableUpgradesDB.getInstance().getMaxUpgrades(itemDrone, upgrade);
//            if (max > 0) {
//                ItemStack upgradeStack = upgrade.getItemStack();
//                List<String> text = new ArrayList<>();
//                text.add(TextFormatting.GRAY + I18n.format("gui.tab.upgrades.max", max));
//                text.addAll(PneumaticCraftUtils.splitString(I18n.format("gui.tab.info.item.drone." + upgrade.getName() + "Upgrade")));
//                addAnimatedStat(upgradeStack.getDisplayName().getFormattedText(), upgradeStack, 0xFF6060FF, leftSided)
//                        .setTextWithoutCuttingString(text);
//                leftSided = !leftSided;
//            }
//        }
//    }

    @Override
    protected int getDefaultVolume() {
        return PneumaticValues.DRONE_VOLUME;
    }
}
