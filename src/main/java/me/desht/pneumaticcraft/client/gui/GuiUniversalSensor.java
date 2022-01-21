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

package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.misc.RangedInt;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorSetting;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRangeToggleButton;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerUniversalSensor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor.SensorStatus;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static me.desht.pneumaticcraft.api.misc.Symbols.*;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiUniversalSensor extends GuiPneumaticContainerBase<ContainerUniversalSensor,TileEntityUniversalSensor> {
    private static final int MAX_TEXTFIELD_LENGTH = 256;

    private WidgetAnimatedStat sensorInfoStat;
    private EditBox nameFilterField;
    private int page;
    private int maxPage;
    private static final int MAX_SENSORS_PER_PAGE = 4;
    private int ticksExisted;
    private final List<AbstractWidget> sensorButtons = new ArrayList<>();

    public GuiUniversalSensor(ContainerUniversalSensor container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageHeight = 239;
    }

    public static void maybeUpdateButtons() {
        Screen guiScreen = Minecraft.getInstance().screen;
        if (guiScreen instanceof GuiUniversalSensor) {
            ((GuiUniversalSensor) guiScreen).updateButtons();
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_UNIVERSAL_SENSOR;
    }

    @Override
    public void init() {
        super.init();

        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;

        sensorInfoStat = addAnimatedStat(new TextComponent("Sensor Info"), new ItemStack(ModBlocks.UNIVERSAL_SENSOR.get()), 0xFFFFAA00, false);
        sensorInfoStat.setForegroundColor(0xFF000000);

        nameFilterField = new EditBox(font, xStart + 70, yStart + 58, 98, 10, TextComponent.EMPTY);
        nameFilterField.setMaxLength(MAX_TEXTFIELD_LENGTH);
        nameFilterField.setValue(te.getText(0));
        nameFilterField.setResponder(s -> sendDelayed(5));
        addRenderableWidget(nameFilterField);

        updateButtons();
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) {
        super.renderLabels(matrixStack, x, y);

        if (maxPage > 1) {
            font.draw(matrixStack, page + "/" + maxPage, 110, 46 + 22 * MAX_SENSORS_PER_PAGE, 0x404040);
        }

        String[] folders = te.getSensorSetting().split("/");
        if (folders.length == 1 && !folders[0].isEmpty()) {
            Set<EnumUpgrade> requiredUpgrades = SensorHandler.getInstance().getRequiredStacksFromText(folders[0]);
            int curX = 92;
            for (EnumUpgrade upgrade : requiredUpgrades) {
                GuiUtils.renderItemStack(matrixStack, upgrade.getItemStack(), curX, 20);
                curX += 18;
            }
        } else {
            int xSpace = imageWidth - 96;
            int size = font.width(folders[folders.length - 1]);
            matrixStack.pushPose();
            matrixStack.translate(92, 24, 0);
            if (size > xSpace) {
                matrixStack.scale((float)xSpace / (float)size, 1, 1);
            }
            font.draw(matrixStack, folders[folders.length - 1], 0, 0, 0x4040A0);
            matrixStack.popPose();
        }

        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting());
        if (sensor != null) {
            List<Component> info = new ArrayList<>();
            sensor.getAdditionalInfo(info);
            int yOff = 0;
            for (Component line : info) {
                font.draw(matrixStack, line.getVisualOrderText(), 70, 48 + yOff, 0x404040);
                yOff += font.lineHeight;
            }
            nameFilterField.y = topPos + 48 + yOff + 2;
        }

        if (nameFilterField.visible && sensor != null && sensor.isEntityFilter()) {
            if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
                GuiUtils.showPopupHelpScreen(matrixStack, this, font,
                        GuiUtils.xlateAndSplit("pneumaticcraft.gui.entityFilter.helpText"));
            } else {
                String str = I18n.get("pneumaticcraft.gui.entityFilter.holdF1");
                font.draw(matrixStack, str, (imageWidth - font.width(str)) / 2f, imageHeight + 5, 0x808080);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeContainer();
        }

        return nameFilterField.keyPressed(keyCode, scanCode, modifiers)
                || nameFilterField.canConsumeInput()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected PointXY getInvTextOffset() {
        return new PointXY(0, 2);
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + 34, yStart + imageHeight / 4 - 18);
    }

    private void updateButtons() {
        sensorButtons.forEach(this::removeWidget);
        sensorButtons.clear();

        String[] directories = SensorHandler.getInstance().getDirectoriesAtLocation(te.getSensorSetting());

        if (!te.getSensorSetting().isEmpty()) {
            addButtonLocal(new WidgetButtonExtended(leftPos + 70, topPos + 20, 16, 16, ARROW_LEFT).withTag("back"));
        }
        if (directories.length == 0 || te.getSensorSetting().isEmpty()) {
            addButtonLocal(new WidgetRangeToggleButton(leftPos + 150, topPos + 130, te));
        }

        maxPage = (directories.length - 1) / MAX_SENSORS_PER_PAGE + 1;
        if (page > maxPage) page = maxPage;
        if (page < 1) page = 1;
        if (maxPage > 1) {
            addButtonLocal(new WidgetButtonExtended(leftPos + 70, topPos + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, TRIANGLE_LEFT, b -> {
                page--;
                if (page <= 0) page = maxPage;
                updateButtons();
            }));
            addButtonLocal(new WidgetButtonExtended(leftPos + 138, topPos + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, TRIANGLE_RIGHT, b -> {
                page++;
                if (page > maxPage) page = 1;
                updateButtons();
            }));
        }

        int buttonsOnPage = MAX_SENSORS_PER_PAGE;
        if (page == maxPage && (directories.length % MAX_SENSORS_PER_PAGE > 0 || directories.length == 0)) {
            buttonsOnPage = directories.length % MAX_SENSORS_PER_PAGE;
        }
        for (int i = 0; i < buttonsOnPage; i++) {
            String buttonText = directories[i + (page - 1) * MAX_SENSORS_PER_PAGE];
            if (SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting() + "/" + buttonText) != null) {
                buttonText = ChatFormatting.YELLOW + buttonText;
            }
            int buttonID = i * 10 + 10 + (page - 1) * MAX_SENSORS_PER_PAGE * 10;
            int buttonX = leftPos + 70;
            int buttonY = topPos + 40 + i * 22;
            int buttonWidth = 98;
            int buttonHeight = 20;
            if (te.getSensorSetting().equals("")) {
                Set<EnumUpgrade> requiredUpgrades = SensorHandler.getInstance().getRequiredStacksFromText(buttonText);
                WidgetButtonExtended button = new WidgetButtonExtended(buttonX, buttonY, buttonWidth, buttonHeight, "").withTag("set:" + buttonID);
                button.setRenderStacks(requiredUpgrades.stream().map(EnumUpgrade::getItemStack).toArray(ItemStack[]::new));
                button.active = (te.sensorStatus == SensorStatus.OK || te.sensorStatus == SensorStatus.NO_SENSOR)
                        && te.areGivenUpgradesInserted(requiredUpgrades);
                addButtonLocal(button);
            } else {
                addButtonLocal(new WidgetButtonExtended(buttonX, buttonY, buttonWidth, buttonHeight, buttonText).withTag("set:" + buttonID));
            }
        }
        sensorInfoStat.setText(getSensorInfo());

        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting());
        boolean textboxEnabled = sensor != null && sensor.needsTextBox();
        nameFilterField.setVisible(textboxEnabled);
        if (textboxEnabled) {
            setFocused(nameFilterField);
            RangedInt range = sensor.getTextboxIntRange();
            if (range != null) {
                nameFilterField.setFilter(s -> validateTextValue(s, range));
                String max = Integer.toString(range.max());
                nameFilterField.setMaxLength(max.length() + 1);
                nameFilterField.setWidth(font.width(max) + 10);
            } else {
                nameFilterField.setFilter(Objects::nonNull);
                nameFilterField.setMaxLength(MAX_TEXTFIELD_LENGTH);
                nameFilterField.setWidth(98);
            }
        }
        nameFilterField.setFocus(textboxEnabled);
    }

    private boolean validateTextValue(String s, RangedInt r) {
        if (PneumaticCraftUtils.isInteger(s)) {
            int n = s.isEmpty() || s.equals("-") ? 0 : Integer.parseInt(s);
            return r.test(n);
        }
        return false;
    }

    private void addButtonLocal(AbstractWidget w) {
        addRenderableWidget(w);
        sensorButtons.add(w);
    }

    @Override
    protected void doDelayedAction() {
        te.setText(0, nameFilterField.getValue());
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (te.getSensorSetting().isEmpty() && ticksExisted++ > 5) {
            ticksExisted = 0;
            updateButtons();
        }
        if (!nameFilterField.isFocused()) {
            nameFilterField.setValue(te.getText(0));
        }
    }

    private List<Component> getSensorInfo() {
        List<Component> text = new ArrayList<>();
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting());
        if (sensor != null) {
            String[] folders = te.getSensorSetting().split("/");
            text.add(new TextComponent(folders[folders.length - 1]).withStyle(ChatFormatting.WHITE));
            text.addAll(GuiUtils.xlateAndSplit(sensor.getDescription().get(0)));
        } else {
            text.add(xlate("pneumaticcraft.gui.misc.none").withStyle(ChatFormatting.BLACK));
        }
        return text;
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        if (te.isSensorActive) {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.airUsage", PneumaticValues.USAGE_UNIVERSAL_SENSOR).withStyle(ChatFormatting.BLACK));
        }
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);

        if (!te.getPrimaryInventory().getStackInSlot(0).isEmpty() && te.outOfRange > 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.universalSensor.outOfRange", te.outOfRange));
        }
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        if (!te.lastSensorExceptionText.isEmpty()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.universalSensor.sensorException", te.lastSensorExceptionText));
        }
        if (te.sensorStatus != SensorStatus.OK) {
            curInfo.addAll(GuiUtils.xlateAndSplit(te.sensorStatus.getTranslationKey()));
        }
    }
}
