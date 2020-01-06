package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class GuiPneumaticInventoryItem extends GuiPneumaticContainerBase<ContainerChargingStationItemInventory,TileEntityChargingStation> {

    protected final ItemStack itemStack;
    private Button guiBackButton;

    GuiPneumaticInventoryItem(ContainerChargingStationItemInventory container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        itemStack = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_PNEUMATIC_ARMOR_LOCATION;
    }

    @Override
    public void init() {
        super.init();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiBackButton = new WidgetButtonExtended(xStart + 90, yStart + 15, 25, 20, "\u2b05").withTag("close_upgrades");
        addButton(guiBackButton);
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        pressureStatText.add("\u00a77Current Pressure:");
        float curPressure = te.chargingItemPressure;
        int volume = UpgradableItemUtils.getUpgrades(itemStack, EnumUpgrade.VOLUME) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getDefaultVolume();
        pressureStatText.add("\u00a70" + (double) Math.round(curPressure * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double) Math.round(curPressure * volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + getDefaultVolume() + " mL.");
        if (volume > getDefaultVolume()) {
            pressureStatText.add("\u00a70" + (double) Math.round(volume - getDefaultVolume()) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double) Math.round(volume) + " mL.");
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
        font.drawString(containerName, xSize / 2f - font.getStringWidth(containerName) / 2f, 4, 0x404040);
        font.drawString(I18n.format("gui.tab.upgrades"), 36, 14, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        itemStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(h -> {
            int xStart = (width - xSize) / 2;
            int yStart = (height - ySize) / 2;
            GuiUtils.drawPressureGauge(font, 0, h.maxPressure(), h.maxPressure(), 0,
                    te.chargingItemPressure, xStart + xSize * 3 / 4 + 8, yStart + ySize / 4 + 4);
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

    List<EnumUpgrade> getApplicableUpgrades() {
        Map<EnumUpgrade, Integer> map = ((IUpgradeAcceptor)itemStack.getItem()).getApplicableUpgrades();
        return map.keySet().stream()
                .sorted(Comparator.comparing(upgrade -> I18n.format(upgrade.getItem().getTranslationKey())))
                .collect(Collectors.toList());
    }
}
