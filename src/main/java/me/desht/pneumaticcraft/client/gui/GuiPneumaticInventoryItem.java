package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiPneumaticInventoryItem extends GuiPneumaticContainerBase<ContainerChargingStationItemInventory,TileEntityChargingStation> {

    protected final ItemStack itemStack;
    private Button guiBackButton;

    GuiPneumaticInventoryItem(ContainerChargingStationItemInventory container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        itemStack = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);

        ySize = 176;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_PNEUMATIC_ARMOR;
    }

    @Override
    public void init() {
        super.init();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiBackButton = new WidgetButtonExtended(xStart + 152, yStart + 4, 18, 18, GuiConstants.ARROW_LEFT_SHORT).withTag("close_upgrades");
        addButton(guiBackButton);
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        int upgrades = UpgradableItemUtils.getUpgrades(itemStack, EnumUpgrade.VOLUME);
        int volume = ApplicableUpgradesDB.getInstance().getUpgradedVolume(getDefaultVolume(), upgrades);
        float curPressure = te.chargingItemPressure;
        String col = TextFormatting.BLACK.toString();

        pressureStatText.add(col + I18n.format("gui.tooltip.pressure",
                PneumaticCraftUtils.roundNumberTo(te.chargingItemPressure, 2)));
        pressureStatText.add(col + I18n.format("gui.tooltip.air", String.format("%,d", Math.round(curPressure * volume))));
        pressureStatText.add(col + I18n.format("gui.tooltip.baseVolume", String.format("%,d", getDefaultVolume())));
        if (volume > getDefaultVolume()) {
            pressureStatText.add(col + GuiConstants.TRIANGLE_RIGHT + " " + upgrades + " x " + EnumUpgrade.VOLUME.getItemStack().getDisplayName().getFormattedText());
            pressureStatText.add(col + I18n.format("gui.tooltip.effectiveVolume", String.format("%,d",volume)));
        }
    }

    @Override
    protected boolean shouldAddRedstoneTab() {
        return false;
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }

    @Override
    protected boolean shouldAddInfoTab() {
        return false;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    protected abstract int getDefaultVolume();

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        String containerName = itemStack.getDisplayName().getFormattedText();
        font.drawString(containerName, xSize / 2f - font.getStringWidth(containerName) / 2f, 5, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        itemStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(h -> {
            int xStart = (width - xSize) / 2;
            int yStart = (height - ySize) / 2;
            GuiUtils.drawPressureGauge(font, 0, h.maxPressure(), h.maxPressure(), 0,
                    te.chargingItemPressure, xStart + xSize * 3 / 4 + 10, yStart + ySize / 4 + 4);
        });
    }

    @Override
    protected PointXY getGaugeLocation() {
        return null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            guiBackButton.onPress();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    void addUpgradeTabs(Item item, String... what) {
        boolean leftSided = true;
        for (EnumUpgrade upgrade : EnumUpgrade.values()) {
            int max = ApplicableUpgradesDB.getInstance().getMaxUpgrades(item, upgrade);
            if (max > 0) {
                ItemStack upgradeStack = upgrade.getItemStack();
                List<String> text = new ArrayList<>();
                text.add(TextFormatting.GRAY + I18n.format("gui.tab.upgrades.max", max));
                for (String w : what) {
                    String key = "gui.tab.info.item." + w + "." + upgrade.getName() + "Upgrade";
                    if (I18n.hasKey(key)) {
                        text.addAll(PneumaticCraftUtils.splitString(I18n.format(key)));
                        break;
                    }
                }
                addAnimatedStat(upgradeStack.getDisplayName().getFormattedText(), upgradeStack, 0xFF6060FF, leftSided)
                        .setTextWithoutCuttingString(text);
                leftSided = !leftSided;
            }
        }
    }
}
