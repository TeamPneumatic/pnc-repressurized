package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class GuiPneumaticInventoryItem extends GuiPneumaticContainerBase<TileEntityChargingStation> {

    protected final ItemStack itemStack;
    private GuiButton guiBackButton;

    public GuiPneumaticInventoryItem(ContainerChargingStationItemInventory container, TileEntityChargingStation te) {
        super(container, te, Textures.GUI_PNEUMATIC_ARMOR_LOCATION);
        itemStack = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
    }

    @Override
    public void initGui() {
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiBackButton = new GuiButton(2, xStart + 90, yStart + 15, 25, 20, "\u2b05");
        buttonList.add(guiBackButton);
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        pressureStatText.add("\u00a77Current Pressure:");
        ItemStack stack = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
        float curPressure = te.chargingItemPressure;
        int volume = UpgradableItemUtils.getUpgrades(EnumUpgrade.VOLUME, stack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getDefaultVolume();
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

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        NetworkHandler.sendToServer(new PacketGuiButton(button.id));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        String containerName = itemStack.getDisplayName();
        fontRenderer.drawString(containerName, xSize / 2 - fontRenderer.getStringWidth(containerName) / 2, 4, 4210752);
        fontRenderer.drawString(I18n.format("gui.tab.upgrades"), 36, 14, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        IPressurizable p = (IPressurizable) itemStack.getItem();
        GuiUtils.drawPressureGauge(fontRenderer, 0, p.maxPressure(itemStack), p.maxPressure(itemStack), 0,
                te.chargingItemPressure, xStart + xSize * 3 / 4 + 8, yStart + ySize / 4 + 4, zLevel);
    }

    @Override
    protected Point getGaugeLocation() {
        return null;
    }

    @Override
    protected void keyTyped(char key, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            // equivalent to pressing the Back button
            actionPerformed(guiBackButton);
        } else {
            super.keyTyped(key, keyCode);
        }
    }
}
