/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.upgrademanager;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftContainerScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.inventory.ChargingStationUpgradeManagerMenu;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.upgrades.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class AbstractUpgradeManagerScreen extends AbstractPneumaticCraftContainerScreen<ChargingStationUpgradeManagerMenu,ChargingStationBlockEntity> {
    private static final long TIER_CYCLE_TIME = 20L;

    protected final ItemStack itemStack;
    private Button guiBackButton;
    private final Map<PNCUpgrade, IGuiAnimatedStat> cycleTabs = new Object2ObjectOpenHashMap<>();

    AbstractUpgradeManagerScreen(ChargingStationUpgradeManagerMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
        itemStack = te.getItemHandler().getStackInSlot(ChargingStationBlockEntity.CHARGE_INVENTORY_INDEX);

        imageHeight = 182;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_CHARGING_UPGRADE_MANAGER;
    }

    @Override
    public void init() {
        super.init();

        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        guiBackButton = new WidgetButtonExtended(xStart + 8, yStart + 5, 16, 16, Symbols.TRIANGLE_LEFT).withTag("close_upgrades");
        addRenderableWidget(guiBackButton);
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        int upgrades = UpgradableItemUtils.getUpgradeCount(itemStack, ModUpgrades.VOLUME.get());
        int volume = PNCCapabilities.getAirHandler(itemStack).map(IAirHandler::getVolume).orElse(getDefaultVolume());
        float curPressure = te.chargingItemPressure;
        addPressureInfo(pressureStatText, curPressure, volume, getDefaultVolume(), upgrades);
    }

    @Override
    protected void addExtraVolumeModifierInfo(List<Component> text) {
        ItemRegistry.getInstance().addVolumeModifierInfo(itemStack, text);
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
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        graphics.drawString(font, itemStack.getHoverName(), (imageWidth - font.width(itemStack.getHoverName())) / 2, 5, 0x404040, false);

        int gaugeX = imageWidth * 3 / 4 + 10;
        int gaugeY = imageHeight / 4 + 10;

        PNCCapabilities.getAirHandler(itemStack).ifPresent(h ->
                PressureGaugeRenderer2D.drawPressureGauge(graphics, font, 0, h.maxPressure(), h.maxPressure(),
                        0, te.chargingItemPressure, gaugeX, gaugeY));

        graphics.pose().pushPose();
        graphics.pose().scale(2f, 2f, 2f);
        graphics.renderItem(itemStack, 3, 22);
        graphics.pose().popPose();
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

    @Override
    public void containerTick() {
        super.containerTick();

        long gameTime = ClientUtils.getClientLevel().getGameTime();
        if (gameTime % TIER_CYCLE_TIME == 0) {
            cycleTabs.forEach((upgrade, tab) -> {
                long tier = gameTime % (TIER_CYCLE_TIME * upgrade.getMaxTier()) / TIER_CYCLE_TIME;
                ItemStack stack = new ItemStack(upgrade.getItem((int) tier + 1));
                tab.setTitle(stack.getHoverName());
                tab.setTexture(stack);
            });
        }
    }

    void addUpgradeTabs(Item item, String... what) {
        boolean leftSided = true;

        for (PNCUpgrade upgrade: PneumaticRegistry.getInstance().getUpgradeRegistry().getKnownUpgrades()) {
            if (upgrade.isDependencyLoaded()) {
                int max = ApplicableUpgradesDB.getInstance().getMaxUpgrades(item, upgrade);
                if (max > 0) {
                    ItemStack upgradeStack = upgrade.getItemStack();
                    List<Component> text = new ArrayList<>();
                    text.add(xlate("pneumaticcraft.gui.tab.upgrades.max", max).withStyle(ChatFormatting.GRAY));
                    for (String w : what) {
                        String name = PneumaticCraftUtils.modDefaultedString(upgrade.getId()).replace(':', '.');
                        String key = "pneumaticcraft.gui.tab.info.item." + w + "." + name + "Upgrade";
                        if (I18n.exists(key)) {
                            text.addAll(GuiUtils.xlateAndSplit(key));
                            break;
                        }
                    }
                    IGuiAnimatedStat stat = addAnimatedStat(upgradeStack.getHoverName(), upgradeStack, 0xFF1C53A8, leftSided).setText(text);
                    stat.setForegroundColor(0xFF000000);
                    if (upgrade.getMaxTier() > 1) cycleTabs.put(upgrade, stat);
                    leftSided = !leftSided;
                }
            }
        }
    }
}
