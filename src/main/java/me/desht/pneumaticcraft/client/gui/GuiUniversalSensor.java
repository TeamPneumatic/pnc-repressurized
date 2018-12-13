package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.universalSensor.ISensorSetting;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerUniversalSensor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class GuiUniversalSensor extends GuiPneumaticContainerBase<TileEntityUniversalSensor> {
    private GuiAnimatedStat sensorInfoStat;
    private GuiTextField nameFilterField;
    private int page;
    private int maxPage;
    private static final int MAX_SENSORS_PER_PAGE = 4;
    private int ticksExisted;

    public GuiUniversalSensor(InventoryPlayer player, TileEntityUniversalSensor te) {
        super(new ContainerUniversalSensor(player, te), te, Textures.GUI_UNIVERSAL_SENSOR);
        ySize = 239;
    }

    @Override
    public void initGui() {
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        sensorInfoStat = addAnimatedStat("Sensor Info", new ItemStack(Blockss.UNIVERSAL_SENSOR), 0xFFFFAA00, false);
        addAnimatedStat("gui.tab.upgrades", Textures.GUI_UPGRADES_LOCATION, 0xFF0000FF, true).setText(getUpgradeText());

        nameFilterField = new GuiTextField(-1, fontRenderer, xStart + 70, yStart + 58, 100, 10);
        nameFilterField.setText(te.getText(0));

        updateButtons();//also adds the redstoneButton.
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {

        super.drawGuiContainerForegroundLayer(x, y);
        if (maxPage > 1) fontRenderer.drawString(page + "/" + maxPage, 110, 46 + 22 * MAX_SENSORS_PER_PAGE, 4210752);
        fontRenderer.drawString("Upgr.", 23, 98, 4210752);

        String[] folders = te.getSensorSetting().split("/");
        if (folders.length == 1 && !folders[0].equals("")) {
            Set<Item> requiredItems = SensorHandler.getInstance().getRequiredStacksFromText(folders[0]);
            int curX = 102;
            for (Item requiredItem : requiredItems) {
                GuiUtils.drawItemStack(new ItemStack(requiredItem), curX, 20);
                curX += 18;
            }
        } else {
            fontRenderer.drawString(folders[folders.length - 1], 102, 24, 4210752);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_F1)) {
            GuiUtils.showPopupHelpScreen(this, fontRenderer,
                    PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.entityFilter.helpText"), 60));
        }
    }

    @Override
    protected Point getInvTextOffset() {
        return new Point(0, 2);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        if (nameFilterField != null) nameFilterField.drawTextBox();

        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting());
        if (sensor != null) {
            sensor.drawAdditionalInfo(fontRenderer);
        }
    }

    @Override
    protected Point getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + 34, yStart + ySize / 4);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        nameFilterField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException {
        if (nameFilterField.isFocused() && par2 != 1) {
            nameFilterField.textboxKeyTyped(par1, par2);
            te.setText(0, nameFilterField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        } else {
            super.keyTyped(par1, par2);
        }
    }

    public void updateButtons() {
        buttonList.clear();
        buttonList.add(redstoneButton);
        if (!te.getSensorSetting().equals("")) {
            buttonList.add(new GuiButton(1, guiLeft + 70, guiTop + 18, 30, 20, "back"));
        } else {
            buttonList.add(new GuiButton(-1, guiLeft + 70, guiTop + 125, 98, 20, I18n.format("gui.universalSensor.button.showRange")));
        }
        String[] directories = SensorHandler.getInstance().getDirectoriesAtLocation(te.getSensorSetting());
        maxPage = (directories.length - 1) / MAX_SENSORS_PER_PAGE + 1;
        if (page > maxPage) page = maxPage;
        if (page < 1) page = 1;
        if (maxPage > 1) {
            buttonList.add(new GuiButton(2, guiLeft + 70, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, "<--"));
            buttonList.add(new GuiButton(3, guiLeft + 138, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, "-->"));
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
                Set<Item> requiredItems = SensorHandler.getInstance().getRequiredStacksFromText(buttonText);
                GuiButtonSpecial button = new GuiButtonSpecial(buttonID, buttonX, buttonY, buttonWidth, buttonHeight, "");
                ItemStack[] requiredStacks = new ItemStack[requiredItems.size()];
                Iterator<Item> iterator = requiredItems.iterator();
                for (int j = 0; j < requiredStacks.length; j++) {
                    requiredStacks[j] = new ItemStack(iterator.next());
                }
                button.setRenderStacks(requiredStacks);
                button.enabled = te.areGivenUpgradesInserted(requiredItems);
                buttonList.add(button);
            } else {
                buttonList.add(new GuiButton(buttonID, buttonX, buttonY, buttonWidth, buttonHeight, buttonText));
            }
        }
        sensorInfoStat.setText(getSensorInfo());
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting());
        boolean textboxEnabled = sensor != null && sensor.needsTextBox();
        nameFilterField.setVisible(textboxEnabled);
        if (!textboxEnabled) nameFilterField.setFocused(false);

    }

    @Override
    public void updateScreen() {
        super.updateScreen();
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
        upgradeInfo.add("gui.tab.upgrades.volume");
        upgradeInfo.add("gui.tab.upgrades.security");
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
            pressureStatText.add(TextFormatting.GRAY + "Usage:");
            pressureStatText.add(TextFormatting.BLACK.toString() + PneumaticValues.USAGE_UNIVERSAL_SENSOR + "mL/tick");
        }
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (SensorHandler.getInstance().getSensorFromPath(te.getSensorSetting()) == null) {
            textList.add(TextFormatting.GRAY + "No sensor selected!");
            textList.add(TextFormatting.BLACK + "Insert upgrades and select the desired sensor.");
        }
        if (!te.lastSensorError.isEmpty()) {
            textList.add(TextFormatting.GRAY + "Sensor error reported!");
            textList.add(TextFormatting.BLACK + te.lastSensorError);
        }
        for (int i = 0; i < te.getUpgradesInventory().getSlots(); i++) {
            ItemStack stack = te.getUpgradesInventory().getStackInSlot(i);
            if (stack.getItem() instanceof IPositionProvider) {
                BlockPos pos = ((IPositionProvider) stack.getItem()).getStoredPositions(stack).get(0);
                if (pos == null) {
                    textList.add(TextFormatting.GRAY + "The inserted GPS Tool doesn't have a coordinate selected!");
                    textList.add(TextFormatting.BLACK + "Insert a GPS Tool with a stored coordinate.");
                    break;
                }
                int sensorRange = te.getRange();
                if (Math.abs(pos.getX() - te.getPos().getX()) > sensorRange || Math.abs(pos.getY() - te.getPos().getY()) > sensorRange || Math.abs(pos.getZ() - te.getPos().getZ()) > sensorRange) {
                    textList.add(TextFormatting.GRAY + "The stored coordinate in the GPS Tool is out of the Sensor's range!");
                    textList.add(TextFormatting.BLACK + "Move the sensor closer, select a closer coordinate or insert Range Upgrades.");
                }
            }
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            page--;
            if (page <= 0) page = maxPage;
            updateButtons();
        } else if (button.id == 3) {
            page++;
            if (page > maxPage) page = 1;
            updateButtons();
        } else if (button.id == -1) {
            te.showRangeLines();
        } else {
            super.actionPerformed(button);
        }
    }
}
