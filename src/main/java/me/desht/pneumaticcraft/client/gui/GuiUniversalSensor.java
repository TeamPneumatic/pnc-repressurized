package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorSetting;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.lib.GuiConstants.*;

public class GuiUniversalSensor extends GuiPneumaticContainerBase<ContainerUniversalSensor,TileEntityUniversalSensor> {
    private WidgetAnimatedStat sensorInfoStat;
    private TextFieldWidget nameFilterField;
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

        sensorInfoStat = addAnimatedStat("Sensor Info", new ItemStack(ModBlocks.UNIVERSAL_SENSOR.get()), 0xFFFFAA00, false);
        addAnimatedStat("pneumaticcraft.gui.tab.upgrades", Textures.GUI_UPGRADES_LOCATION, 0xFF6060FF, true).setText(getUpgradeText());

        nameFilterField = new TextFieldWidget(font, xStart + 70, yStart + 58, 98, 10, "");
        nameFilterField.setText(te.getText(0));
        nameFilterField.setResponder(s -> sendDelayed(5));
        addButton(nameFilterField);

        updateButtons();
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        if (maxPage > 1) {
            font.drawString(page + "/" + maxPage, 110, 46 + 22 * MAX_SENSORS_PER_PAGE, 0x404040);
        }

        String[] folders = te.getSensorSetting().split("/");
        if (folders.length == 1 && !folders[0].isEmpty()) {
            Set<EnumUpgrade> requiredUpgrades = SensorHandler.getInstance().getRequiredStacksFromText(folders[0]);
            int curX = 92;
            for (EnumUpgrade upgrade : requiredUpgrades) {
                GuiUtils.drawItemStack(upgrade.getItemStack(), curX, 20);
                curX += 18;
            }
        } else {
            int xSpace = xSize - 96;
            int size = font.getStringWidth(folders[folders.length - 1]);
            RenderSystem.pushMatrix();
            RenderSystem.translated(92, 24, 0);
            if (size > xSpace) {
                RenderSystem.scaled((float)xSpace / (float)size, 1, 1);
            }
            font.drawString(folders[folders.length - 1], 0, 0, 0x4040A0);
            RenderSystem.popMatrix();
        }

        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(this, font,
                    PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.entityFilter.helpText"), 60));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeScreen();
        }

        return nameFilterField.keyPressed(keyCode, scanCode, modifiers)
                || nameFilterField.canWrite()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected PointXY getInvTextOffset() {
        return new PointXY(0, 2);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting());
        if (sensor != null) {
            RenderSystem.translated(guiLeft, guiTop, 0);
            sensor.drawAdditionalInfo(font);
            RenderSystem.translated(-guiLeft, -guiTop, 0);
        }
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

        if (!te.getSensorSetting().equals("")) {
            addButtonLocal(new WidgetButtonExtended(guiLeft + 70, guiTop + 18, 20, 20, ARROW_LEFT_SHORT).withTag("back"));
        } else {
            addButtonLocal(new WidgetButtonExtended(guiLeft + 70, guiTop + 125, 98, 20, I18n.format("pneumaticcraft.gui.button.showRange"), b -> { onClose(); te.showRangeLines(); }));
        }

        String[] directories = SensorHandler.getInstance().getDirectoriesAtLocation(te.getSensorSetting());
        maxPage = (directories.length - 1) / MAX_SENSORS_PER_PAGE + 1;
        if (page > maxPage) page = maxPage;
        if (page < 1) page = 1;
        if (maxPage > 1) {
            addButtonLocal(new WidgetButtonExtended(guiLeft + 70, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, ARROW_LEFT, b -> {
                page--;
                if (page <= 0) page = maxPage;
                updateButtons();
            }));
            addButtonLocal(new WidgetButtonExtended(guiLeft + 138, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, ARROW_RIGHT, b -> {
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
        }
        nameFilterField.setFocused2(textboxEnabled);
    }

    private void addButtonLocal(Widget w) {
        addButton(w);
        sensorButtons.add(w);
    }

    @Override
    protected void doDelayedAction() {
        te.setText(0, nameFilterField.getText());
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
    }

    @Override
    public void tick() {
        super.tick();

        if (te.getSensorSetting().equals("") && ticksExisted++ > 5) {
            ticksExisted = 0;
            updateButtons();
        }
        if (!nameFilterField.isFocused()) {
            nameFilterField.setText(te.getText(0));
        }
    }

    private List<String> getUpgradeText() {
        List<String> upgradeInfo = new ArrayList<>();
        upgradeInfo.add("pneumaticcraft.gui.tab.upgrades.generic.volume");
        upgradeInfo.add("pneumaticcraft.gui.tab.upgrades.generic.security");
        upgradeInfo.addAll(SensorHandler.getInstance().getUpgradeInfo());
        return upgradeInfo;
    }

    private List<String> getSensorInfo() {
        List<String> text = new ArrayList<>();
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting());
        if (sensor != null) {
            String[] folders = te.getSensorSetting().split("/");
            text.add(TextFormatting.WHITE + folders[folders.length - 1]);
            text.addAll(sensor.getDescription());
        } else {
            text.add(TextFormatting.BLACK + "No sensor selected.");
        }
        return text;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        if (te.isSensorActive) {
            pressureStatText.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tooltip.airUsage", PneumaticValues.USAGE_UNIVERSAL_SENSOR));
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        if (!te.getPrimaryInventory().getStackInSlot(0).isEmpty() && te.outOfRange > 0) {
            curInfo.addAll(PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.universalSensor.outOfRange", te.outOfRange)));
        }
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        if (!te.lastSensorExceptionText.isEmpty()) {
            curInfo.addAll(PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.universalSensor.sensorException", te.lastSensorExceptionText)));
        }
        if (te.sensorStatus != SensorStatus.OK) {
            curInfo.addAll(PneumaticCraftUtils.splitString(I18n.format(te.sensorStatus.getTranslationKey())));
        }
    }
}
