package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorSetting;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static me.desht.pneumaticcraft.lib.GuiConstants.*;

public class GuiUniversalSensor extends GuiPneumaticContainerBase<ContainerUniversalSensor,TileEntityUniversalSensor> {
    private static final int MAX_TEXTFIELD_LENGTH = 256;

    private WidgetAnimatedStat sensorInfoStat;
    private TextFieldWidget textField;
    private WidgetComboBox comboBox;
    private int page;
    private int maxPage;
    private static final int MAX_SENSORS_PER_PAGE = 4;
    private int ticksExisted;
    private final List<Widget> sensorButtons = new ArrayList<>();

    public GuiUniversalSensor(ContainerUniversalSensor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        ySize = 239;
    }

    public static void maybeUpdateButtons() {
        Screen guiScreen = Minecraft.getInstance().currentScreen;
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

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        sensorInfoStat = addAnimatedStat(new StringTextComponent("Sensor Info"), new ItemStack(ModBlocks.UNIVERSAL_SENSOR.get()), 0xFFFFAA00, false);
        sensorInfoStat.setForegroundColor(0xFF000000);

        textField = new TextFieldWidget(font, xStart + 70, yStart + 58, 98, 10, StringTextComponent.EMPTY);
        textField.setMaxStringLength(MAX_TEXTFIELD_LENGTH);
        textField.setText(te.getText(0));
        addButton(textField);

        comboBox = new WidgetComboBox(font, xStart + 70, yStart + 58, 98, 10);
        comboBox.setMaxStringLength(MAX_TEXTFIELD_LENGTH);
        comboBox.setText(te.getText(0));
        addButton(comboBox);
        comboBox.setVisible(false);

        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);

        if (maxPage > 1) {
            font.drawString(matrixStack, page + "/" + maxPage, 110, 46 + 22 * MAX_SENSORS_PER_PAGE, 0x404040);
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
            int xSpace = xSize - 92;
            int size = font.getStringWidth(folders[folders.length - 1]);
            matrixStack.push();
            matrixStack.translate(88, 24, 0);
            if (size > xSpace) {
                matrixStack.scale((float)xSpace / (float)size, 1, 1);
            }
            font.drawString(matrixStack, folders[folders.length - 1], 0, 0, 0x4040A0);
            matrixStack.pop();
        }

        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting());
        if (sensor != null) {
            List<ITextComponent> info = new ArrayList<>();
            sensor.getAdditionalInfo(info);
            int yOff = 0;
            for (ITextComponent line : info) {
                font.func_238422_b_(matrixStack, line.func_241878_f(), 70, 48 + yOff, 0x404040);
                yOff += font.FONT_HEIGHT;
            }
            textField.y = comboBox.y = guiTop + 48 + yOff + 2;
        }

        if (sensor != null && !sensor.getHelpText().isEmpty()) {
            if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
                GuiUtils.showPopupHelpScreen(matrixStack, this, font, GuiUtils.xlateAndSplit(sensor.getHelpText()));
            } else if (!sensor.getHelpPromptText().isEmpty()) {
                String str = I18n.format(sensor.getHelpPromptText());
                font.drawString(matrixStack, str, (xSize - font.getStringWidth(str)) / 2f, ySize + 5, 0xFFFF00);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeScreen();
        }

        return textField.keyPressed(keyCode, scanCode, modifiers)
                || textField.canWrite()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected PointXY getInvTextOffset() {
        return new PointXY(0, 2);
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + 34, yStart + ySize / 4 - 18);
    }

    private void updateButtons() {
        sensorButtons.forEach(w -> {
            buttons.remove(w);
            children.remove(w);
        });
        sensorButtons.clear();

        String[] directories = SensorHandler.getInstance().getDirectoriesAtLocation(te.getSensorSetting());

        if (!te.getSensorSetting().isEmpty()) {
            addButtonLocal(new WidgetButtonExtended(guiLeft + 70, guiTop + 20, 16, 16, ARROW_LEFT).withTag("back"));
        }
        if (directories.length == 0 || te.getSensorSetting().isEmpty()) {
            addButtonLocal(new WidgetRangeToggleButton(guiLeft + 150, guiTop + 130, te));
        }

        maxPage = (directories.length - 1) / MAX_SENSORS_PER_PAGE + 1;
        if (page > maxPage) page = maxPage;
        if (page < 1) page = 1;
        if (maxPage > 1) {
            addButtonLocal(new WidgetButtonExtended(guiLeft + 70, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, TRIANGLE_LEFT, b -> {
                page--;
                if (page <= 0) page = maxPage;
                updateButtons();
            }));
            addButtonLocal(new WidgetButtonExtended(guiLeft + 138, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, TRIANGLE_RIGHT, b -> {
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
                buttonText = TextFormatting.YELLOW + buttonText;
            }
            int buttonID = i * 10 + 10 + (page - 1) * MAX_SENSORS_PER_PAGE * 10;
            int buttonX = guiLeft + 70;
            int buttonY = guiTop + 40 + i * 22;
            int buttonWidth = 98;
            int buttonHeight = 20;
            if (te.getSensorSetting().isEmpty()) {
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
        List<String> textOptions = sensor == null ? null : sensor.getTextBoxOptions(ClientUtils.getClientPlayer());
        textField.setVisible(textboxEnabled && textOptions == null);
        comboBox.setVisible(textboxEnabled && textOptions != null);
        if (textboxEnabled) {
            if (textOptions == null) {
                setListener(textField);
                RangedInteger range = sensor.getTextboxIntRange();
                if (range != null) {
                    textField.setValidator(s -> validateTextValue(s, range));
                    String max = Integer.toString(range.getMax());
                    textField.setMaxStringLength(max.length() + 1);
                    textField.setWidth(font.getStringWidth(max) + 10);
                } else {
                    textField.setValidator(Objects::nonNull);
                    textField.setMaxStringLength(MAX_TEXTFIELD_LENGTH);
                    textField.setWidth(98);
                }
                textField.setFocused2(true);
            } else {
                setListener(comboBox);
                comboBox.setElements(textOptions);
                comboBox.setShouldSort(true);
                comboBox.setFixedOptions(sensor.strictComboBox());
                comboBox.setFocused2(true);
            }
        }
    }

    private boolean validateTextValue(String s, RangedInteger r) {
        if (PneumaticCraftUtils.isInteger(s)) {
            int n = s.isEmpty() || s.equals("-") ? 0 : Integer.parseInt(s);
            return n >= r.getMinInclusive() && n < r.getMax();
        }
        return false;
    }

    private void addButtonLocal(Widget w) {
        addButton(w);
        sensorButtons.add(w);
    }

    @Override
    protected void doDelayedAction() {
        if (textField.getVisible()) {
            te.setText(0, textField.getText());
        } else if (comboBox.getVisible()) {
            te.setText(0, comboBox.getText());
        }
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
    }

    @Override
    public void tick() {
        if (firstUpdate /*|| (!textField.isFocused() && !comboBox.isFocused())*/) {
            if (textField.getVisible()) {
                textField.setText(te.getText(0));
                textField.setResponder(s -> sendDelayed(5));
            } else if (comboBox.getVisible()) {
                comboBox.setText(te.getText(0));
                comboBox.setResponder(s -> sendDelayed(5));
            }
        }

        super.tick();

        if (te.getSensorSetting().isEmpty() && ticksExisted++ > 5) {
            ticksExisted = 0;
            updateButtons();
        }
    }

    private List<ITextComponent> getSensorInfo() {
        List<ITextComponent> text = new ArrayList<>();
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting());
        if (sensor != null) {
            String[] folders = te.getSensorSetting().split("/");
            text.add(new StringTextComponent(folders[folders.length - 1]).mergeStyle(TextFormatting.WHITE));
            text.addAll(GuiUtils.xlateAndSplit(sensor.getDescription().get(0)));
        } else {
            text.add(xlate("pneumaticcraft.gui.misc.none").mergeStyle(TextFormatting.BLACK));
        }
        return text;
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        if (te.isSensorActive) {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.airUsage", PneumaticValues.USAGE_UNIVERSAL_SENSOR).mergeStyle(TextFormatting.BLACK));
        }
    }

    @Override
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);

        if (!te.getPrimaryInventory().getStackInSlot(0).isEmpty() && te.outOfRange > 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.universalSensor.outOfRange", te.outOfRange));
        }
    }

    @Override
    protected void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (!te.lastSensorExceptionText.isEmpty()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.universalSensor.sensorException", te.lastSensorExceptionText));
        }
        if (te.sensorStatus != SensorStatus.OK) {
            curInfo.addAll(GuiUtils.xlateAndSplit(te.sensorStatus.getTranslationKey()));
        }
    }
}
