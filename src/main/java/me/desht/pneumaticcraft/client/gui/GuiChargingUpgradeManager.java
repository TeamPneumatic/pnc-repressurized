package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationUpgradeManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
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

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class GuiChargingUpgradeManager extends GuiPneumaticContainerBase<ContainerChargingStationUpgradeManager,TileEntityChargingStation> {

    protected final ItemStack itemStack;
    private Button guiBackButton;

    GuiChargingUpgradeManager(ContainerChargingStationUpgradeManager container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        itemStack = te.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);

        ySize = 182;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_CHARGING_UPGRADE_MANAGER;
    }

    @Override
    public void init() {
        super.init();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiBackButton = new WidgetButtonExtended(xStart + 8, yStart + 5, 16, 16, GuiConstants.TRIANGLE_LEFT).withTag("close_upgrades");
        addButton(guiBackButton);
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        int upgrades = UpgradableItemUtils.getUpgrades(itemStack, EnumUpgrade.VOLUME);
        int volume = ApplicableUpgradesDB.getInstance().getUpgradedVolume(getDefaultVolume(), upgrades);
        float curPressure = te.chargingItemPressure;
        addPressureInfo(pressureStatText, curPressure, volume, getDefaultVolume(), upgrades);
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
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        font.func_238422_b_(matrixStack, itemStack.getDisplayName().func_241878_f(), (xSize - font.getStringPropertyWidth(itemStack.getDisplayName())) / 2f, 5, 0x404040);

        int gaugeX = xSize * 3 / 4 + 10;
        int gaugeY = ySize / 4 + 10;

        itemStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .ifPresent(h -> PressureGaugeRenderer2D.drawPressureGauge(matrixStack, font, 0, h.maxPressure(), h.maxPressure(), 0, te.chargingItemPressure, gaugeX, gaugeY));

        matrixStack.push();
        matrixStack.scale(2f, 2f, 2f);
        GuiUtils.renderItemStack(matrixStack, itemStack, 3, 22);
        matrixStack.pop();
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
            if (upgrade.isDepLoaded()) {
                int max = ApplicableUpgradesDB.getInstance().getMaxUpgrades(item, upgrade);
                if (max > 0) {
                    ItemStack upgradeStack = upgrade.getItemStack();
                    List<ITextComponent> text = new ArrayList<>();
                    text.add(xlate("pneumaticcraft.gui.tab.upgrades.max", max).mergeStyle(TextFormatting.GRAY));
                    for (String w : what) {
                        String key = "pneumaticcraft.gui.tab.info.item." + w + "." + upgrade.getName() + "Upgrade";
                        if (I18n.hasKey(key)) {
                            text.addAll(GuiUtils.xlateAndSplit(key));
                            break;
                        }
                    }
                    addAnimatedStat(upgradeStack.getDisplayName(), upgradeStack, 0xFF244BB3, leftSided).setText(text).setForegroundColor(0xFF000000);
                    leftSided = !leftSided;
                }
            }
        }
    }
}
