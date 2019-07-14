package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.item.ItemDrone;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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
        ItemDrone itemDrone = (ItemDrone) itemStack.getItem();

        maybeAddUpgradeTab(itemDrone, EnumUpgrade.SPEED, false);
        maybeAddUpgradeTab(itemDrone, EnumUpgrade.DISPENSER, false);
        maybeAddUpgradeTab(itemDrone, EnumUpgrade.ITEM_LIFE, false);
        maybeAddUpgradeTab(itemDrone, EnumUpgrade.MAGNET, false);
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tab.info.item.drone");
        maybeAddUpgradeTab(itemDrone, EnumUpgrade.SECURITY, true);
        maybeAddUpgradeTab(itemDrone, EnumUpgrade.VOLUME, true);
        maybeAddUpgradeTab(itemDrone, EnumUpgrade.ENTITY_TRACKER, true);
        maybeAddUpgradeTab(itemDrone, EnumUpgrade.RANGE, true);

    }

    private void maybeAddUpgradeTab(ItemDrone itemDrone, EnumUpgrade upgrade, boolean leftSided) {
        if (itemDrone.upgradeApplies(upgrade)) {
            ItemStack upgradeStack = CraftingRegistrator.getUpgrade(upgrade);
            addAnimatedStat(upgradeStack.getDisplayName().getFormattedText(), upgradeStack,
                    0xFF4040FF, leftSided).setText("gui.tab.info.item.drone." + upgrade.getName() + "Upgrade");
        }
    }

    @Override
    protected int getDefaultVolume() {
        return PneumaticValues.DRONE_VOLUME;
    }
}
