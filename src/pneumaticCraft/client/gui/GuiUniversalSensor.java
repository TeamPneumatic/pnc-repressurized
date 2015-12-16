package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.api.universalSensor.ISensorSetting;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerUniversalSensor;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdateTextfield;
import pneumaticCraft.common.sensor.SensorHandler;
import pneumaticCraft.common.tileentity.TileEntityUniversalSensor;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiUniversalSensor extends GuiPneumaticContainerBase<TileEntityUniversalSensor>{
    private GuiAnimatedStat sensorInfoStat;
    private GuiTextField nameFilterField;
    private int page;
    private int maxPage;
    private static final int MAX_SENSORS_PER_PAGE = 4;
    private int ticksExisted;

    public GuiUniversalSensor(InventoryPlayer player, TileEntityUniversalSensor te){
        super(new ContainerUniversalSensor(player, te), te, Textures.GUI_UNIVERSAL_SENSOR);
        ySize = 239;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        sensorInfoStat = addAnimatedStat("Sensor Info", new ItemStack(Blockss.universalSensor), 0xFFFFAA00, false);
        addAnimatedStat("gui.tab.upgrades", Textures.GUI_UPGRADES_LOCATION, 0xFF0000FF, true).setText(getUpgradeText());

        nameFilterField = new GuiTextField(fontRendererObj, xStart + 70, yStart + 58, 100, 10);
        nameFilterField.setText(te.getText(0));

        updateButtons();//also adds the redstoneButton.
    }

    @Override
    protected boolean shouldAddUpgradeTab(){
        return false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        super.drawGuiContainerForegroundLayer(x, y);
        if(maxPage > 1) fontRendererObj.drawString(page + "/" + maxPage, 110, 46 + 22 * MAX_SENSORS_PER_PAGE, 4210752);
        fontRendererObj.drawString("Upgr.", 23, 98, 4210752);

        String[] folders = te.getSensorSetting().split("/");
        if(folders.length == 1) {
            ItemStack[] requiredStacks = SensorHandler.instance().getRequiredStacksFromText(folders[0]);
            for(int i = 0; i < requiredStacks.length; i++) {
                GuiUtils.drawItemStack(requiredStacks[i], 102 + i * 18, 20);
            }
        } else {
            fontRendererObj.drawString(folders[folders.length - 1], 102, 24, 4210752);
        }

    }

    @Override
    protected Point getInvTextOffset(){
        return new Point(0, 2);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        nameFilterField.drawTextBox();

        ISensorSetting sensor = SensorHandler.instance().getSensorFromPath(te.getSensorSetting());
        if(sensor != null) {
            sensor.drawAdditionalInfo(fontRendererObj);
        }
    }

    @Override
    public String getRedstoneButtonText(int mode){
        return te.invertedRedstone ? "gui.tab.redstoneBehaviour.universalSensor.button.inverted" : "gui.tab.redstoneBehaviour.universalSensor.button.normal";
    }

    @Override
    public String getRedstoneString(){
        return "gui.tab.redstoneBehaviour.universalSensor.redstoneEmission";
    }

    @Override
    protected Point getGaugeLocation(){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + 34, yStart + ySize * 1 / 4);
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);

        nameFilterField.mouseClicked(par1, par2, par3);
    }

    @Override
    protected void keyTyped(char par1, int par2){
        if(nameFilterField.isFocused() && par2 != 1) {
            nameFilterField.textboxKeyTyped(par1, par2);
            te.setText(0, nameFilterField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        } else {
            super.keyTyped(par1, par2);
        }
    }

    public void updateButtons(){
        buttonList.clear();
        buttonList.add(redstoneButton);
        if(!te.getSensorSetting().equals("")) {
            buttonList.add(new GuiButton(1, guiLeft + 70, guiTop + 18, 30, 20, "back"));
        } else {
            buttonList.add(new GuiButton(-1, guiLeft + 70, guiTop + 125, 98, 20, I18n.format("gui.universalSensor.button.showRange")));
        }
        String[] directories = SensorHandler.instance().getDirectoriesAtLocation(te.getSensorSetting());
        maxPage = (directories.length - 1) / MAX_SENSORS_PER_PAGE + 1;
        if(page > maxPage) page = maxPage;
        if(page < 1) page = 1;
        if(maxPage > 1) {
            buttonList.add(new GuiButton(2, guiLeft + 70, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, "<--"));
            buttonList.add(new GuiButton(3, guiLeft + 138, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, "-->"));
        }

        int buttonsOnPage = MAX_SENSORS_PER_PAGE;
        if(page == maxPage && (directories.length % MAX_SENSORS_PER_PAGE > 0 || directories.length == 0)) {
            buttonsOnPage = directories.length % MAX_SENSORS_PER_PAGE;
        }
        for(int i = 0; i < buttonsOnPage; i++) {
            String buttonText = directories[i + (page - 1) * MAX_SENSORS_PER_PAGE];
            if(SensorHandler.instance().getSensorFromPath(te.getSensorSetting() + "/" + buttonText) != null) {
                buttonText = EnumChatFormatting.YELLOW + buttonText;
            }
            int buttonID = i * 10 + 10 + (page - 1) * MAX_SENSORS_PER_PAGE * 10;
            int buttonX = guiLeft + 70;
            int buttonY = guiTop + 40 + i * 22;
            int buttonWidth = 98;
            int buttonHeight = 20;
            if(te.getSensorSetting().equals("")) {
                ItemStack[] requiredStacks = SensorHandler.instance().getRequiredStacksFromText(buttonText);
                GuiButtonSpecial button = new GuiButtonSpecial(buttonID, buttonX, buttonY, buttonWidth, buttonHeight, "");
                button.setRenderStacks(requiredStacks);
                button.enabled = te.areGivenUpgradesInserted(requiredStacks);
                buttonList.add(button);
            } else {
                buttonList.add(new GuiButton(buttonID, buttonX, buttonY, buttonWidth, buttonHeight, buttonText));
            }
        }
        sensorInfoStat.setText(getSensorInfo());
        ISensorSetting sensor = SensorHandler.instance().getSensorFromPath(te.getSensorSetting());
        boolean textboxEnabled = sensor != null && sensor.needsTextBox();
        nameFilterField.setVisible(textboxEnabled);
        if(!textboxEnabled) nameFilterField.setFocused(false);

    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(te.getSensorSetting().equals("") && ticksExisted++ > 5) {
            ticksExisted = 0;
            updateButtons();
        }
        if(!nameFilterField.isFocused()) {
            nameFilterField.setText(te.getText(0));
        }
    }

    private List<String> getUpgradeText(){
        List<String> upgradeInfo = new ArrayList<String>();
        upgradeInfo.add("gui.tab.upgrades.volume");
        upgradeInfo.add("gui.tab.upgrades.security");
        upgradeInfo.addAll(SensorHandler.instance().getUpgradeInfo());
        return upgradeInfo;
    }

    private List<String> getSensorInfo(){
        List<String> text = new ArrayList<String>();
        ISensorSetting sensor = SensorHandler.instance().getSensorFromPath(te.getSensorSetting());
        if(sensor != null) {
            String[] folders = te.getSensorSetting().split("/");
            text.add(EnumChatFormatting.GRAY + folders[folders.length - 1]);
            text.addAll(sensor.getDescription());
        } else {
            text.add(EnumChatFormatting.BLACK + "No sensor selected.");
        }
        return text;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);
        if(te.isSensorActive) {
            pressureStatText.add(EnumChatFormatting.GRAY + "Usage:");
            pressureStatText.add(EnumChatFormatting.BLACK.toString() + PneumaticValues.USAGE_UNIVERSAL_SENSOR + "mL/tick");
        }
    }

    @Override
    protected void addProblems(List<String> textList){
        super.addProblems(textList);
        if(SensorHandler.instance().getSensorFromPath(te.getSensorSetting()) == null) {
            textList.add(EnumChatFormatting.GRAY + "No sensor selected!");
            textList.add(EnumChatFormatting.BLACK + "Insert upgrades and select the desired sensor.");
        }
        for(int i = TileEntityUniversalSensor.UPGRADE_SLOT_1; i <= TileEntityUniversalSensor.UPGRADE_SLOT_4; i++) {
            if(te.getStackInSlot(i) != null && te.getStackInSlot(i).getItem() == Itemss.GPSTool) {
                if(te.getStackInSlot(i).hasTagCompound()) {
                    NBTTagCompound gpsTag = te.getStackInSlot(i).getTagCompound();
                    int toolX = gpsTag.getInteger("x");
                    int toolY = gpsTag.getInteger("y");
                    int toolZ = gpsTag.getInteger("z");
                    if(toolX == 0 && toolY == 0 && toolZ == 0) {
                        textList.add(EnumChatFormatting.GRAY + "The inserted GPS Tool doesn't have a coordinate selected!");
                        textList.add(EnumChatFormatting.BLACK + "Insert a GPS Tool with stored coordinate.");
                        break;
                    }
                    int sensorRange = te.getRange();
                    if(Math.abs(toolX - te.xCoord) > sensorRange || Math.abs(toolY - te.yCoord) > sensorRange || Math.abs(toolZ - te.zCoord) > sensorRange) {
                        textList.add(EnumChatFormatting.GRAY + "The stored coordinate in the GPS Tool is out of the Sensor's range!");
                        textList.add(EnumChatFormatting.BLACK + "Move the sensor closer, select a closer coordinate or insert Range Upgrades.");
                    }
                } else {
                    textList.add(EnumChatFormatting.GRAY + "The inserted GPS Tool doesn't have a coordinate selected!");
                    textList.add(EnumChatFormatting.BLACK + "Insert a GPS Tool with stored coordinate.");
                }
                break;
            }
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button){
        if(button.id == 2) {
            page--;
            if(page <= 0) page = maxPage;
            updateButtons();
        } else if(button.id == 3) {
            page++;
            if(page > maxPage) page = 1;
            updateButtons();
        } else if(button.id == -1) {
            te.showRangeLines();
        } else {
            super.actionPerformed(button);
        }
    }
}
